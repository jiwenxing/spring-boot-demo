# Java Timer & TimeTask
---

## Java 定时任务原理

在 Java 中要实现多线程有实现 Runnable 接口和扩展 Thread 类两种方式。只要将需要异步执行的任务放在 run() 方法中，在主线程中启动要执行任务的子线程就可以实现任务的异步执行。如果需要实现基于时间点触发的任务调度，就需要在子线程中循环的检查系统当前的时间跟触发条件是否一致，然后触发任务的执行。所以最原始的定时任务是创建一个 thread，然后让它在 while 循环里一直运行着，通过 sleep 方法来达到定时任务的效果。如下所示：

```java
public class RunnableTaskDemo {

	public static void main(String[] args) {
		final long timeInterval = 2000;  
		
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				while(true){
					System.out.println("excute every 2 seconds, current time: " + new Date());
					try {
						Thread.sleep(timeInterval); //通过使线程休眠达到间隔一定时间的目的
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
```

## Java Timer 和 TimeTask 实现任务调度

使用Timer 和 TimeTask的简单实现如下：

```java
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
```

可以看到，为了便于开发者快速地实现任务调度，Java JDK 对任务调度的功能进行了封装，实现了Timer 和TimerTask 两个工具类。其中TimeTask 抽象类在实现Runnable 接口的基础上增加了任务cancel() 和任务scheduledExecuttionTime() 两个方法。Timer类采用TaskQueue 来实现对多个TimeTask 的管理。TimerThread 集成自Thread 类，其mainLoop() 用来对任务进行调度。而Timer 类提供了四种重载的schedule() 方法和重载了两种sheduleAtFixedRate() 方法来实现几种基本的任务调度类型。可以发现实现原理其实和上面基本一致都是**利用Java 的多线程技术**，只是做了更多的封装更方便开发者使用。

详细的用法如下所示：

```java
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
```

## Timer + TimeTask 定时任务的缺点

一、任务堆积

所有的TimerTask只有一个线程TimerThread来执行，因此同一时刻只有一个TimerTask在执行。一般情况下我们的线程任务执行所消耗的时间应该非常短，但是由于特殊情况导致某个定时器任务执行的时间太长，那么他就会“独占”计时器的任务执行线程，其后的所有线程都必须等待它执行完，这就会延迟后续任务的执行，使这些任务堆积在一起。

二、任务终止

任何一个TimerTask的执行异常都会导致Timer终止所有任务，在很多场景中这样的情况也是不允许的。

三、集群环境重复执行

Timer + TimeTask定任务和Spring自带的Scheduled Task（支持线程池管理）一样有一个共同的缺点，那就是应用服务器集群下会出现任务多次被调度执行的情况，因为集群的节点之间是不会共享任务信息的，每个节点上的任务都会按时执行。

四、Timer执行周期任务时依赖系统时间

Timer执行周期任务时依赖系统时间，如果当前系统时间发生变化会出现一些执行上的变化，而ScheduledExecutorService基于相对时间的延迟，不会由于系统时间的改变发生执行变化。

## 使用`java.util.concurrent.ScheduledExecutorService`代替`Java.util.Timer/TimerTask`

鉴于以上Timer的缺点，`java.util.concurrent.ScheduledExecutorService`的出现正好弥补了Timer/TimerTask的缺陷。ScheduledExecutorService基于ExecutorService，是一个完整的线程池调度。
它具有以下优点：

- ScheduledExecutorService任务调度是基于相对时间，不管是一次性任务还是周期性任务都是相对于任务加入线程池（任务队列）的时间偏移。
- 基于线程池的ScheduledExecutorService允许多个线程同时执行任务，这在添加多种不同调度类型的任务是非常有用的。
- 同样基于线程池的ScheduledExecutorService在其中一个任务发生异常时会退出执行线程，但同时会有新的线程补充进来进行执行。ScheduledExecutorService可以做到不丢失任务。

```java
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
```


## 总结

综上讨论会发现`Timer + TimeTask`实现定时任务可能真的已经成为历史，因为已经出现了更优秀更便捷的替代方式，ScheduledExecutorService拥有Timer/TimerTask的全部特性，并且使用更简单，支持并发，而且更安全，因此没有理由继续使用`Timer/TimerTask`，完全可以全部替换。需要说明的一点是构造ScheduledExecutorService线程池的核心线程池大小要根据任务数来定，否则可能导致资源的浪费。而Spring的Scheduled Task功能本质上就是利用`java.util.concurrent.ScheduledExecutorService`实现，这将在下一章进行讨论。

## References

- [Timer/TimerTask与ScheduledExecutorService](http://blog.csdn.net/qq_32786873/article/details/53024056)
- [Java实现定时任务的三种方法](http://blog.csdn.net/haorengoodman/article/details/23281343/)