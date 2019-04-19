package com.booxj.opensource.mine.apollo.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 封装ThreadFactory，
 */
public class ApolloThreadFactory implements ThreadFactory {

    private static Logger logger = LoggerFactory.getLogger(ApolloThreadFactory.class);

    private final AtomicLong threadNumber = new AtomicLong(1);

    private final String namePrefix;
    private final boolean daemon;
    private static final ThreadGroup threadGroup = new ThreadGroup("Apollo");

    private ApolloThreadFactory(String namePrefix, boolean daemon) {
        this.namePrefix = namePrefix;
        this.daemon = daemon;
    }

    public static ThreadFactory create(String namePrefix, boolean daemon) {
        return new ApolloThreadFactory(namePrefix, daemon);
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(threadGroup, runnable,
                threadGroup.getName() + "-" + namePrefix + "-" + threadNumber.getAndIncrement());

        thread.setDaemon(daemon);

        if (thread.getPriority() != Thread.NORM_PRIORITY) {
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        return thread;
    }

}
