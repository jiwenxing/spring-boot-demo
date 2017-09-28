package com.jverson.springboot.service;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.jverson.springboot.domain.Quote;

@Service
public class RestTemplateService {

	private final RestTemplate restTemplate;
	
	public RestTemplateService(RestTemplateBuilder builder){  //RestTemplateBuilder will be auto-configured
		this.restTemplate = builder.build();
	}
	
	public Quote someRestCall(){
		return restTemplate.getForObject("http://gturnquist-quoters.cfapps.io/api/random", Quote.class);
	}
	
}
