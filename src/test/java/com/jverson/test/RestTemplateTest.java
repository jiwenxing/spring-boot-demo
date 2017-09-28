package com.jverson.test;

import org.springframework.web.client.RestTemplate;

import com.jverson.springboot.domain.Quote;

public class RestTemplateTest {

	public static void main(String[] args) {
		RestTemplate restT = new RestTemplate();
		Quote quote = restT.getForObject("http://gturnquist-quoters.cfapps.io/api/random", Quote.class);
		String quoteString = restT.getForObject("http://gturnquist-quoters.cfapps.io/api/random", String.class);
		System.out.println(quoteString);
	}
	
}
