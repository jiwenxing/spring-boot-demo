package com.jverson.springboot.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.jverson.springboot.interceptor.MyInterceptor;


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
	
    /**
     * 对于一些没有后台交互的静态页面，就不用写Controller去映射页面了，直接addViewController即可
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/index").setViewName("redirect:countries");
    }
    
    /**
     * 重写addCorsMappings方法实现跨域的设置
     * 当然跨域还可以通过在Controller或方法上添加‘@CrossOrigin("http://domain2.com")’的注解实现，不过下面这种方便统一管理
     * 参考：https://docs.spring.io/spring/docs/current/spring-framework-reference/html/cors.html
     */
    @Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/cities/**")
			.allowedOrigins("http://jverson.com")
			.allowedMethods("GET", "DELETE")
			.allowCredentials(true).maxAge(3600);
	}
    
    //全局跨域，Enabling CORS for the whole application is as simple as:
    /*@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**");
	}*/
    
    /**
     * 添加自定义拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // addPathPatterns 用于添加拦截规则
        // excludePathPatterns 用户排除拦截
        registry.addInterceptor(new MyInterceptor()).addPathPatterns("/**").excludePathPatterns("/cities","/login");
    }
    
}
