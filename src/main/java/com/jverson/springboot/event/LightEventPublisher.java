package com.jverson.springboot.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jverson.springboot.event.LightEvent.LightColorEnum;

@RestController
public class LightEventPublisher {
	private static final Logger log = LoggerFactory.getLogger(LightEventPublisher.class);
	/**
	 * 这里注入 ApplicationContext 和 ApplicationEventPublisher 是等价的，后者是一个接口，前者继承了该接口
	 * 也就是说 ApplicationContext 本身提供了发布 event 的能力
	 */
	/*@Autowired
	private ApplicationContext context;*/
	@Autowired
    private ApplicationEventPublisher applicationEventPublisher;
	
	@RequestMapping("/publish")
	public Object publish(){
		LightEvent lightEvent = new LightEvent("", LightColorEnum.YELLOW);
		applicationEventPublisher.publishEvent(lightEvent);
		log.info("things after publish");
//        System.out.println("publish thread name: "+Thread.currentThread().getName());
		return "ok";
	}
}
