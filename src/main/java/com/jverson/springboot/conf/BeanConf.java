package com.jverson.springboot.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.jverson.springboot.bean.Person;
import com.jverson.springboot.bean.Person2;

@Configuration
@PropertySource("classpath:person.properties")
public class BeanConf {

	@Bean("person")
	@ConfigurationProperties(prefix = "person")
	public Person person(){
		return new Person();
	}
	
	@Bean("person2")
	@ConfigurationProperties(prefix = "person2")
	public Person2 person2() {
		return new Person2();
	}
	
}
