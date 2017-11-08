package com.jverson.springboot.conf;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class SchedulingConfig implements SchedulingConfigurer {

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setScheduler(taskExecutor());
	}

	@Bean(destroyMethod = "shutdown")
	public Executor taskExecutor() {
		/**
		 * 注意这里创建的Executor是ScheduledThreadPoolExecutor，而它实现了ScheduledExecutorService接口
		 * 因此本质上还是利用concurrent包里的ScheduledExecutorService实现
		 */
		return Executors.newScheduledThreadPool(100);
	}
}
