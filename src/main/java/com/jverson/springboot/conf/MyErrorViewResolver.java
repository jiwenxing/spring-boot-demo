package com.jverson.springboot.conf;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.web.ErrorViewResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

/**
 * Resolve an error view for the specified details
 * @author jiwenxing
 * @date Jul 25, 2017 9:30:22 PM
 */

@Component  //uncomment this to enable
public class MyErrorViewResolver implements ErrorViewResolver {

	public static final String DEFAULT_ERROR_VIEW = "error";
	
	@Override
	public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
		ModelAndView mav = new ModelAndView();
		mav.addObject("status", status.value());
		mav.addObject("message", status.getReasonPhrase());
        mav.addObject("url", request.getRequestURL().toString());
        mav.setViewName(DEFAULT_ERROR_VIEW);
        return mav;
	}

}
