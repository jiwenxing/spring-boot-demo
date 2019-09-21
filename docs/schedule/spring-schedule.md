# Spring Schedule
---


Spring3.0之后, 增加了调度器功能, 提供了`@Schedule`注解。使用起来非常方便，只需要简单的两个注解即可实现复杂的定时任务功能。


## 使用方法

Spring任务调度的实现同时支持注解配置和XML配置两种方式。这里只讲解基于注解的使用方式。

一、 启动Spring定时任务功能

只需要在配置类上添加注解`@EnableScheduling`即可开启Spring的定时任务功能，它的作用类似于xml中配置`<task:annotation-driven/> `。一旦开启Spring的定时任务功能，Spring便会对其管理的Bean自动扫描其中的`@Scheduled`注解的方法（方法不能有入参），将其注册到任务管理器中等待执行。

```java
@SpringBootApplication 
@EnableScheduling  //Enables Spring's scheduled task execution capability
public class HelloSpringBoot {
	//...
}
```

二、 创建任务并使用注解将其标记为定时任务

下例中`reportCurrentTime()`本来就是一个普通的方法，添加`@Scheduled(fixedRate = 5000)`注解后启动服务便能够每个5秒执行一次，非常方便。

这里有几点需要注意：

1. 方法必须在Spring管理的Bean中，例如使用`@Service`或`@Component`注解的类
2. 如果是集群部署每台实例将都会执行一次任务，这个可能并不是想看到的，这里使用了指定IP执行定时任务的方法避免了这个问题。
3. 注解的方法必须是无输入参数并返回空类型void的。
4. `@EnableScheduling`和`@Scheduled`注解都在`spring-context-4.3.9.RELEASE.jar`中，但是不需要特别添加这个依赖，spring boot中自带。

```java
@Service
public class ScheduledTaskService {
	/**
	 * 集群环境下避免任务多次执行： 
	 * 1. 如果在集群环境下可以限制只有某一台ip的容器可以执行定时任务，这种方式的优点是比集成quartz简单，但是存在单点失效的风险 
	 * 2. 使用redis共享一个锁变量（设置expired），网上也有人使用zookeeper共享锁，quartz则使用mysql数据库实现
	 */
	@Value("${batch.exec.host}")
	private String batchExecHost;

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	@Scheduled(fixedRate = 5000) // 通过@Scheduled声明该方法是计划任务，使用fixedRate属性每隔固定时间执行
	public void reportCurrentTime() throws UnknownHostException {
		Thread current = Thread.currentThread();
		String hostAddr = InetAddress.getLocalHost().getHostAddress();
		if (hostAddr.equals(batchExecHost)) {
			System.out.println("task excute @ 5s rate " + dateFormat.format(new Date()) 
			+ ", thread: " + current.getId());
		} else {
			System.out.println("task skip " + dateFormat.format(new Date()) 
			+ ", thread: " + current.getId());
		}
	}

	@Scheduled(cron = "0/2 * * * * ?")
	public void depositJob() {
		Thread current = Thread.currentThread();
		System.out.println("hello scheduled. current time:" + dateFormat.format(new Date()) 
				+ ", thread: " + current.getId());
	}
}
```

执行结果：

> hello scheduled. current time:15:46:26, thread: 26   
hello scheduled. current time:15:46:28, thread: 26   
hello scheduled. current time:15:46:30, thread: 26   
task skip 15:46:30, thread: 26   
hello scheduled. current time:15:46:32, thread: 26   

## 使用多线程

不难发现上面的任务都是单线程执行的，如果想使用多线程并行执行任务则需要配置一个线程池，如下所示：

```java
@Configuration
public class SchedulingConfig implements SchedulingConfigurer {

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setScheduler(taskExecutor());
	}

	@Bean(destroyMethod = "shutdown")
	public Executor taskExecutor() {
		/**
		 * 注意这里创建的Executor是ScheduledThreadPoolExecutor，而它实现了ScheduledExecutorService接口
		 * 因此本质上还是利用concurrent包里的ScheduledExecutorService实现
		 */
		return Executors.newScheduledThreadPool(100);
	}
}
```

> hello scheduled. current time:15:43:22, thread: 27   
hello scheduled. current time:15:43:24, thread: 28   
task skip 15:43:25, thread: 26   
hello scheduled. current time:15:43:26, thread: 27   
hello scheduled. current time:15:43:28, thread: 28   
hello scheduled. current time:15:43:30, thread: 26   
task skip 15:43:30, thread: 46   
hello scheduled. current time:15:43:32, thread: 48  


## References

- [Spring Scheduler实现解析](http://zhwbqd.github.io/2015/01/26/spring-cron.html)
- [SpringBoot Schedule 配置](http://www.cnblogs.com/slimer/p/6222485.html)

