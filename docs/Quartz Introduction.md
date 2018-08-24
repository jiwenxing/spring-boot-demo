# Quartz Introduction
---

Quartz是一个完全由java编写的开源作业调度框架。如果有这样一个需求：在某一个有规律的时间点干某件事。并且时间的触发的条件可以非常复杂（比如每月最后一个工作日的17:50），复杂到需要一个专门的框架来干这个事。 Quartz就是来干这样的事，你给它一个触发条件的定义，它负责到了时间点，触发相应的Job起来干活。

> Quartz is a richly featured, open source job scheduling library that can be integrated within virtually any Java application - from the smallest stand-alone application to the largest e-commerce system. Quartz can be used to create simple or complex schedules for executing tens, hundreds, or even tens-of-thousands of jobs; jobs whose tasks are defined as standard Java components that may execute virtually anything you may program them to do. The Quartz Scheduler includes many enterprise-class features, such as support for JTA transactions and clustering.


## Quartz体系结构

Quartz对任务调度的领域问题进行了高度的抽象，提出了调度器、作业任务和触发器这3个核心的概念，并在org.quartz通过接口和类对重要的这些核心概念进行描述：

**Job**

是一个接口，只有一个方法void execute(JobExecutionContext context)，开发者实现该接口定义运行任务，JobExecutionContext类提供了调度上下文的各种信息。Job运行时的信息保存在 JobDataMap实例中；

**JobDetail**

Quartz在每次执行Job时，都重新创建一个Job实例，所以它不直接接受一个Job的实例，相反它接收一个Job实现 类，以便运行时通过newInstance()的反射机制实例化Job。因此需要通过一个类来描述Job的实现类及其它相关的静态信息，如Job名字、描 述、关联监听器等信息，JobDetail承担了这一角色。通过该类的构造函数可以更具体地了解它的功用：JobDetail(java.lang.String name, java.lang.String group,java.lang.Class jobClass)，该构造函数要求指定Job的实现类，以及任务在Scheduler中的组名和Job名称；

**Scheduler**

调度器，代表一个Quartz的独立运行容器，Trigger和JobDetail可以注册到Scheduler中，两者在Scheduler中拥有各自的组及名称，组及名称是Scheduler查找定位容器中某一对象的依据，Trigger的组及名称必须唯一，JobDetail的组和名称也必须唯一（但可以和Trigger的组和名称相同，因为它们是不同类型的）。Scheduler定义了多个接口方法，允许外部通过组及名称访问和控制容器中Trigger和JobDetail。Scheduler可以将Trigger绑定到某一JobDetail中，这样当Trigger触发时，对应的Job就被执行。一个Job可以对应多个Trigger，但一个Trigger只能对应一个Job。可以通过SchedulerFactory创建一个Scheduler实例。Scheduler拥有一个SchedulerContext，它类似于ServletContext，保存着Scheduler上下文信息，Job和Trigger都可以访问SchedulerContext内的信息。SchedulerContext内部通过一个Map，以键值对的方式维护这些上下文数据，SchedulerContext为保存和获取数据提供了多个put()和getXxx()的方法。可以通过Scheduler# getContext()获取对应的SchedulerContext实例。

调度器是Quartz框架的核心。调度器负责管理Quartz应用运行时环境。调度器不是靠自己做所有的工作，而是依赖框架内一些非常重要的部件。Quartz不仅仅是线程和线程管理。为确保可伸缩性，Quartz采用了基于多线程的架构。启动时，框架初始化一套worker线程，这套线程被调度器用来执行预定的作业。这就是Quartz怎样能并发运行多个作业的原理。Quartz依赖一套松耦合的线程池管理部件来管理线程环境。

**Trigger**

是一个类，描述触发Job执行的时间触发规则。Quartz 中五种类型的Trigger：SimpleTrigger，CronTirgger，DateIntervalTrigger，NthIncludedDayTrigger和Calendar 类（ org.quartz.Calendar）。其中最常用的是SimpleTrigger和CronTrigger这两个子类。当仅需触发一次或者以固定时间间隔周期执行，SimpleTrigger是最适合的选择；而CronTrigger则可以通过Cron表达式定义出各种复杂时间规 则的调度方案：如每早晨9:00执行，周一、周三、周五下午5:00执行等； 

**ThreadPool**

Scheduler使用一个线程池作为任务运行的基础设施，任务通过共享线程池中的线程提高运行效率。


Quartz主要类和接口之间的关系如下：

