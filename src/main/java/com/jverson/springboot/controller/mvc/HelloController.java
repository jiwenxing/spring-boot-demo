package com.jverson.springboot.controller.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class HelloController {

	@RequestMapping(value="hello.html", method={RequestMethod.GET, RequestMethod.POST}, produces = { "text/html;charset=UTF-8" })
	public String Hello(Model model){
		model.addAttribute("name", "jverson");
		return "welcome";
	}
	
}
