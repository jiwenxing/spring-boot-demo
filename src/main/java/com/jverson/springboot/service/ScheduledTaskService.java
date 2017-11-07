package com.jverson.springboot.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledTaskService {

	/**
	 * 如果在集群环境下可以限制只有某一台ip的容器可以执行定时任务
	 * 这种方式的有点事比集成quartz简单，但是存在单点失效的风险
	 */
	@Value("${batch.exec.host}")
	private String batchExecHost;
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	
	@Scheduled(fixedRate = 5000) //通过@Scheduled声明该方法是计划任务，使用fixedRate属性每隔固定时间执行
    public void reportCurrentTime() throws UnknownHostException{
		String hostAddr = InetAddress.getLocalHost().getHostAddress();
		if (hostAddr.equals(batchExecHost)) {
			System.out.println("task excute @ 5s rate "+dateFormat.format(new Date()));
		}else {
			System.out.println("task skip "+dateFormat.format(new Date()));
		}
    }
	
}
