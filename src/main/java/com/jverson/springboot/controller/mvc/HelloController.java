package com.jverson.springboot.controller.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jverson.springboot.common.ErrorEnum;
import com.jverson.springboot.common.PageException;
import com.jverson.springboot.common.RestException;
import com.jverson.springboot.mapper.UserMapper;

@Controller
public class HelloController {

	/*@Autowired UserMapper userMapper;
	
	@RequestMapping(value="hello.html", method={RequestMethod.GET, RequestMethod.POST}, produces = { "text/html;charset=UTF-8" })
	public String Hello(Model model){
		model.addAttribute("name", "jverson");
		return "welcome";
	}
	
	@RequestMapping("/json")
	public String jsonException() throws RestException{
		throw new RestException(ErrorEnum.ERROR_PARAMETERS);
	}
	
	@RequestMapping("/page")
	public String pageException() throws PageException{
		throw new PageException(ErrorEnum.ERROR_SERVER);
	}
	
	@RequestMapping("/user")
	@ResponseBody
	public Object mybatisTest() throws PageException{
		return userMapper.findByName("jverson");
	}*/
	
}
