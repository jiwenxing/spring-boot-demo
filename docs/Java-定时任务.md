
现代的 Web 应用程序框架在范围和复杂性方面都有所发展，应用程序的每个底层组件也必须相应地发展。作业调度是现代系统中对 Java 应用程序的一般要求，而且也是对 Java 开发人员一贯的要求。目前 Java 系统中实现调度任务的方式大体有一下三种：


### Java 实现调度任务的三种方式

#### 一、使用JDK自带的`java.util.Timer`及`java.util.TimerTask`类实现

在我们编程过程中如果需要执行一些简单的定时任务，无须做复杂的控制，我们可以考虑使用JDK中的Timer定时任务来实现。当然如果集成了Spring框架的话，利用下面介绍的第二种方法将会更简单一些。

Timer计时器可以定时（指定时间执行任务）、延迟（延迟5秒执行任务）、周期性地执行任务（每隔个1秒执行任务），但是，Timer存在一些缺陷。首先Timer对调度的支持是基于绝对时间的，而不是相对时间，所以它对系统时间的改变非常敏感。其次Timer线程是不会捕获异常的，如果TimerTask抛出的了未检查异常则会导致Timer线程终止，同时Timer也不会重新恢复线程的执行，他会错误的将整个Timer线程都会取消。同时，已经被安排单尚未执行的TimerTask也不会再执行了，新的任务也不能被调度。故如果TimerTask抛出未检查的异常，Timer将会产生无法预料的行为。


**但是JDK在1.5推出了`java.util.concurrent.ScheduledExecutorService`用来代替`Java.util.Timer/TimerTask`，基于线程池，拥有Timer/TimerTask的全部特性，并且使用更简单，支持并发，而且更安全，可以完全替代。**


#### 二、使用Spring 定时任务

Spring 3.0+ 自带的任务调度实现，是一个轻量级的定时任务调度器，支持固定时间(支持cron表达式)和固定时间间隔调度任务，支持线程池管理。和第一种方式一样，应用服务器集群下会出现任务多次被调度执行的情况，因为集群的节点之间是不会共享任务信息的，每个节点上的任务都会按时执行。




#### 三、使用第三方插件 Quartz

Quartz是一个功能完善的任务调度框架，支持集群环境下的任务调度，需要将任务调度状态序列化到数据库。


### 总结

如果是在纯粹的Java环境需要实现定时任务毫无疑问就使用JDK自带的`java.util.concurrent.ScheduledExecutorService`替代`Timer & TimerTask`实现即可，这种场景一般比较简单，也不存在集群的问题。

如果是集成Spring框架开发应用，则使用Spring的`@Scheduled`注解实现，简洁方便省事。但是此类应用很可能是集群部署，因此需要通过一定的途径避免集群环境下任务被多次调用的现象发生。常见的方法有使用redis存储一个会过期的常量锁，每台容器执行器先读取锁变量值判断任务是否已被执行；另一种常见的方法是只让指定IP的容器执行定时任务（存在单点的问题）。

如果在集群环境下，想实现定时任务的可视化管理，或者想做一个统一的定时任务应用，亦或者定时任务的场景非常复杂，则建议使用企业级应用系统常用的Quartz，而且现在Spring 或者 Springboot集成Quartz也非常方便。


### 代码示例

下面对三种方式具体实现做详细解读：
- [Java Timer & TimerTask](https://github.com/jiwenxing/spring-boot-demo/wiki/Timer)
- [Spring Schedule](https://github.com/jiwenxing/spring-boot-demo/wiki/Spring-Schedule)
- [Quartz介绍](https://github.com/jiwenxing/spring-boot-demo/wiki/Quartz-%E4%BB%8B%E7%BB%8D)
- [Spring Boot集成Quartz](https://github.com/jiwenxing/spring-boot-demo/wiki/quartz-springboot)