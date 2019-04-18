package com.booxj.opensource.mine.apollo.sample.property.spring.config;


import com.booxj.opensource.mine.apollo.sample.property.constants.PropertySourcesConstants;
import com.booxj.opensource.mine.apollo.sample.property.internals.ConfigRepository;
import com.booxj.opensource.mine.apollo.sample.property.spring.annotation.ApolloConfigRegistrar;
import com.booxj.opensource.mine.apollo.sample.property.spring.property.AutoUpdateConfigChangeListener;
import com.booxj.opensource.mine.apollo.sample.property.spring.util.SpringInjector;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Apollo 配置处理器
 * <p>
 * BeanFactoryPostProcessor：在BeanDefinition加载之后，Bean实例化之前运行
 * EnvironmentAware：获得environment，修改PropertySources
 * <p>
 * PriorityOrdered：实现BeanPostProcessor接口的注册顺序
 * 1. 实现了PriorityOrdered接口的，排序后
 * 2. 实现了Ordered接口的，排序后
 * 3. 既没实现PriorityOrdered接口，也没有实现Ordered接口的
 * 4. 实现了MergedBeanDefinitionPostProcessor接口的
 */
public class PropertySourcesProcessor implements BeanFactoryPostProcessor, EnvironmentAware, PriorityOrdered {

    private static Logger logger = LoggerFactory.getLogger(PropertySourcesProcessor.class);

    /**
     * 需要解析的配置文件名，默认为application
     * <p>
     * 加载时机
     *
     * @see ApolloConfigRegistrar#registerBeanDefinitions(org.springframework.core.type.AnnotationMetadata, org.springframework.beans.factory.support.BeanDefinitionRegistry)
     */
    private static final Multimap<Integer, String> NAMESPACE_NAMES = LinkedHashMultimap.create();

    private ConfigurableEnvironment environment;

    private final ConfigPropertySourceFactory configPropertySourceFactory = SpringInjector.getInstance(ConfigPropertySourceFactory.class);

    public static boolean addNamespaces(Collection<String> namespaces, int order) {
        return NAMESPACE_NAMES.putAll(order, namespaces);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 初始化 PropertySource
        initializePropertySources();
        // 初始化 AutoUpdateConfigChangeListener 对象，实现属性的自动更新
        initializeAutoUpdatePropertiesFeature(beanFactory);
    }

    private void initializePropertySources() {

        if (environment.getPropertySources().contains(PropertySourcesConstants.APOLLO_PROPERTY_SOURCE_NAME)) {
            //already initialized
            return;
        }


        CompositePropertySource composite = new CompositePropertySource(PropertySourcesConstants.APOLLO_PROPERTY_SOURCE_NAME);

        //sort by order asc
        ImmutableSortedSet<Integer> orders = ImmutableSortedSet.copyOf(NAMESPACE_NAMES.keySet());
        Iterator<Integer> iterator = orders.iterator();

        while (iterator.hasNext()) {
            int order = iterator.next();
            for (String namespace : NAMESPACE_NAMES.get(order)) {
                // TODO: 2019/4/16 获取配置
                Config config = getConfig(namespace);

                composite.addFirstPropertySource(configPropertySourceFactory.getConfigPropertySource(namespace, config));
            }
        }

        // clean up
        NAMESPACE_NAMES.clear();

        // add after the bootstrap property source or to the first
        if (environment.getPropertySources()
                .contains(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME)) {

            // ensure ApolloBootstrapPropertySources is still the first
            ensureBootstrapPropertyPrecedence(environment);

            environment.getPropertySources()
                    .addAfter(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME, composite);
        } else {
            environment.getPropertySources().addFirst(composite);
        }
    }

    private void initializeAutoUpdatePropertiesFeature(ConfigurableListableBeanFactory beanFactory) {

        // 配置自动更新 监听器
        AutoUpdateConfigChangeListener autoUpdateConfigChangeListener = new AutoUpdateConfigChangeListener(environment, beanFactory);

        List<ConfigPropertySource> configPropertySources = configPropertySourceFactory.getAllConfigPropertySources();
        for (ConfigPropertySource configPropertySource : configPropertySources) {
            configPropertySource.addChangeListener(autoUpdateConfigChangeListener);
        }
    }

    private void ensureBootstrapPropertyPrecedence(ConfigurableEnvironment environment) {
        MutablePropertySources propertySources = environment.getPropertySources();

        PropertySource<?> bootstrapPropertySource = propertySources.get(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME);

        // not exists or already in the first place
        if (bootstrapPropertySource == null || propertySources.precedenceOf(bootstrapPropertySource) == 0) {
            return;
        }

        propertySources.remove(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME);
        propertySources.addFirst(bootstrapPropertySource);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private Config getConfig(String namespace) {
        ConfigRepository configRepository = new ConfigRepository();
        Properties properties = new Properties();
        properties.put("spring.application.name", "sample-property");
        properties.put("server.port", "8888");
        properties.put("apollo.value", "application");

        /**
         * 创建 DefaultConfig 时需要传参 ConfigRepository
         * 因为 DefaultConfig 本身是一个监听器（RepositoryChangeListener），需要监听 ConfigRepository 发送的配置修改事件（ConfigChangeEvent）
         */
        return new DefaultConfig(namespace, configRepository, properties);
    }


}
