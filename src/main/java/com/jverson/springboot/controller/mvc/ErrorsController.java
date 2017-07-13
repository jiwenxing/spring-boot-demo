package com.jverson.springboot.controller.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class ErrorsController implements ErrorController {

	private static final String PATH = "/error";
	
	@RequestMapping(value = PATH)
	public String errorsHtml(HttpServletRequest request,
			HttpServletResponse response) {
		return "error";
	}

	@Override
	public String getErrorPath() {
		// TODO Auto-generated method stub
		return PATH;
	}

}
