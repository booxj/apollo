package com.booxj.opensource.mine.apollo.sample.property.spring.config;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * 配置工厂->
 * 用于保存 PropertyList
 * 通过 namespace 获取配置 PropertySource
 */
public class ConfigPropertySourceFactory {

    private final List<ConfigPropertySource> configPropertySources = Lists.newLinkedList();

    public ConfigPropertySource getConfigPropertySource(String name, Config source) {
        ConfigPropertySource configPropertySource = new ConfigPropertySource(name, source);

        configPropertySources.add(configPropertySource);

        return configPropertySource;
    }

    public List<ConfigPropertySource> getAllConfigPropertySources() {
        return Lists.newLinkedList(configPropertySources);
    }
}
