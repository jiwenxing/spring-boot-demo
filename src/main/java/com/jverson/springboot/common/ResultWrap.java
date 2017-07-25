package com.jverson.springboot.common;

public class ResultWrap<T> {

	public static final Integer STATUS_OK = 0;
	
	private Integer code;
	private String message;
	private String url;
	private T date;
	
	
	public ResultWrap(Integer code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public ResultWrap() {
	}

	public Integer getCode() {
		return code;
	}
	public void setCode(Integer code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public T getDate() {
		return date;
	}
	public void setDate(T date) {
		this.date = date;
	}
	
}
