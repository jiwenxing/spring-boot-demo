package com.jverson.springboot.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jverson.springboot.service.RestTemplateService;

@RestController
@RequestMapping("/api/rest")
public class RestTemplateController {

	@Autowired 
	private RestTemplateService restTemplateService;
	
	@RequestMapping
	public Object index() {
		return restTemplateService.someRestCall();
	}
	
}
