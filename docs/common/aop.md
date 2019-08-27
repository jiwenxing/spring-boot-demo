# 利用 AOP 实现接口统一日志打印、异常处理及方法监控
---

一般对外的重要接口会有一些通用功能：

- 打印出参和入参以便调试排查问题
- 方法体一般会嵌套在 try catch 里捕获异常封装错误码（一般对外接口不会直接抛异常出去）
- 对外接口的方法性能监控代码

而这几个功能一般都是一些模板代码，如果每个方法里去都写一遍，代码冗长重复甚至多于真正的业务逻辑代码，显然不够优雅。下面介绍一种 aop 的方式，他可以将以上这些逻辑统统抽取出来在切面中完成，而接口中只需要保留业务代码即可。

## 添加依赖

主要是引入 aspectj，基于 cglib 实现动态代理

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

## RPC 接口在切面类中完成通用逻辑

其中 @Around 中需要注明切面逻辑使用的范围，本例即限定对 `com.jverson.service.component` 包下类进行切面织入。另外注意不需要在 Springboot 中注解 @EnableAspectJAutoProxy，添加了依赖默认会开启


```java
@Aspect
@Component("bizExecptionAndLogHandler") 
public class BizExecptionAndLogHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(BizExecptionAndLogHandler.class);
	
	@Value(value = "${devMode:false}")
	private boolean devMode;

	@Around("execution(* com.jverson.service.component.*.*(..))")
	public String invokeService(ProceedingJoinPoint joinPoint) {
		String className = joinPoint.getTarget().getClass().getName();
		String methodName = joinPoint.getSignature().getName();
		if (devMode) { //开发模式下打印入参
			printRequest(joinPoint, false); 
		}
		Object result = null;
		try {
			result = joinPoint.proceed(); //真正的方法调用，可以在此处添加方法监控代码
		} catch (Throwable e) {
			if (e instanceof BizException) { //异常处理，首先处理自定义的业务异常
				ErrorCodeUgc errorCodeUgc = ((BizException) e).getErrorCodeUgc();
				ResultUgcVo<Object> resultEx = new ResultUgcVo<>(errorCodeUgc, e.getMessage());
				if (errorCodeUgc.equals(SHIELD_CONTENT)) {
					resultEx.setExtMap(((BizException) e).getExtMap());
				}
				result = resultEx;
			}else {  //其它非业务异常统一按照系统异常处理
				result = new ResultUgcVo<>(EXCEPTION_ERROR);
			}
			LOGGER.error(new StringBuilder().append(className).append(".").append(methodName)
					.append(" error!").toString(), e);
			printRequest(joinPoint, true);  //异常时入参出参都打印
			printResult(result, joinPoint, true);
		}
		if (devMode) { //开发模式下打印出参
			printResult(result, joinPoint, false);
		}
		return JSON.toJSONString(result); //接口结果是 String 类型
	}

	// 打印入参
	private void printRequest(ProceedingJoinPoint joinPoint, boolean isError) {
		String className = joinPoint.getTarget().getClass().getName();
		String methodName = joinPoint.getSignature().getName();
		Object[] args = joinPoint.getArgs();
		String params = String.valueOf(args[0]);
		StringBuilder logBegin = new StringBuilder();
		String requestParam = logBegin.append(className).append(".").append(methodName).append("begin, param = ").append(params)
				.toString();
		if (isError) {
			LOGGER.error(requestParam);
		}else {
			LOGGER.info(requestParam);
		}
	}

	// 打印返回值
	private void printResult(Object result, ProceedingJoinPoint joinPoint, boolean isError) {
		String className = joinPoint.getTarget().getClass().getName();
		String methodName = joinPoint.getSignature().getName();
		StringBuilder logEnd = new StringBuilder();
		String resultStr = logEnd.append(className).append(".").append(methodName).append("end, result = ").append(JSON.toJSONString(result))
				.toString();
		if (isError) {
			LOGGER.error(resultStr);
		}else {
			LOGGER.info(resultStr);
		}
	}
	
}
```

## Controller 接口在切面类中完成通用逻辑

如果对外提供的是 web api，即织入逻辑需要加到 controller 里，这时候直接使用 `@ControllerAdvice` 即可，原理一样，是 spring 为 controller 封装的注解，使用更加方便

```java
@ControllerAdvice("komento.admin.web.controller.vue")
public class GlobalExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
	/**
     * 全局异常捕捉处理
     * @param ex
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Object errorHandler(Exception ex) {
    	LOGGER.error("unexpect exception!", ex);
    	ResultUgcVo<Object> result = new ResultUgcVo<Object>(ErrorCodeUgc.EXCEPTION_ERROR);
        return result;
    }
    
    /**
     * 全局捕捉自定义异常
     * @param ex
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = BizException.class)
    public Object bizExceptionHandler(BizException ex) {
    	LOGGER.error("biz exception, error code = {}!", ex.getErrorCodeUgc());
    	ResultUgcVo<Object> result = new ResultUgcVo<Object>(ex.getErrorCodeUgc());
        return result;
    }	
}
```

## 原理

刚开一直担心这样做会不会对接口的性能有损耗，我们知道 aop 是通过动态代理实现的，在 main 方法中添加一下代码查看日志。

```java
public static void main(String[] args) {
	ApplicationContext ctx = new SpringApplicationBuilder().sources(KometoSdkBootApplication.class).run(args);
        String[] activeProfiles = ctx.getEnvironment().getActiveProfiles();
        for (String string : activeProfiles) {
        	LOGGER.warn("the active profile is: " + string);
        }
    Map<String, CommentService> map = ctx.getBeansOfType(CommentService.class);
    map.values().forEach(val -> System.out.println(val.getClass().getName()));
    System.out.println(map.keySet());
}
```

打印输出

> com.jverson.component.CommentServiceJsfImpl$$EnhancerBySpringCGLIB$$ec227a0d
[commentServiceJsfImpl]

可以看到在 spring 在启动的时候会创建动态代理类实例来取代我们在代码中定义的实例，并且类 id 与代码中设置的相同。也就是说其它地方如果注入了此类都将被代理类替代。在代理类内部则是通过反射调用的被代理类，反射就肯定会有性能损耗，不过这个一般情况下可以忽略不计。



## 参考

- [Spring Boot中使用AOP统一处理Web请求日志](http://blog.didispace.com/springbootaoplog/)


