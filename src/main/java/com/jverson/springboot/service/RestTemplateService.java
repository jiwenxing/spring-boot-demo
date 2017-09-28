package com.jverson.springboot.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.jverson.springboot.domain.Quote;

@Service
public class RestTemplateService {

	/**
	 * 单个类定制RestTemplate
	 */
 /*	private final RestTemplate restTemplate;
	public RestTemplateService(RestTemplateBuilder builder){  //RestTemplateBuilder will be auto-configured
		this.restTemplate = builder.setConnectTimeout(1000).setReadTimeout(1000).build();
	}*/
	
	/**
	 * 按照类型注入
	 */
//	@Autowired RestTemplate restTemplate;
	
	/**
	 * 按照名称注入
	 */
	@Resource(name = "restTemplateB")
	private RestTemplate restTemplate;
	
	public Quote someRestCall(){
		return restTemplate.getForObject("http://gturnquist-quoters.cfapps.io/api/random", Quote.class);
	}
	
}
