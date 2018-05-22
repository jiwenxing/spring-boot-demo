package com.jverson.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestJmm {

	private static int count = 0;

	
	/*public static void main(String[] args) throws InterruptedException {
		
		final CountDownLatch begin = new CountDownLatch(1); //为0时开始执行
		final ExecutorService exec = Executors.newFixedThreadPool(15);
		
		for (int i = 0; i < 9; i++) {
			final int NO = i + 1;
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						begin.await(); //等待直到 CountDownLatch减到1
						synchronized (this) {
							count++;
							System.out.println("thread " + NO + ": " + count);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			exec.submit(runnable); 
		}
		Thread.sleep(2000);
		System.out.println("开始执行");    
	    begin.countDown(); // begin减一，开始并发执行  
	    exec.shutdown();      
	}*/
	
	/*public static void main(String[] args) throws InterruptedException {
		System.out.println("-----------");
		final CountDownLatch cdl = new CountDownLatch(1);
		final ExecutorService executorService = Executors.newFixedThreadPool(10);
		
		for(int i=0; i<9; i++){
			final int NO = 1;
			cdl.await();
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					System.out.println("thread " + NO + ": " + count++);
				}
			};
			executorService.submit(runnable);
		}
		
		System.out.println("开始执行。。。");
		cdl.countDown();
		executorService.shutdown();
	}*/
	
	public static void main(String[] args) {
		System.out.println((int)(Double.valueOf("1.5")*400));
	}

}
