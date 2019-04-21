# Spring Data Cache
---

默认使用 ConcurrentMapCacheManager  不可配置，一般用于测试

 it comes with no cache configuration options. However, it may be useful for testing or simple caching scenarios. For advanced local caching needs, consider [`JCacheCacheManager`](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/cache/jcache/JCacheCacheManager.html), [`EhCacheCacheManager`](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/cache/ehcache/EhCacheCacheManager.html), [`CaffeineCacheManager`](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/cache/caffeine/CaffeineCacheManager.html).

https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/cache/concurrent/ConcurrentMapCacheManager.html


Guava Cache 是 Google Guava 工具包中的一个非常方便易用的本地化缓存实现，基于 LRU 算法实现，支持多种缓存过期策略；
EhCache 是一个纯Java的进程内缓存框架，具有快速、精干等特点，是 Hibernate 中默认的 CacheProvider； 
Caffeine 是使用 Java8 对 Guava 缓存的重写版本，在 Spring Boot 2.0 中将取代，基于 LRU 算法实现，支持多种缓存过期策略。


guava cache

使用方法

原理 及 数据结构

强引用 弱引用

淘汰策略

统计



## 参考

- [Caffeine Cache 进程缓存之王](https://www.itcodemonkey.com/article/9498.html)

