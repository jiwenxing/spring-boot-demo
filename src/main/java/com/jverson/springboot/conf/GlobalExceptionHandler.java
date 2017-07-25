package com.jverson.springboot.conf;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.jverson.springboot.common.ResultWrap;
import com.jverson.springboot.common.PageException;
import com.jverson.springboot.common.RestException;


@ControllerAdvice
public class GlobalExceptionHandler {

	public static final String DEFAULT_ERROR_VIEW = "error";
	
	@ExceptionHandler(value = RestException.class)
    @ResponseBody
    public ResultWrap<String> defaultErrorJsonHandler(HttpServletRequest req, RestException e) throws Exception {
    	ResultWrap<String> errorInfo = new ResultWrap<String>();
    	errorInfo.setMessage(e.getErrorEnum().message);
    	errorInfo.setCode(e.getErrorEnum().code);
    	errorInfo.setDate("something is wrong!");
    	errorInfo.setUrl(req.getRequestURL().toString());
        return errorInfo;
    }
	
	@ExceptionHandler(value = PageException.class)
    public ModelAndView defaultErrorPageHandler(HttpServletRequest req, PageException e) throws Exception {
        ModelAndView mav = new ModelAndView();
        mav.addObject("code", e.getErrorEnum().code);
        mav.addObject("message", e.getErrorEnum().message);
        mav.addObject("url", req.getRequestURL().toString());
        mav.setViewName(DEFAULT_ERROR_VIEW);
        return mav;
    }
	
}
