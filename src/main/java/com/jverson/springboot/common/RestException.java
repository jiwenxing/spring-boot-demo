package com.jverson.springboot.common;


public class RestException extends Exception {

	private static final long serialVersionUID = 1L;

	private ErrorEnum errorEnum;

	public RestException(ErrorEnum errorEnum) {
		super();
		this.setErrorEnum(errorEnum);
	}

	public ErrorEnum getErrorEnum() {
		return errorEnum;
	}

	public void setErrorEnum(ErrorEnum errorEnum) {
		this.errorEnum = errorEnum;
	}
	
}