![](http://7xry05.com1.z0.glb.clouddn.com/201711071513_476.png)


## 代码示例

下面是一个quartz的简单示例demo，当然quartz还可以集成到springboot的自动配置，也可以利用数据库进行集群部署，这些都会在后面的文章中讲解。

添加依赖：

```xml
<dependency>
	<groupId>org.quartz-scheduler</groupId>
	<artifactId>quartz</artifactId>
	<version>2.2.1</version>
</dependency>
```

Java代码：

```java
public class QuartzTestDemo {
	public static void main(String[] args) throws SchedulerException {
		
		// 1. 创建Scheduler的工厂(如果未指定配置文件，默认根据jar包中/org/quartz/quartz.properties文件初始化工厂)
		SchedulerFactory sf = new StdSchedulerFactory(); //StdSchedulerFactory is An implementation of <code>{@link org.quartz.SchedulerFactory}</code> that does all of its work of creating a <code>QuartzScheduler</code> instance based on the contents of a <code>Properties</code> file.
		
		// 2. 从工厂中获取调度器实例
		Scheduler scheduler = sf.getScheduler();
		
		// 1、2 两步可以简写为一步完成，内部实现相同 
//		Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
		
		// 3. 创建JobDetail，此处传入自定义的Job类MyJob
		JobDetail jobDetail = JobBuilder.newJob(MyJob.class)
				.withDescription("job_desc")
				.withIdentity("job_name", "job_group")
				.build();
		
		// 4. 创建Trigger
		// 4.1 Trigger the job with CronTrigger, and then repeat every 3 seconds
		Trigger trigger1 = TriggerBuilder.newTrigger()
				.withSchedule(CronScheduleBuilder.cronSchedule("0/2 * * * * ?")) //两秒执行一次，可以使用SimpleScheduleBuilder或者CronScheduleBuilder
				.withDescription("cronTrigger_tigger_desc")
				.withIdentity("trigger_name", "trigger_group")
				.startNow()
				.build();
		
		// 4.2 Trigger the job with SimpleTrigger, and then repeat every 3 seconds
		Trigger trigger2 = TriggerBuilder.newTrigger()
		        .withIdentity("trigger1", "group1")
		        .withDescription("simpleTrigger_tigger_desc")
		        .startNow()
		        .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(3))
		        .build();
		
		
		// 5. 注册任务和定时器
		scheduler.scheduleJob(jobDetail, trigger2);
		
		// 6. 启动调度器
		scheduler.start();
		
		System.out.println("启动时间 ： " + new Date());
	}
}
```

其中MyJob类的代码如下：

```java
public class MyJob implements Job {
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String description = context.getTrigger().getDescription();
		System.out.println("hello quartz. description:" + description + ", current time:" + new Date());
	}
}
```

这里需要注意，execute 方法接受一个 JobExecutionContext 对象作为参数。这个对象提供了作业实例的运行时上下文。特别地，它**提供了对调度器和触发器的访问**，这两者协作来启动作业以及作业的 JobDetail 对象的执行。Quartz通过把作业的状态放在JobDetail对象中并让JobDetail构造函数启动一个作业的实例，分离了作业的执行和作业周围的状态。JobDetail对象储存作业的侦听器、群组、数据映射、描述以及作业的其他属性。

## Cron表达式举例
 
> "30 * * * * ?" 每半分钟触发任务  
"30 10 * * * ?" 每小时的10分30秒触发任务  
"30 10 1 * * ?" 每天1点10分30秒触发任务  
"30 10 1 20 * ?" 每月20号1点10分30秒触发任务  
"30 10 1 20 10 ? *" 每年10月20号1点10分30秒触发任务  
"30 10 1 20 10 ? 2011" 2011年10月20号1点10分30秒触发任务  
"30 10 1 ? 10 * 2011" 2011年10月每天1点10分30秒触发任务  
"30 10 1 ? 10 SUN 2011" 2011年10月每周日1点10分30秒触发任务  
"15,30,45 * * * * ?" 每15秒，30秒，45秒时触发任务  
"15-45 * * * * ?" 15到45秒内，每秒都触发任务  
"15/5 * * * * ?" 每分钟的每15秒开始触发，每隔5秒触发一次  
"15-30/5 * * * * ?" 每分钟的15秒到30秒之间开始触发，每隔5秒触发一次  
"0 0/3 * * * ?" 每小时的第0分0秒开始，每三分钟触发一次  
"0 15 10 ? * MON-FRI" 星期一到星期五的10点15分0秒触发任务  
"0 15 10 L * ?" 每个月最后一天的10点15分0秒触发任务  
"0 15 10 LW * ?" 每个月最后一个工作日的10点15分0秒触发任务  
"0 15 10 ? * 5L" 每个月最后一个星期四的10点15分0秒触发任务  
"0 15 10 ? * 5#3" 每个月第三周的星期四的10点15分0秒触发任务  


## 调度任务信息存储

在默认情况下Quartz将任务调度的运行信息保存在内存中，这种方法提供了最佳的性能，因为内存中数据访问最快。不足之处是缺乏数据的持久性，当程序路途停止或系统崩溃时，所有运行的信息都会丢失。如果业务确实需要持久化任务调度信息，Quartz允许你通过调整其属性文件，将这些信息保存到数据库中。使用数据库保存任务调度信息后，即使系统崩溃后重新启动，任务的调度信息将得到恢复。持久化的任务调度将在quartz集群环境实现中具体讲解。

## References

- [Quartz Best Practices](http://www.quartz-scheduler.org/documentation/best-practices.html)
- [Quartz 学习](http://blog.csdn.net/u010648555/article/details/54863144)
- [Quartz Job Scheduler Tutorials](http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/)
- [Quartz Job Scheduler Cookbook](http://www.quartz-scheduler.org/documentation/quartz-2.x/cookbook/)