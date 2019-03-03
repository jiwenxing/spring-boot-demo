# Error Handling
---

对于异常和错误我们一般希望给用户展示一个比较友好的页面，或者给调用者一个格式化得错误信息，springboot对于异常会有一个默认的`‘whitelabel’ error view`页面，对于http rest客户端也会返回一个标准的json数据。现在我们要做的是定制自己的错误页和错误信息格式。

## 单个静态错误页

如果需求很简单，不需要针对不同的异常展示不同的页面，所有的异常和错误都对应到某一个定制的静态错误页，只需要将静态页放在特定的目录即可（src/main/resources/templates），springboot便会自动映射到该页面，这时访问一个不存在的链接便会返回这个错误页。值得注意的是有一些错误属性是可以在模板文件中直接使用的，比如status、message、path等。

![](https://jverson.oss-cn-beijing.aliyuncs.com/201707251744_27.png)

## 单个动态错误页

可以通过实现`ErrorViewResolver`接口向error页面添加一些动态数据

```java
@Component
public class MyErrorViewResolver implements ErrorViewResolver {
	public static final String DEFAULT_ERROR_VIEW = "error";
	@Override
	public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
		ModelAndView mav = new ModelAndView();
		mav.addObject("status", status.value());
        mav.addObject("message", status.getReasonPhrase());
        mav.setViewName(DEFAULT_ERROR_VIEW);
        return mav;
	}
}
```

error.ftl代码如下：

```html
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <title>统一异常处理</title>
  <body>
    <h1>Error Handler</h1>
    <div>status: ${status}</div>
    <div>message: ${message}</div>
    <div>url: ${url}</div>
  </body>
</html>
```

这时启动随意访问一个不存在的网址便会看到

![](https://jverson.oss-cn-beijing.aliyuncs.com/201707252124_957.png)

## 根据不同的情况返回不同的错误页

在配置类中添加以下代码，可以将不同的错误映射到不同的地址，而这些映射并不需要专门的controller去处理，springboot会自动从特定目录（src/main/resources/static）寻找这些页面。

这个原理其实就是对内置Servlet容器进行配置，之前使用的外部容器配置的web.xml作用一样。如果Spring MVC在处理过程抛出异常到Servlet容器，容器会定向到指定的错误页面（错误页的默认值就是`/error`）。

```java
/**
 * Roughly equivalent to the error-page element traditionally found in web.xml
 */
@Bean
public EmbeddedServletContainerCustomizer containerCustomizer() {
   return (container -> {
        ErrorPage error401Page = new ErrorPage(HttpStatus.UNAUTHORIZED, "/401.html");
        ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/404.html");
        ErrorPage error500Page = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/500.html");

        container.addErrorPages(error401Page, error404Page, error500Page);
   });
}
```

![](https://jverson.oss-cn-beijing.aliyuncs.com/201707252137_848.png)

## 针对不同的异常（或controller）返回不同的json数据

这是通过`@ControllerAdvice`来实现的，定义如下一个类（注意可以不用继承ResponseEntityExceptionHandler），其中`basePackageClasses`和`ExceptionHandler`指定了该类会对指定的controller和指定的exception进行处理，By default the methods in an @ControllerAdvice apply globally to all Controllers。

```java
@ControllerAdvice(basePackageClasses = HelloController.class)
public class FooControllerAdvice extends ResponseEntityExceptionHandler {
	
	@ExceptionHandler(RestException.class)
	@ResponseBody
	ResponseEntity<?> handleControllerException(HttpServletRequest request, RestException rex) {
		HttpStatus status = getStatus(request);
		return new ResponseEntity<>(new CustomerErrorType(rex.getErrorEnum().code, rex.getErrorEnum().message), status);
	}

	private HttpStatus getStatus(HttpServletRequest request) {
		Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
		if (statusCode == null) {
		return HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return HttpStatus.valueOf(statusCode);
	}
}
```

这里便可以自定义`CustomerErrorType`，例如

```java
public class CustomerErrorType {
	private Integer code;
	private String message;
	public CustomerErrorType(Integer code, String message) {
		this.code = code;
		this.message = message;
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
}
```

自定义异常类，

```java
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
	public enum ErrorEnum{
		ERROR_SERVER(100, "server error"), ERROR_PARAMETERS(101, "parameters error");
		public Integer code;
		public String message;
		private ErrorEnum(Integer code, String message) {
			this.code = code;
			this.message = message;
		}
	}
}
```

这时再访问如下Controller方法

```java
@RequestMapping("/json")
	public String jsonException() throws RestException{
		throw new RestException(ErrorEnum.ERROR_PARAMETERS);
}
```

返回结果如下：

```json
{
"code": 101,
"message": "parameters error"
}
```

## 在同一个类中处理页面异常和rest请求异常

还是使用@ControllerAdvice类似于上面提到的方法，将page和rest请求异常处理放在同一个类里如下

```java
@ControllerAdvice
public class GlobalExceptionHandler {

	public static final String DEFAULT_ERROR_VIEW = "error";
	
	@ExceptionHandler(value = RestException.class)
    @ResponseBody
    public ResultWrap<String> defaultErrorJsonHandler(HttpServletRequest req, RestException e) throws Exception {
    	ResultWrap<String> errorInfo = new ResultWrap<String>();
    	errorInfo.setMessage(e.getErrorEnum().message);
    	errorInfo.setCode(e.getErrorEnum().code);
    	errorInfo.setDate("something is wrong!");
    	errorInfo.setUrl(req.getRequestURL().toString());
        return errorInfo;
    }
	
	@ExceptionHandler(value = PageException.class)
    public ModelAndView defaultErrorPageHandler(HttpServletRequest req, PageException e) throws Exception {
        ModelAndView mav = new ModelAndView();
        mav.addObject("code", e.getErrorEnum().code);
        mav.addObject("message", e.getErrorEnum().message);
        mav.addObject("url", req.getRequestURL().toString());
        mav.setViewName(DEFAULT_ERROR_VIEW);
        return mav;
    }
	
}
```

需要自定义业务异常，分别在page和rest请求中抛出不同的异常类型即可。当然容器中的异常还是需要通过上面提到的`EmbeddedServletContainerCustomizer`或重写`resolveErrorView`的方式实现。



## 参考

- [Error Handling](http://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/reference/htmlsingle/#boot-features-error-handling)
- [CUSTOM ERROR PAGES WITH SPRING BOOT](https://www.sporcic.org/2014/05/custom-error-pages-with-spring-boot/)
- [Spring Boot异常处理详解(原理)](http://www.open-open.com/lib/view/open1446607376779.html)
- [Spring Boot中Web应用的统一异常处理](http://blog.didispace.com/springbootexception/)
