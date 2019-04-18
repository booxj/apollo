package com.booxj.opensource.mine.apollo.sample.property.spring.property;

import com.booxj.opensource.mine.apollo.sample.property.spring.util.SpringInjector;
import com.google.common.collect.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * BeanDefinitionRegistryPostProcessor继承了BeanFactoryPostProcessor接口,有两个方法
 * <p>
 * postProcessBeanFactory():主要用来对bean定义做一些改变
 * postProcessBeanDefinitionRegistry():用来注册更多的bean到spring容器
 */
public class SpringValueDefinitionProcessor implements BeanDefinitionRegistryPostProcessor {

    /**
     * Multimap<String,SpringValueDefinition> == Map<String,Set<SpringValueDefinition>>
     * <p>
     * Map<BeanDefinitionRegistry, Multimap<String, SpringValueDefinition>>
     * BeanDefinitionRegistry -> BeanDefinitionRegistry(IOC注册器)
     * String -> BeanName
     * SpringValueDefinition -> SpringValueDefinition(自定义Definition，用于保存bean的所有属性)
     */
    private static final Map<BeanDefinitionRegistry, Multimap<String, SpringValueDefinition>> beanName2SpringValueDefinitions =
            Maps.newConcurrentMap();

    private static final Set<BeanDefinitionRegistry> PROPERTY_VALUES_PROCESSED_BEAN_FACTORIES = Sets.newConcurrentHashSet();


    private final PlaceholderHelper placeholderHelper;

    public SpringValueDefinitionProcessor() {
        placeholderHelper = SpringInjector.getInstance(PlaceholderHelper.class);
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        processPropertyValues(registry);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    public static Multimap<String, SpringValueDefinition> getBeanName2SpringValueDefinitions(BeanDefinitionRegistry registry) {
        Multimap<String, SpringValueDefinition> springValueDefinitions = beanName2SpringValueDefinitions.get(registry);
        if (springValueDefinitions == null) {
            springValueDefinitions = LinkedListMultimap.create();
        }

        return springValueDefinitions;
    }

    private void processPropertyValues(BeanDefinitionRegistry beanRegistry) {
        if (!PROPERTY_VALUES_PROCESSED_BEAN_FACTORIES.add(beanRegistry)) {
            // already initialized
            return;
        }

        if (!beanName2SpringValueDefinitions.containsKey(beanRegistry)) {
            beanName2SpringValueDefinitions.put(beanRegistry, LinkedListMultimap.<String, SpringValueDefinition>create());
        }

        Multimap<String, SpringValueDefinition> springValueDefinitions = beanName2SpringValueDefinitions.get(beanRegistry);

        String[] beanNames = beanRegistry.getBeanDefinitionNames();
        // 遍历所有的Bean，
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = beanRegistry.getBeanDefinition(beanName);
            MutablePropertyValues mutablePropertyValues = beanDefinition.getPropertyValues();
            List<PropertyValue> propertyValues = mutablePropertyValues.getPropertyValueList();
            for (PropertyValue propertyValue : propertyValues) {
                Object value = propertyValue.getValue();
                if (!(value instanceof TypedStringValue)) {
                    continue;
                }
                String placeholder = ((TypedStringValue) value).getValue();
                Set<String> keys = placeholderHelper.extractPlaceholderKeys(placeholder);

                if (keys.isEmpty()) {
                    continue;
                }

                for (String key : keys) {
                    springValueDefinitions.put(beanName, new SpringValueDefinition(key, placeholder, propertyValue.getName()));
                }
            }
        }
    }

}
