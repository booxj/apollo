package com.booxj.opensource.mine.apollo.sample.property.spring.annotation;

import com.booxj.opensource.mine.apollo.sample.property.spring.config.PropertySourcesProcessor;
import com.booxj.opensource.mine.apollo.sample.property.spring.property.SpringValueDefinitionProcessor;
import com.booxj.opensource.mine.apollo.sample.property.spring.util.BeanRegistrationUtil;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.HashMap;
import java.util.Map;

/**
 * 加载 Apollo 需要的 Bean 到 IOC 容器
 */
public class ApolloConfigRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(EnableApolloConfig.class.getName()));
        String[] namespaces = attributes.getStringArray("value");
        int order = attributes.getNumber("order");

        //加入需要导入的配置文件，默认为application
        PropertySourcesProcessor.addNamespaces(Lists.newArrayList(namespaces), order);

        Map<String, Object> propertySourcesPlaceholderPropertyValues = new HashMap<>();
        // to make sure the default PropertySourcesPlaceholderConfigurer's priority is higher than PropertyPlaceholderConfigurer
        propertySourcesPlaceholderPropertyValues.put("order", 0);

        // PropertyPlaceholderConfigurer:这个类是把所有的属性集中放到Properties中
        // PropertySourcesPlaceholderConfigurer:该类有一个PropertySources的集合
        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry,
                PropertySourcesPlaceholderConfigurer.class.getName(),
                PropertySourcesPlaceholderConfigurer.class,
                propertySourcesPlaceholderPropertyValues);


        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry,
                PropertySourcesProcessor.class.getName(),
                PropertySourcesProcessor.class);

        // SpringValueDefinitionProcessor
        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry,
                SpringValueDefinitionProcessor.class.getName(),
                SpringValueDefinitionProcessor.class);

        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry,
                SpringValueProcessor.class.getName(),
                SpringValueProcessor.class);
    }
}
