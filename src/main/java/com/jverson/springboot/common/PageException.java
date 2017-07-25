package com.jverson.springboot.common;


public class PageException extends Exception {

	private static final long serialVersionUID = 1L;

	private ErrorEnum errorEnum;

	public PageException(ErrorEnum errorEnum) {
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
