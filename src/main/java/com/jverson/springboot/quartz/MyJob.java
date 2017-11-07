package com.jverson.springboot.quartz;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class MyJob implements Job {
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String description = context.getTrigger().getDescription();
		System.out.println("hello quartz. description:" + description + ", current time:" + new Date());
	}
}
