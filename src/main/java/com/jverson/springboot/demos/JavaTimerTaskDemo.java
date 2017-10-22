package com.jverson.springboot.demos;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Timer 是一种定时器工具，而 TimerTask 是一个抽象类，它的子类代表一个可以被 Timer 计划的任务。
 * Timer 控制任务以何种方式运行（固定时间、固定频率、固定时延）， TimerTask 则是具体要执行的任务。
 * @author jiwenxing
 * @date Oct 21, 2017 3:26:57 PM
 */

public class JavaTimerTaskDemo extends TimerTask{

	private String jobName = "";
	
	public JavaTimerTaskDemo(String jobName) { 
		super(); 
		this.jobName = jobName; 
	} 
	
	@Override
	public void run() {
		System.out.println("execute: " + jobName);
	}

	public static void main(String[] args) {
		// Timer 类是线程安全的，下面多个线程可以共享单个 Timer 对象而无需进行外部同步
		Timer timer = new Timer();
		
		long delay = 5 * 1000;
		long period = 1 * 1000;
		System.out.println("timer begin...");
		
		// delay 时间后执行 job1
		timer.schedule(new JavaTimerTaskDemo("job1 execute after fixed delay."), delay);
		
		// 指定时间执行 job2（注意大于该时间时启动任务会立即执行）
		Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 15);
        calendar.set(Calendar.MINUTE, 19);
        calendar.set(Calendar.SECOND, 00);
        Date time = calendar.getTime();
		timer.schedule(new JavaTimerTaskDemo("job2 execute at set time."), time);
		
		// 安排指定的任务job3在指定的时间开始进行重复的固定延迟执行（注意大于该时间时启动任务会立即执行）
		// 同理 scheduleAtFixedRate(TimerTask task, Date firstTime, long period)
		Calendar calendar1 = Calendar.getInstance();
		calendar1.set(Calendar.HOUR_OF_DAY, 17);
		calendar1.set(Calendar.MINUTE, 59);
		calendar1.set(Calendar.SECOND, 00);
        Date time1 = calendar1.getTime();
        timer.schedule(new JavaTimerTaskDemo("job3 execute at set time."), time1, period); 
        // 等同于下面的 scheduleAtFixedRate
//		timer.scheduleAtFixedRate(new JavaTimerTaskDemo("job3 execute at set time."), time1, period);
		
		// 在延迟指定时间后以指定的间隔时间循环执行定时任务
        // 同理scheduleAtFixedRate(TimerTask task, long delay, long period)
		timer.schedule(new JavaTimerTaskDemo("job4 execute at fixed rate after fixed delay."), delay, period);
		// 等同于下面的 scheduleAtFixedRate
//		timer.scheduleAtFixedRate(new JavaTimerTaskDemo("job4 execute at fixed rate after fixed delay."), delay, period);
		
		
	}
	
}
