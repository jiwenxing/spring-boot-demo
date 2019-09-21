# `@EnableWebMvc` 介绍
---

> 该部分内容参考[Configuring Spring MVC](https://docs.spring.io/spring/docs/3.2.x/spring-framework-reference/html/mvc.html#mvc-config)

在Spring中有两种方式启用并配置SpringMVC，即`the MVC Java config` and `the MVC XML namespace`。

## the MVC Java config

```java
@Configuration
@EnableWebMvc
public class WebConfig {
//...
}
```

## the MVC XML namespace

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:mvc="http://www.springframework.org/schema/mvc"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/mvc
        http://www.springframework.org/schema/mvc/spring-mvc.xsd">

    <mvc:annotation-driven/>

</beans>
```

以上两种方式等效的启用了mvc的默认配置，具体的作用包括不限于：注册了`RequestMappingHandlerMapping`、`RequestMappingHandlerAdapter`及`ExceptionHandlerExceptionResolver`从而可以支持处理注解了`@RequestMapping`及`@ExceptionHandler`的Controller。

其实打开`@EnableWebMvc`的源码会发现它主要是引入了一个配置类`DelegatingWebMvcConfiguration`，在进入这个类发现它继承了`WebMvcConfigurationSupport`， 然后再跟进去会发现它有很多`@Bean`的注解，这就能明白@EnableWebMvc的作用了
![](https://jverson.oss-cn-beijing.aliyuncs.com/201709132142_791.png)

`EnableWebMvc`注解源码
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(DelegatingWebMvcConfiguration.class)
public @interface EnableWebMvc {
}
```

**需要特别注意的是在springboot中，pom中如果依赖了springMVC，则springboot的自动配置会自动启用，无需在配置类中再添加`@EnableWebMvc`注解，并且如果加了还可能导致一些问题出现**
> Don’t use @EnableWebMvc in Spring Boot, just include spring-web as a maven/gradle dependency and it will be autoconfigured
这里参考[Spring Boot, @EnableWebMvc, and Common Use Cases](https://dzone.com/articles/spring-boot-enablewebmvc-and-common-use-cases)

## 自定义配置

如果想在默认配置的基础上添加一些自定义配置，只需要实现`WebMvcConfigurer`接口即可，但是这样就需要实现该接口的所有方法，一种更方便的方法是继承`WebMvcConfigurerAdapter`类（该类实现了WebMvcConfigurer接口，不过实现方法体基本都是空的），并选择性的覆写其中需要的方法即可。这里简单就几个最常用的配置举例如下。

```java
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

	/**
	 * 1. Resources 映射配置
	 * 自定义静态资源静态资源映射目录，也可以在配置文件中设置，很多情况下默认即可
	 * 参考：http://tengj.top/2017/03/30/springboot6/
	 */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }
    
    /**
     * 2. view Controller 配置
     * 对于一些没有后台交互的静态页面，就不用写Controller去映射页面了，直接addViewController即可
     * Use it in static cases when there is no Java controller logic to execute before the view generates the response.
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/index").setViewName("redirect:countries");
    }
    
    /**
     * 3. 跨域配置
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
     * 4. 拦截器配置
     * 添加自定义拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // addPathPatterns 用于添加拦截规则
        // excludePathPatterns 用户排除拦截
        registry.addInterceptor(new MyInterceptor()).addPathPatterns("/**").excludePathPatterns("/cities","/login");
    }
}
```

## 总结

在springboot中只要依赖了mvc的jar包，自动配置会自动引入mvc的默认配置，无需`@EnableWebMvc`注解。如果想自定义一些配置，则创建一个继承`WebMvcConfigurerAdapter`的配置类即可。