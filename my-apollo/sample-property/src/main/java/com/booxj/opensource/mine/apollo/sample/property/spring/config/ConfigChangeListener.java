package com.booxj.opensource.mine.apollo.sample.property.spring.config;


import com.booxj.opensource.mine.apollo.sample.property.model.ConfigChangeEvent;

/**
 * 配置修改事件监听器
 */
public interface ConfigChangeListener {

    void onChange(ConfigChangeEvent changeEvent);
}
