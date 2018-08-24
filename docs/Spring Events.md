
# Spring Events
---

Spring 事件体系是观察者模式的典型应用，观察者模式中有主题(Subject)和观察者(Observer)，它定义了对象之间的一对多的依赖关系，当主题状态发生变化时，所有观察者都会收到通知并且自动更新.

![](http://7xry05.com1.z0.glb.clouddn.com/201801241600_183.png)

<!-- more -->

## Overview

Spring 的事件机制和事件发布是 `ApplicationContext` 本身提供的功能，要实现 Spring Events 需要遵循以下几点：

- 自定义事件必须继承 ApplicationEvent
- 事件发布者需要注入并使用 `ApplicationEventPublisher` 发布事件 
    - 也可以直接使用 `ApplicationContext` 发布事件，因为它继承了 `ApplicationEventPublisher` 接口
- 事件监听器需要实现 `ApplicationListener` 接口
    - 也可以使用注解 `@EventListener` （推荐） 


## Spring Events 实现

Spring Events 虽然在应用代码中不是很常用，但其框架本身大量使用了事件机制，例如 ApplicationStartedEvent、ApplicationEnvironmentPreparedEvent 等。同时 Spring 允许自定义事件，下面就来演示一下如何实现自定义事件。

### 创建自定义事件

以一个信号灯的事件为例通过继承 `ApplicationEvent` 定义一个自定义 Event 如下：

```java
public class LightEvent extends ApplicationEvent {
	private static final long serialVersionUID = 1L;
	private LightColorEnum lightColor;
	public LightEvent(Object source) {
		super(source);
	}
	public LightEvent(Object source, LightColorEnum lightColor) {
		super(source);
		this.setLightColor(lightColor);
	}
	public LightColorEnum getLightColor() {
		return lightColor;
	}
	public void setLightColor(LightColorEnum lightColor) {
		this.lightColor = lightColor;
	}
	public enum LightColorEnum {
		RED("stop"), YELLOW("wait"), GREEN("go");
		
		private String message;
		
		private LightColorEnum(String message){
			this.message = message;
		}
		
		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}
}
```


### 创建事件监听器

方式一: 实现 `ApplicationListener` 接口

```java
@Component
public class LightEventListener implements ApplicationListener<LightEvent> {
	@Override
	public void onApplicationEvent(LightEvent event) {
		try {
			Thread.sleep(10000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(event.getLightColor().getMessage());
	}
}
```

方式二: 使用注解 `@EventListener` 可以将一个普通方法标识为监听器 （推荐），注意 **Listener 需要注解为 Spring 管理的 Bean**

Spring4.2 开始提供了 @EventListener 注解，使得监听器不再需要实现 ApplicationListener 接口，只需要在监听方法上加上该注解即可，方法不一定叫 onApplicationEvent，但有且只能有一个参数，指定监听的事件类型。

```java
@Component
public class LightEventAnnotationListener {
	private static final Logger log = LoggerFactory.getLogger(LightEventAnnotationListener.class);
	@EventListener
	public void handler(LightEvent event){
		try {
			Thread.sleep(2000); //模拟一个比较耗时的操作
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("traffic light：" + event.getLightColor().getMessage());
		log.info(Thread.currentThread().getName()); 
	}
}
```


### 发布事件

在任何 Bean 中都可通过注入 `ApplicationContext` 或 `ApplicationEventPublisher` 来发布事件，这里在一个 rest api 中进行事件发布

```java
@RestController
public class LightEventPublisher {
	private static final Logger log = LoggerFactory.getLogger(LightEventPublisher.class);
	
	/**
	 * 这里注入 ApplicationContext 和 ApplicationEventPublisher 是等价的，后者是一个接口，前者继承了该接口
	 * 也就是说 ApplicationContext 本身提供了发布 event 的能力
	 */

	//@Autowired
	//private ApplicationContext context;
	@Autowired
    private ApplicationEventPublisher applicationEventPublisher;
	
	@RequestMapping("/publish")
	public Object publish(){
		LightEvent lightEvent = new LightEvent("", LightColorEnum.YELLOW);
		applicationEventPublisher.publishEvent(lightEvent);
		log.info("things after publish");
		return "ok";
	}
}
```


### 测试

启动 Spring Boot 应用，访问 `http://127.0.0.1/publish` 可以看到以下结果：

> traffic light：wait     
things after publish  

从以上结果可以看到在请求中发布的自定义事件 `lightEvent` 被我们自己定义的监听器 `LightEventAnnotationListener` 监听到并执行了 `@EventListener` 注解的方法。但同时会发现主线程的 publish 方法是阻塞执行，会等待监听方法执行完后才会继续往下执行。

## 异步事件

> Spring allows to create and publish custom events which – by default – are synchronous. This has a few advantages – such as, for example the listener being able to participate in the publisher’s transaction context.

上边的例子中我们知道 Spring 的时间默认是同步的，这个在例如事务场景中是必要的，但是也有一些场景我们并不关心监听器的执行结果，因此不希望其阻塞主线程从而导致响应变慢，此时就需要实现异步事件。这里可以结合 Spring 对异步方法的支持所提供的注解 `@Async` 和 `@EnableAsync` 来实现。这里涉及到多线程中的知识点，不详细展开讨论。

### 标注监听器方法为异步方法

将上例中 LightEventAnnotationListener 的监听方法添加异步注解

```java
@Component
public class LightEventAnnotationListener {
	private static final Logger log = LoggerFactory.getLogger(LightEventAnnotationListener.class);
	@EventListener
	@Async  //异步方法
	public void handler(LightEvent event){
		try {
			Thread.sleep(2000); //模拟一个比较耗时的操作
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("traffic light：" + event.getLightColor().getMessage());
		log.info(Thread.currentThread().getName()); 
	}
}
```


### 开启 Spring 异步支持

```java
@SpringBootApplication 
@EnableAsync //开启异步支持
public class HelloSpringBoot {
	public static void main(String[] args) throws UnknownHostException {
	    SpringApplication.run(HelloWorld.class, args);
    }
}
```


### 测试

再次测试发现日志输出的顺序变了，说明主线程并没有一直等待耗时2秒的监听器质性完成才继续向下执行，而是在发布完事件后直接就执行后面的任务了。这就说明 listener 的方法实现异步执行了。

> things after publish  
traffic light：wait    


## Existing Framework Events

Spring 本身会发布一系列框架事件，例如 `ContextRefreshedEvent`、`ContextStartedEvent` 及 `RequestHandledEvent ` 等，这些事件提供了一些hook，可以让开发者很方便的将一些逻辑添加到 Spring 容器的生命周期中，例如可以监听容器刷新事件，代码如下：

```java
public class ContextRefreshedListener 
  implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent cse) {
        System.out.println("Handling context re-freshed event. ");
    }
}
```

## Spring Events 原理

Spring Events 从实现原理上讲即所有观察者继承一个包含触发方法的父类并重写该方法，然后注册到被观察者的一个列表中。当被观察者发生变化时通过调用列表中所有已注册观察者的触发方法，使观察者得到通知，从而作进一步处理。详细的源码解析可以参考[Spring的事件与异步事件](https://www.limisky.com/125.html)


## 注意点

1. Spring Events 只能监听同一个 JVM 中的事件
2. 异步事件需要关注线程资源，在大并发的情况下需要自定义线程池
3. 方法只要注解了`@Async`并且开启异步即可实现异步，异步是多线程中的知识点，而event是一种设计模式（观察者模式或发布订阅模式）


## 参考

- [Spring Events](http://www.baeldung.com/spring-events)
- [Spring的事件与异步事件](https://www.limisky.com/125.html)
- [Application events with Spring](http://zoltanaltfatter.com/2016/05/11/application-events-with-spring/)
- [Spring5源码解析-Spring框架中的事件和监听器](https://muyinchen.github.io/2017/09/27/Spring5%E6%BA%90%E7%A0%81%E8%A7%A3%E6%9E%90-Spring%E6%A1%86%E6%9E%B6%E4%B8%AD%E7%9A%84%E4%BA%8B%E4%BB%B6%E5%92%8C%E7%9B%91%E5%90%AC%E5%99%A8/)


