package com.jverson.springboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jverson.springboot.common.PageException;
import com.jverson.springboot.domain.Car;
import com.jverson.springboot.mapper.UserMapper;

@Controller
public class HelloController {

	@Autowired UserMapper userMapper;
	
	@RequestMapping("/user")
	@ResponseBody
	public Object mybatisTest() throws PageException{
		return userMapper.selectAll();
	}
	
	@RequestMapping(value = "/getCar", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.APPLICATION_XML_VALUE})
	@ResponseBody
	public Object getCar(@ModelAttribute("hello") String msg) throws PageException{
		System.out.println("从全局Attribute中获取的额外信息：" + msg);
		Car car = new Car();
		car.setBrand("bmw");
		car.setColor("red");
		car.setPrice(33.88);
		return car;
	}
	
}
