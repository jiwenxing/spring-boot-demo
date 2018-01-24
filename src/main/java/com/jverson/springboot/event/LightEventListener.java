package com.jverson.springboot.event;

import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;

/**
 * 使用继承 ApplicationListener 的方式实现事件监听器，比较老的做法
 * spring4.2 开始还可以直接使用`@EventListener`注解将一个普通类注解为监听器，见""
 */

//@Component
public class LightEventListener implements ApplicationListener<LightEvent> {
	@Override
	@Async
	public void onApplicationEvent(LightEvent event) {
		try {
			Thread.sleep(10000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(event.getLightColor().getMessage());
	}
}
