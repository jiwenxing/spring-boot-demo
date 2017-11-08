package com.jverson.springboot.schedule;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Timer类可以调度任务，TimerTask则是通过在run()方法里实现具体任务。 Timer实例可以调度多任务，它是线程安全的。 
 * 当Timer的构造器被调用时，它创建了一个线程，这个线程可以用来调度任务。
 * @author jiwenxing
 * @date Nov 8, 2017 2:25:57 PM
 */
public class TimerTaskDemo {

	public static void main(String[] args) {
		// TimerTask 是实现了Runnable的抽象类
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				System.out.println("timerTask excute every 2 seconds, current time: " + new Date());
			}
		};
		
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(timerTask, 1000, 2000);
	}
	
}
