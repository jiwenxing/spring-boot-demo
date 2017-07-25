package com.jverson.springboot.conf;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.jverson.springboot.common.CustomerErrorType;
import com.jverson.springboot.common.RestException;
import com.jverson.springboot.controller.mvc.HelloController;

/*@ControllerAdvice(basePackageClasses = HelloController.class)
public class FooControllerAdvice extends ResponseEntityExceptionHandler {

	@ExceptionHandler(RestException.class)
	@ResponseBody
	ResponseEntity<?> handleControllerException(HttpServletRequest request, RestException rex) {
		HttpStatus status = getStatus(request);
		return new ResponseEntity<>(new CustomerErrorType(rex.getErrorEnum().code, rex.getErrorEnum().message), status);
	}
	private HttpStatus getStatus(HttpServletRequest request) {
		Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
		if (statusCode == null) {
		return HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return HttpStatus.valueOf(statusCode);
	}
}*/
