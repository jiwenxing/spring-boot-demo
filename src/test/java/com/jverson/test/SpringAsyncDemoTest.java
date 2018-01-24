package com.jverson.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.jverson.springboot.HelloSpringBoot;
import com.jverson.springboot.async.SpringAsyncDemo;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = HelloSpringBoot.class)
public class SpringAsyncDemoTest {

	private static final Logger log = LoggerFactory.getLogger(SpringAsyncDemoTest.class);
	
	@Autowired
	private SpringAsyncDemo springAsyncDemo;
	
	@Test
	public void sayHelloTest() throws InterruptedException{
		springAsyncDemo.sayHello();
		log.info("宝塔镇河妖！");
		Thread.currentThread().join();
	}
	
}
