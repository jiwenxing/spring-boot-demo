默认使用 ConcurrentMapCacheManager  不可配置，一般用于测试

 it comes with no cache configuration options. However, it may be useful for testing or simple caching scenarios. For advanced local caching needs, consider [`JCacheCacheManager`](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/cache/jcache/JCacheCacheManager.html), [`EhCacheCacheManager`](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/cache/ehcache/EhCacheCacheManager.html), [`CaffeineCacheManager`](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/cache/caffeine/CaffeineCacheManager.html).

https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/cache/concurrent/ConcurrentMapCacheManager.html



guava cache

使用方法

原理 及 数据结构

强引用 弱引用

