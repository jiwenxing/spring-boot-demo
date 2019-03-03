# Quartz + Spring Boot
---

最近使用 spring boot、 quartz、H2 以及 RabbitMQ 等实现了一个动态的任务管理系统，可以动态的进行任务的创建、修改、暂停、运行以及删除操作，并且使用了 RabbitMQ 消息队列实现了定时任务系统与具体业务系统的解耦，再也不需要每次加个定时任务都上线一次了。

源码：https://github.com/jiwenxing/springboot-quartz

预览：    
![](https://jverson.oss-cn-beijing.aliyuncs.com/206751c9ac95c7860f087a02e5f2fd9f.jpg)


这里也将常规的 Quartz 与 Spring 的整合过程记录如下。

## 集成步骤

### 添加依赖

```xml
<!-- Includes spring's support classes for quartz -->
<dependency>
	<groupId>org.springframework</groupId>
	<artifactId>spring-context-support</artifactId>
</dependency>
<dependency>
	<groupId>org.quartz-scheduler</groupId>
	<artifactId>quartz</artifactId>
	<version>2.2.1</version>
</dependency>
```

### 创建 job

创建一个 Job 类，该类需要继承 QuartzJobBean 或者实现 Job 方法

```java
public class SampleJob implements Job {
	@Autowired
	private SampleService sampleService;
	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    	// execute task inner quartz system
    	// spring bean can be @Autowired
    	sampleService.hello(jobName);
	}

}
```

### 配置 jobDetail、jobTrigger 及 schedule 实例

```java
@Bean
public JobDetailFactoryBean sampleJobDetail() {
    return createJobDetail(SampleJob.class);
}

@Bean(name = "sampleJobTrigger")
public SimpleTriggerFactoryBean sampleJobTrigger(@Qualifier("sampleJobDetail") JobDetail jobDetail,
                                                 @Value("${samplejob.frequency}") long frequency) {
    return createTrigger(jobDetail, frequency);
}

@Bean
public Scheduler schedulerFactoryBean(DataSource dataSource, JobFactory jobFactory,
                                      @Qualifier("sampleJobTrigger") Trigger sampleJobTrigger) throws Exception {
    SchedulerFactoryBean factory = new SchedulerFactoryBean();
    // this allows to update triggers in DB when updating settings in config file:
    factory.setOverwriteExistingJobs(true);
    factory.setDataSource(dataSource);
    factory.setJobFactory(jobFactory);

    factory.setQuartzProperties(quartzProperties());
    factory.afterPropertiesSet();

    Scheduler scheduler = factory.getScheduler();
    scheduler.setJobFactory(jobFactory);
    scheduler.scheduleJob((JobDetail) sampleJobTrigger.getJobDataMap().get("jobDetail"), sampleJobTrigger);

    scheduler.start();
    return scheduler;
}
```


## 注意事项

### 引入 spring-context-support 依赖（非必须）

在使用 Spring 集成 Quartz 的时候，一定不要忘记引入 spring-context-support 这个包，该依赖包含支持UI模版（Velocity，FreeMarker，JasperReports），邮件服务，脚本服务(JRuby)，缓存Cache（EHCache），任务计划Scheduling（uartz）等方面的类。

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context-support</artifactId>
    <version>4.2.6.RELEASE</version>
</dependency>
```

### job 中无法注入业务类

Job 类中，注入 XXService 的时候，会报空指针异常，因为 Job 对象是 Quartz 通过反射创建的，而业务 service 是通过 Spring 创建的，所以在 Job 类中使用由 Spring 管理的对象就会报空指针异常。

Quartz 中有一个 JobFactory 接口，负责生成 Job 类的实例。那我们是不是可以通过自定义实现这个接口在创建 Job 的时候给予依赖注入的特性，实现如下所示，最后在 SchedulerFactoryBean 中设置新的 JobFactory 即可。

```java
public final class SpringJobFactory extends SpringBeanJobFactory implements
        ApplicationContextAware {

    private transient AutowireCapableBeanFactory beanFactory;

    @Override
    public void setApplicationContext(final ApplicationContext context) {
        beanFactory = context.getAutowireCapableBeanFactory();
    }

    @Override
    protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
        final Object job = super.createJobInstance(bundle);
        beanFactory.autowireBean(job);
        return job;
    }
}

@Configuration
public class SchedulerConfig {
    @Autowired
    private SpringJobFactory springJobFactory;
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setJobFactory(springJobFactory);
        return schedulerFactoryBean;
    }
}
```

## References

- [https://github.com/davidkiss/spring-boot-quartz-demo](https://github.com/davidkiss/spring-boot-quartz-demo)
- [quartz与spring实现任务动态管理](http://lixuguang.iteye.com/blog/2256478)
- [Spring 3整合Quartz 2实现定时任务](https://www.dexcoder.com/selfly/article/308)
- [Spring 中使用 Quartz 时，Job 无法注入spring bean的问题](https://blog.seveniu.com/post/spring-quartz-autowired/)
