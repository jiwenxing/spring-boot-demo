package com.jverson.springboot.event;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 使用 ThreadPoolTaskExecutor 自定义线程池替代默认的线程
 */

@Configuration
@EnableAsync
public class AsyncConf {

	@Bean
	public AsyncTaskExecutor taskExecutor() {  
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor(); 
        executor.setThreadNamePrefix("Anno-Executor-Max10PoolSize");
        executor.setMaxPoolSize(2);  
        return executor;  
    } 
	
}
