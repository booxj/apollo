package com.booxj.opensource.mine.apollo.sample.property;

import com.booxj.opensource.mine.apollo.sample.property.spring.annotation.EnableApolloConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
@EnableApolloConfig
public class PropertiesApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(PropertiesApplication.class).run(args);
    }
}
