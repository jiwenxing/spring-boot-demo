package com.jverson.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.jverson.springboot.HelloSpringBoot;
import com.jverson.springboot.condition.CmdService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = HelloSpringBoot.class)
public class SpringConditionalDemoTest {

	@Autowired
	private CmdService cmdService;
	
	@Test
	public void testConditional(){
		System.out.println(cmdService.getListCmd());
	}
	
}
