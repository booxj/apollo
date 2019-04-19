package com.booxj.opensource.mine.apollo.sample.configservice;


import com.booxj.opensource.mine.apollo.sample.metaservice.ApolloMetaServiceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestTemplate;

@EnableEurekaServer
@EnableAutoConfiguration // (exclude = EurekaClientConfigBean.class)
@Configuration
@PropertySource(value = {"classpath:configservice.properties"})
@ComponentScan(basePackageClasses = {ConfigServiceApplication.class, ApolloMetaServiceConfig.class})
public class ConfigServiceApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ConfigServiceApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

}
