package com.jverson.springboot.conf;

import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ErrorPage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;

import com.jverson.springboot.domain.Person;
import com.jverson.springboot.domain.Person2;

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
	
	/**
	 * Roughly equivalent to the error-page element traditionally found in web.xml
	 */
//	@Bean   //uncomment this to enable
	public EmbeddedServletContainerCustomizer containerCustomizer() {
	   return (container -> {
	        ErrorPage error401Page = new ErrorPage(HttpStatus.UNAUTHORIZED, "/401.html");
	        ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/404.html");
	        ErrorPage error500Page = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/500.html");

	        container.addErrorPages(error401Page, error404Page, error500Page);
	   });
	}
	
}
