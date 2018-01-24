package com.jverson.springboot.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

//@Service
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
