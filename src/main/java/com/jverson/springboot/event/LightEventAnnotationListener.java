package com.jverson.springboot.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.mysql.jdbc.log.Log;

/**
 * 注解方式实现的 application event 的 listener，更加简洁
 * 使用 @EventListener 即可以将一个方法标注为一个listener
 * 注意需要将类注解为 spring 管理的 bean
 * 使用 @Async 可以实现监听器的异步调用（需要在配置类上注解 @EnableAsync 开启异步支持）
 */

@Component
public class LightEventAnnotationListener {
	private static final Logger log = LoggerFactory.getLogger(LightEventAnnotationListener.class);
	@EventListener
	@Async
	public void handler(LightEvent event){
		try {
			Thread.sleep(5000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info(event.getLightColor().getMessage());
		log.info(Thread.currentThread().getName());
	}
}
