package com.jverson.springboot.quartz;

import java.util.Date;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

public class QuartzTestDemo {

	public static void main(String[] args) throws SchedulerException {
		
		// 1. 创建Scheduler的工厂(如果未指定配置文件，默认根据jar包中/org/quartz/quartz.properties文件初始化工厂)
		SchedulerFactory sf = new StdSchedulerFactory(); //StdSchedulerFactory is An implementation of <code>{@link org.quartz.SchedulerFactory}</code> that does all of its work of creating a <code>QuartzScheduler</code> instance based on the contents of a <code>Properties</code> file.
		
		// 2. 从工厂中获取调度器实例
		Scheduler scheduler = sf.getScheduler();
		
		// 3. 创建JobDetail
		JobDetail jobDetail = JobBuilder.newJob(MyJob.class)
				.withDescription("job_desc")
				.withIdentity("job_name", "job_group")
				.build();
		
		// 4. 创建Trigger
		Trigger trigger = TriggerBuilder.newTrigger()
				.withSchedule(CronScheduleBuilder.cronSchedule("0/2 * * * * ?")) //两秒执行一次，可以使用SimpleScheduleBuilder或者CronScheduleBuilder
				.withDescription("tigger_desc")
				.withIdentity("trigger_name", "trigger_group")
				.build();
		
		// 5. 注册任务和定时器
		scheduler.scheduleJob(jobDetail, trigger);
		
		// 6. 启动调度器
		scheduler.start();
		
		System.out.println("启动时间 ： " + new Date());
	}
	
}
