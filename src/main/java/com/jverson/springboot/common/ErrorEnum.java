package com.jverson.springboot.common;

public enum ErrorEnum {

	ERROR_SERVER(100, "server error"), ERROR_PARAMETERS(101, "parameters error");
	public Integer code;
	public String message;
	private ErrorEnum(Integer code, String message) {
		this.code = code;
		this.message = message;
	}
	
}
