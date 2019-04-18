package com.booxj.opensource.mine.apollo.sample.property.spring.config;

import com.booxj.opensource.mine.apollo.core.utils.ApolloThreadFactory;
import com.booxj.opensource.mine.apollo.sample.property.enums.PropertyChangeType;
import com.booxj.opensource.mine.apollo.sample.property.internals.ConfigRepository;
import com.booxj.opensource.mine.apollo.sample.property.internals.RepositoryChangeListener;
import com.booxj.opensource.mine.apollo.sample.property.model.ConfigChange;
import com.booxj.opensource.mine.apollo.sample.property.model.ConfigChangeEvent;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 */
public class DefaultConfig implements Config, RepositoryChangeListener {

    private static Logger logger = LoggerFactory.getLogger(DefaultConfig.class);

    private static final ExecutorService executorService;

    private final List<ConfigChangeListener> listeners = Lists.newCopyOnWriteArrayList();
    private final ConfigRepository configRepository;
    private final String namespace;
    private final AtomicReference<Properties> configProperties;

    static {
        executorService = Executors.newCachedThreadPool(ApolloThreadFactory.create("Config", true));
    }

    public DefaultConfig(String namespace, ConfigRepository configRepository, Properties newConfigProperties) {
        this.namespace = namespace;
        this.configProperties = new AtomicReference<>();
        this.configProperties.set(newConfigProperties);
        this.configRepository = configRepository;
        this.configRepository.addChangeListener(this);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        // step 1: check system properties, i.e. -Dkey=value
        String value = System.getProperty(key);

        // step 2: check local cached properties file
        if (value == null && configProperties.get() != null) {
            value = configProperties.get().getProperty(key);
        }

        /**
         * step 3: check env variable, i.e. PATH=...
         * normally system environment variables are in UPPERCASE, however there might be exceptions.
         * so the caller should provide the key in the right case
         */
        if (value == null) {
            value = System.getenv(key);
        }

        // step 4: check properties file from classpath
//        if (value == null && resourceProperties != null) {
//            value = (String) resourceProperties.get(key);
//        }

        return value == null ? defaultValue : value;
    }

    @Override
    public Set<String> getPropertyNames() {
        Properties properties = configProperties.get();
        if (properties == null) {
            return Collections.emptySet();
        }

        return stringPropertyNames(properties);
    }

    private Set<String> stringPropertyNames(Properties properties) {
        //jdk9以下版本Properties#enumerateStringProperties方法存在性能问题，keys() + get(k) 重复迭代, jdk9之后改为entrySet遍历.
        Map<String, String> h = new HashMap<>();
        for (Map.Entry<Object, Object> e : properties.entrySet()) {
            Object k = e.getKey();
            Object v = e.getValue();
            if (k instanceof String && v instanceof String) {
                h.put((String) k, (String) v);
            }
        }
        return h.keySet();
    }

    @Override
    public void addChangeListener(ConfigChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public boolean removeChangeListener(ConfigChangeListener listener) {
        return listeners.remove(listener);
    }

    @Override
    public void onRepositoryChange(String namespace, Properties newProperties) {
        if (newProperties.equals(configProperties.get())) {
            return;
        }

        Properties newConfigProperties = new Properties();
        newConfigProperties.putAll(newProperties);

        Map<String, ConfigChange> actualChanges = updateAndCalcConfigChanges(newConfigProperties);

        //check double checked result
        if (actualChanges.isEmpty()) {
            return;
        }

        this.fireConfigChange(new ConfigChangeEvent(namespace, actualChanges));

        logger.info("Apollo.Client.ConfigChanges {}", namespace);

    }

    private Map<String, ConfigChange> updateAndCalcConfigChanges(Properties newConfigProperties) {
        List<ConfigChange> configChanges = calcPropertyChanges(namespace, configProperties.get(), newConfigProperties);

        ImmutableMap.Builder<String, ConfigChange> actualChanges = new ImmutableMap.Builder<>();

        //1. use getProperty to update configChanges's old value
        for (ConfigChange change : configChanges) {
            change.setOldValue(this.getProperty(change.getPropertyName(), change.getOldValue()));
        }

        //2. update m_configProperties
        updateConfig(newConfigProperties);
//        clearConfigCache();

        //3. use getProperty to update configChange's new value and calc the final changes
        for (ConfigChange change : configChanges) {
            change.setNewValue(this.getProperty(change.getPropertyName(), change.getNewValue()));
            switch (change.getChangeType()) {
                case ADDED:
                    if (java.util.Objects.equals(change.getOldValue(), change.getNewValue())) {
                        break;
                    }
                    if (change.getOldValue() != null) {
                        change.setChangeType(PropertyChangeType.MODIFIED);
                    }
                    actualChanges.put(change.getPropertyName(), change);
                    break;
                case MODIFIED:
                    if (!java.util.Objects.equals(change.getOldValue(), change.getNewValue())) {
                        actualChanges.put(change.getPropertyName(), change);
                    }
                    break;
                case DELETED:
                    if (java.util.Objects.equals(change.getOldValue(), change.getNewValue())) {
                        break;
                    }
                    if (change.getNewValue() != null) {
                        change.setChangeType(PropertyChangeType.MODIFIED);
                    }
                    actualChanges.put(change.getPropertyName(), change);
                    break;
                default:
                    //do nothing
                    break;
            }
        }
        return actualChanges.build();
    }

    List<ConfigChange> calcPropertyChanges(String namespace, Properties previous,
                                           Properties current) {
        if (previous == null) {
            previous = new Properties();
        }

        if (current == null) {
            current = new Properties();
        }

        Set<String> previousKeys = previous.stringPropertyNames();
        Set<String> currentKeys = current.stringPropertyNames();

        Set<String> commonKeys = Sets.intersection(previousKeys, currentKeys);
        Set<String> newKeys = Sets.difference(currentKeys, commonKeys);
        Set<String> removedKeys = Sets.difference(previousKeys, commonKeys);

        List<ConfigChange> changes = Lists.newArrayList();

        for (String newKey : newKeys) {
            changes.add(new ConfigChange(namespace, newKey, null, current.getProperty(newKey),
                    PropertyChangeType.ADDED));
        }

        for (String removedKey : removedKeys) {
            changes.add(new ConfigChange(namespace, removedKey, previous.getProperty(removedKey), null, PropertyChangeType.DELETED));
        }

        for (String commonKey : commonKeys) {
            String previousValue = previous.getProperty(commonKey);
            String currentValue = current.getProperty(commonKey);
            if (Objects.equal(previousValue, currentValue)) {
                continue;
            }
            changes.add(new ConfigChange(namespace, commonKey, previousValue, currentValue,
                    PropertyChangeType.MODIFIED));
        }

        return changes;
    }

    private void updateConfig(Properties newConfigProperties) {
        configProperties.set(newConfigProperties);
    }

    protected void fireConfigChange(final ConfigChangeEvent changeEvent) {
        for (final ConfigChangeListener listener : listeners) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    String listenerName = listener.getClass().getName();
                    try {
                        listener.onChange(changeEvent);
                    } catch (Throwable ex) {
                        logger.error("Failed to invoke config change listener {}", listenerName, ex);
                    }

                }
            });
        }
    }

}
