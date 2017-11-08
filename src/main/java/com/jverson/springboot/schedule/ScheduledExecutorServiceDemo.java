package com.jverson.springboot.schedule;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ScheduledExecutorService是从Java SE5的java.util.concurrent里，做为并发工具类被引进的，这是最理想的定时任务实现方式。  
 * 相比于上两个方法，它有以下好处： 
 * 1. 相比于Timer的单线程，它是通过线程池的方式来执行任务的  
 * 2. 可以很灵活的去设定第一次执行任务delay时间 
 * 3. 提供了良好的约定，以便设定执行的时间间隔 
 * @author jiwenxing
 * @date Nov 8, 2017 2:32:05 PM
 */
public class ScheduledExecutorServiceDemo {

	public static void main(String[] args) {

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				System.out.println("ScheduledExecutorService excute every 2 seconds, current time: " + new Date());
			}
		};
		
		ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		scheduledExecutorService.scheduleAtFixedRate(runnable, 1, 2, TimeUnit.SECONDS);
	}
	
}
