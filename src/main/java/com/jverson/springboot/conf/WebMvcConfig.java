package com.jverson.springboot.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

	/**
	 * 自定义静态资源静态资源映射目录，也可以在配置文件中设置，很多情况下默认即可
	 * 参考：http://tengj.top/2017/03/30/springboot6/
	 */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }
	
}
