package com.jverson.springboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jverson.springboot.bean.Car;

@Controller
@EnableAutoConfiguration
@ComponentScan
//@SpringBootApplication 
public class HelloWorld {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired Car car;
	
	@RequestMapping("/")
	@ResponseBody
	String home(){
		logger.info("info  "+car.toString());
		logger.warn("warn  "+car.toString());
		logger.error("error  "+car.toString());
		
		return car.toString();
	}
	
	@RequestMapping("/test")
	String error(){
		return "error";
	}
	
	public static void main(String[] args) {
		SpringApplication.run(HelloWorld.class, args);
	}
	
}
