package com.jverson.springboot.controller.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jverson.springboot.common.PageException;
import com.jverson.springboot.mapper.UserMapper;

@Controller
public class HelloController {

	@Autowired UserMapper userMapper;
	
	@RequestMapping("/user")
	@ResponseBody
	public Object mybatisTest() throws PageException{
		return userMapper.selectAll();
	}
	
}
