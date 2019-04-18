package com.booxj.opensource.mine.apollo.sample.property.spring.annotation;

import com.booxj.opensource.mine.apollo.core.ConfigConstants;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(ApolloConfigRegistrar.class)
public @interface EnableApolloConfig {

    String[] value() default {ConfigConstants.DEFAULT_NAMESPACE_APPLICATION};

    int order() default Ordered.LOWEST_PRECEDENCE;
}
