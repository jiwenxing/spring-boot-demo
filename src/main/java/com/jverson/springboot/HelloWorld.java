package com.jverson.springboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jverson.springboot.bean.Car;
import com.jverson.springboot.bean.Person;
import com.jverson.springboot.bean.Person2;

@Controller
@EnableAutoConfiguration
@ComponentScan
//@SpringBootApplication 
public class HelloWorld {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired Car car;
	@Autowired Person person;
	
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
//		SpringApplication.run(HelloWorld.class, args);
//		SpringApplication app = new SpringApplication(HelloWorld.class);
//		app.setBannerMode(Banner.Mode.OFF);
//		app.run(args);
		new SpringApplicationBuilder().sources(HelloWorld.class)
		    .bannerMode(Banner.Mode.LOG).run(args);
	}
	
}
