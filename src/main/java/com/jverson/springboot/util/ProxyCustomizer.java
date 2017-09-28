package com.jverson.springboot.util;

import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 全局定制RestTemplate
 * To make an application-wide restTemplate customation, additive customization a RestTemplateCustomizer bean can be used. All such beans are automatically registered with the auto-configured RestTemplateBuilder and will be applied to any templates that are built with it.
 * @author jiwenxing
 * @date Sep 28, 2017 5:45:39 PM
 */

@Component
public class ProxyCustomizer implements RestTemplateCustomizer {

	@Override
	public void customize(RestTemplate restTemplate) {
		/*new RestTemplateBuilder()  
        .detectRequestFactory(false)  
        .basicAuthorization("username", "password")  
        .uriTemplateHandler((UriTemplateHandler) new OkHttp3ClientHttpRequestFactory())  
        .configure(restTemplate); */
	}
	
}

