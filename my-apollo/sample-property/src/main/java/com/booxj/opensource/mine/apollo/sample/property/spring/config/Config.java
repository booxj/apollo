package com.booxj.opensource.mine.apollo.sample.property.spring.config;

import java.util.Set;

/**
 * 定义基础 config 接口
 *
 * 具有获取属性和管理监听器的功能
 */
public interface Config {

    String getProperty(String key, String defaultValue);

    Set<String> getPropertyNames();

    void addChangeListener(ConfigChangeListener listener);

    boolean removeChangeListener(ConfigChangeListener listener);
}
