package com.jverson.springboot.controller.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jverson.springboot.service.RestTemplateService;

@RestController
@RequestMapping("/api/rest")
public class RestTemplateController {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired 
	private RestTemplateService restTemplateService;
	
	@RequestMapping
//	@CrossOrigin(origins = "http://initem.m.jd.com", allowCredentials = "true")
	public Object index(HttpServletRequest request, HttpServletResponse response, @CookieValue(value = "sid", required = false) String sid) {
//		response.setHeader("Access-Control-Allow-Origin","http://initem.m.jd.com"); //允许跨域的Origin设置
//		response.setHeader("Access-Control-Allow-Credentials","true"); //允许携带cookie
		logger.info("origin = " + request.getHeader("Origin"));
		logger.info("cookie sid = " + sid);
		return restTemplateService.someRestCall();
	}
	
}
