package com.jverson.springboot.controller.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.jverson.springboot.domain.Quote;
import com.jverson.springboot.service.RestTemplateService;

@RestController
@RequestMapping("/api/rest")
public class RestTemplateController {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired 
	private RestTemplateService restTemplateService;
	
	@Autowired 
	private RestTemplate restTemplate;
	
	@RequestMapping
//	@CrossOrigin(origins = "http://initem.m.jd.com", allowCredentials = "true")
	public Object index(HttpServletRequest request, HttpServletResponse response, @CookieValue(value = "sid", required = false) String sid) {
//		response.setHeader("Access-Control-Allow-Origin","http://initem.m.jd.com"); //允许跨域的Origin设置
//		response.setHeader("Access-Control-Allow-Credentials","true"); //允许携带cookie
		logger.info("origin = " + request.getHeader("Origin"));
		logger.info("cookie sid = " + sid);
		return restTemplateService.someRestCall();
	}
	
	@RequestMapping("/post")
	public Object test(@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "body", required = false) String body) {
		
		HttpHeaders headers = new HttpHeaders();
	    MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
	    headers.setContentType(type);
	    headers.add("Accept", MediaType.APPLICATION_JSON.toString());
	    String jsonString = "\"age\":28";
    	HttpEntity<String> formEntity = new HttpEntity<String>(jsonString, headers);
		
		logger.info("sid = " + sid);
		logger.info("body = " + body);
		return restTemplate.postForObject("http://gturnquist-quoters.cfapps.io/api/random", formEntity, Quote.class);
	}
	
}
