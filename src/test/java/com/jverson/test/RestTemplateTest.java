package com.jverson.test;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.jverson.springboot.domain.Quote;

public class RestTemplateTest {

	public static void main(String[] args) {
		/**
		 * RestTemplate默认构造方法不使用连接池，如果想使用则需要一个`ClientHttpRequestFactory`接口的实现类来池化连接。例如使用`HttpComponentsClientHttpRequestFactory`。
		 */
//		RestTemplate restT = new RestTemplate();
		RestTemplate restT = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
		
		Quote quote = restT.getForObject("http://gturnquist-quoters.cfapps.io/api/random", Quote.class);
		String quoteString = restT.getForObject("http://gturnquist-quoters.cfapps.io/api/random", String.class);
		System.out.println(quote);
		System.out.println(quoteString);
	}
	
}
