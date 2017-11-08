package com.jverson.springboot.schedule;

import java.util.Date;

/**
 * 最原始的实现定时任务的方法，
 * 创建一个thread，然后让它在while循环里一直运行着， 通过sleep方法来达到定时任务的效果。这样可以快速简单的实现，
 * @author jiwenxing
 * @date Nov 8, 2017 2:19:40 PM
 */
public class RunnableTaskDemo {

	public static void main(String[] args) {
		final long timeInterval = 2000;  
		
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				while(true){
					System.out.println("thread excute every 2 seconds, current time: " + new Date());
					try {
						Thread.sleep(timeInterval);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
			}
		};
		
		Thread thread = new Thread(runnable);
		thread.start();
	}
	
}
