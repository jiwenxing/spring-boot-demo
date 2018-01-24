package com.jverson.springboot.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class SpringAsyncDemo {
	private static final Logger log = LoggerFactory.getLogger(SpringAsyncDemo.class);
	
	@Async
	public void sayHello(){
		try {
			Thread.sleep(1000*5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		log.info("天王盖地虎，");
		log.info(Thread.currentThread().getName());
	}
	
}
