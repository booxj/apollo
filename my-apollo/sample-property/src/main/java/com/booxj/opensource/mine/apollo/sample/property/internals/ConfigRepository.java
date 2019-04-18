package com.booxj.opensource.mine.apollo.sample.property.internals;

import com.booxj.opensource.mine.apollo.Apollo;
import com.booxj.opensource.mine.apollo.core.dto.ApolloConfig;
import com.booxj.opensource.mine.apollo.core.utils.ApolloThreadFactory;
import com.booxj.opensource.mine.apollo.sample.property.utils.ExceptionUtil;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ConfigRepository
 *
 * @description:获取配置（自定义），更新配置事件的发生起点
 */
public class ConfigRepository {

    private static Logger logger = LoggerFactory.getLogger(ConfigRepository.class);

    private List<RepositoryChangeListener> listeners = Lists.newCopyOnWriteArrayList();

    private volatile AtomicReference<ApolloConfig> configCache;
    private final String namespace = "application";

    // 定时任务，定时查询配置是否更新，如果有更新，发送数据变化事件（RepositoryChangeListener）
    private final static ScheduledExecutorService executorService;

    static {
        executorService = Executors.newScheduledThreadPool(1, ApolloThreadFactory.create("ConfigRepository", true));
    }

    public ConfigRepository() {
        configCache = new AtomicReference<>();
        this.trySync();
        // 初始化定时刷新配置的任务
        this.schedulePeriodicRefresh();
        // 注册自己到 RemoteConfigLongPollService 中，实现配置更新的实时通知
    }


    private boolean trySync() {
        try {
            // 同步
            sync();
            return true;
        } catch (Throwable ex) {
            logger.error("Sync config failed, will retry. Repository {}, reason: {}", this.getClass(), ExceptionUtil.getDetailMessage(ex));
        }
        return false;
    }

    private void schedulePeriodicRefresh() {
        logger.info("Schedule periodic refresh with interval: {} {}", 60, 60);
        // 创建定时任务，定时刷新配置
        executorService.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        logger.info("Apollo.ConfigService " + String.format("periodicRefresh: %s", namespace));
                        logger.info("refresh config for namespace: {}", namespace);
                        // 尝试同步配置
                        trySync();
                        logger.info("Apollo.Client.Version={}", Apollo.VERSION);
                    }
                }, 30, 10,
                TimeUnit.SECONDS);
    }

    synchronized void sync() {
        ApolloConfig previous = configCache.get();

        // apollo里初始值要去注册中心获取
        // 这里手动赋予一个初始值
        if (previous == null) {
            previous = buildConfig();
        }

        ApolloConfig current = buildConfig();

        // 比较两次配置是否一样，不一样则需要更新配置
        if (previous != current) {
            logger.info("Config refreshed!");
            logger.info("previous config releaseKey : " + previous.getReleaseKey());
            logger.info("current config releaseKey : " + previous.getReleaseKey());
            configCache.set(current);

            // 发布 Repository 的配置发生变化，触发对应的监听器们
            // 这里只更新application
            this.fireRepositoryChange("application", this.getConfig());
        }

        if (current != null) {
            logger.info(String.format("Apollo.Client.Configs.%s", current.getNamespaceName()), current.getReleaseKey());
        }
    }

    public Properties getConfig() {
        // 如果缓存为空，强制从 Config Service 拉取配置
        if (configCache.get() == null) {
            this.sync();
        }
        // 转换成 Properties 对象，并返回
        return transformApolloConfigToProperties(configCache.get());
    }

    private Properties transformApolloConfigToProperties(ApolloConfig apolloConfig) {
        Properties result = new Properties();
        result.putAll(apolloConfig.getConfigurations());
        return result;
    }

    // 模拟构造配置变化事件
    ApolloConfig buildConfig() {
        Map<String, String> configurations = new HashMap<>();
        configurations.put("apollo.value", String.valueOf(new Random().nextInt(10000)));
        return new ApolloConfig("application", configurations, System.currentTimeMillis() + "");
    }

    /**
     * 触发监听器
     *
     * @param namespace     Namespace 名字
     * @param newProperties 配置
     */
    protected void fireRepositoryChange(String namespace, Properties newProperties) {
        // 循环 RepositoryChangeListener 数组
        for (RepositoryChangeListener listener : listeners) {
            try {
                // 触发监听器
                listener.onRepositoryChange(namespace, newProperties);
            } catch (Throwable ex) {
                logger.error("Failed to invoke repository change listener {}", listener.getClass(), ex);
            }
        }
    }

    public void addChangeListener(RepositoryChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeChangeListener(RepositoryChangeListener listener) {
        listeners.remove(listener);
    }

}
