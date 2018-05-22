package com.jverson.test;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Predicates;

public class RetryTest {

	private static Integer num = 0;
	
	public static void main(String[] args) {
		Boolean result = false;
		Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()  
		        .retryIfResult(Predicates.equalTo(false))
		        .withStopStrategy(StopStrategies.stopAfterAttempt(3))
//		        .withWaitStrategy(WaitStrategies.fixedWait(2, TimeUnit.SECONDS))
		        .withWaitStrategy(WaitStrategies.incrementingWait(1, TimeUnit.SECONDS, 1, TimeUnit.SECONDS))  
		        .build(); 
		try {  
			System.out.println("aaaaaaaaaaa");
		    result = retryer.call(getTokenUserCall);  
		} catch (Exception e) {  
		    System.err.println("still failed after retry." );  
		} 
		System.out.println(result);
	}
	
	
	private static Callable<Boolean> getTokenUserCall = new Callable<Boolean>() {

		@Override
		public Boolean call() throws Exception {
			num++;
			System.out.println("calling..........num=" + num);
			if (num==4) {
				return true;
			}
			return false;
		}
		
	};
	
	
}
