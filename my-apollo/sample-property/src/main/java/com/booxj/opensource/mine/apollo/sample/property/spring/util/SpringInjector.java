package com.booxj.opensource.mine.apollo.sample.property.spring.util;

import com.booxj.opensource.mine.apollo.sample.property.spring.config.ConfigPropertySourceFactory;
import com.booxj.opensource.mine.apollo.sample.property.spring.property.PlaceholderHelper;
import com.booxj.opensource.mine.apollo.sample.property.spring.property.SpringValueRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpringInjector {

    private static Logger logger = LoggerFactory.getLogger(SpringInjector.class);

    private static volatile Injector injector;
    private static final Object lock = new Object();

    private static Injector getInjector() {
        if (injector == null) {
            synchronized (lock) {
                if (injector == null) {
                    try {
                        injector = Guice.createInjector(new SpringModule());
                    } catch (Throwable ex) {
                        logger.error("Unable to initialize Apollo Spring Injector!");
                        throw ex;
                    }
                }
            }
        }

        return injector;
    }

    public static <T> T getInstance(Class<T> clazz) {
        try {
            return getInjector().getInstance(clazz);
        } catch (Throwable ex) {
            logger.error("Unable to load instance for %s!", clazz.getName());
            throw ex;
        }
    }

    private static class SpringModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(PlaceholderHelper.class).in(Singleton.class);
            bind(ConfigPropertySourceFactory.class).in(Singleton.class);
            bind(SpringValueRegistry.class).in(Singleton.class);
        }
    }
}
