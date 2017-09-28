package com.jverson.springboot;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication 
@MapperScan(basePackages = "com.jverson.springboot.mapper")
public class HelloSpringBoot {

	private static Logger logger = LoggerFactory.getLogger("com.jverson.springboot.HelloWorld");
	
	public static void main(String[] args) {
		/**
		 * 启动方式1
		 */
//		SpringApplication.run(HelloWorld.class, args);
		/**
		 * 启动方式2
		 */
//		SpringApplication app = new SpringApplication(HelloWorld.class);
//		app.setBannerMode(Banner.Mode.OFF);
//		app.run(args);
		/**
		 * 启动方式3
		 */
		ApplicationContext ctx = new SpringApplicationBuilder().sources(HelloSpringBoot.class)
		    .bannerMode(Banner.Mode.LOG).run(args);
		String[] activeProfiles = ctx.getEnvironment().getActiveProfiles();
		for (String string : activeProfiles) {
			logger.warn("the active profile is: " + string);
		}
	}
	
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.setConnectTimeout(1000).setReadTimeout(1000).build();
	}
	
}
