# Caffeine Cache
---

想必很多人对 Google 出品的 Guava Cache 都不陌生，还没怎么听过 Caffeine Cache，但是如果你使用 2.0.0 以上版本的 Springboot 时会惊讶的发现其已经不再支持 Guava Cache 了，在其文档中推荐了 Caffeine Cache。Guava 这么优秀的工具怎么突然就给废了呢？

其实 Caffeine Cache 就是 Guava Cache 的 JDK8 重写版本，加入了一些优化，两者的原理包括使用方式都基本类似，但是性能有了很大的提升。下图是几种 Cache 的性能对比，[数据出处](http://highscalability.com/blog/2016/1/25/design-of-a-modern-cache.html)

![](https://jverson.oss-cn-beijing.aliyuncs.com/5f9035ee96f6e4e571c30328dd734c79.jpg)


下面重点介绍一下在 Springboot 项目中集成 Caffeine 的过程。

首先加入 Maven 依赖

```xml
<!-- cache -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
<dependency>
	<groupId>com.github.ben-manes.caffeine</groupId>
	<artifactId>caffeine</artifactId>
</dependency>
```

Caffeine 配置

```java
@Configuration
@EnableCaching
public class CaffeineCacheConf {
	public static final int DEFAULT_MAXSIZE = 50000;
	public static final int DEFAULT_TTL = 30*60;	
	/**
	 * cache name、过期时间（秒）、最大容量(条)
	 * 缺省：30分钟超时、最多缓存50000条数据，需要修改可以在构造方法的参数中指定。
	 */
	public enum Caches{
		testCache, //使用默认值
		testCache1(60), //一分钟过期。最大容量使用默认值
		testCache2(10, 20), //指定过期时间和最大容量
		;	
		Caches() {
		}
		Caches(int ttl) {
			this.ttl = ttl;
		}
		Caches(int ttl, int maxSize) {
			this.ttl = ttl;
			this.maxSize = maxSize;
		}		
		private int maxSize = DEFAULT_MAXSIZE;	
		private int ttl = DEFAULT_TTL;	
		
		public int getMaxSize() {
			return maxSize;
		}
		public int getTtl() {
			return ttl;
		}
	}
	
	@Bean
    public CacheManager cacheManager() {
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		ArrayList<CaffeineCache> caches = new ArrayList<CaffeineCache>();
		for(Caches c : Caches.values()){
			caches.add(new CaffeineCache(c.name(), 
				Caffeine.newBuilder()
				.expireAfterWrite(c.getTtl(), TimeUnit.SECONDS)
				.maximumSize(c.getMaxSize())
				.build())
			);
		}
		cacheManager.setCaches(caches);
		return cacheManager;
    }

}
```

利用注解对方法进行缓存，可以简单的对其进行测试。

```java
@Cacheable(value="testCache", sync=true)
public int testCache(int i) {
	System.out.println("----- not from cache!");
	return i*i;
}
```



## 链接

- [Caffeine official github repo](https://github.com/ben-manes/caffeine)
- [Design Of A Modern Cache - Caffeine 设计原理](http://highscalability.com/blog/2016/1/25/design-of-a-modern-cache.html)
- [Caffeine Cache Statistics](https://github.com/ben-manes/caffeine/wiki/Statistics)
- [Caffeine Guava Ehcache 等不同缓存性能对比](https://github.com/ben-manes/caffeine/wiki/Benchmarks)