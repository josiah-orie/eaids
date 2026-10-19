package com.feadev.eaids.config;

import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configures Spring's asynchronous method execution support.
 *
 * <p>Provides a bounded {@link ThreadPoolTaskExecutor} for all {@code @Async} methods
 * and an {@link AsyncUncaughtExceptionHandler} that logs failures at ERROR level so
 * exceptions thrown in async threads (e.g. post-game stat computation) are not
 * silently discarded.
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("eaids-async-");
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
                LoggerFactory.getLogger(AsyncConfig.class)
                        .error("Async method '{}' threw an uncaught exception: {}",
                                method.getName(), ex.getMessage(), ex);
    }
}
