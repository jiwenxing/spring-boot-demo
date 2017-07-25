package com.jverson.springboot.controller.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.jverson.springboot.common.ErrorEnum;
import com.jverson.springboot.common.PageException;
import com.jverson.springboot.common.RestException;

@Controller
public class HelloController {

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
	
}
