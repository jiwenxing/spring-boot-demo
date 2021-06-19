# Cache Introduction
---

## 缓存为王，性能至上

系统性能是用户体验的重要因素，而缓存作为分布式系统中的重要组件，主要解决高并发，大数据场景下，热点数据访问的性能问题，提供高性能的数据快速访问，是一种以空间换时间的艺术。

## 缓存分类

一般按照缓存与应用的耦合程度以及缓存是否可以共享可以将常用的缓存分为本地缓存（local cache）和分布式缓存（remote cache），实际项目中需要根据业务场景选择合适的缓存工具，通常在大型项目中会结合使用多级缓存（例如 redis+guava），利用本地缓存减轻 Redis 的压力，同时提高系统的性能。

从某种角度看，CDN、反向代理也都是缓存。CDN 即内容分发网络（Content Delivery Network），部署在距离终端用户最近的网络服务商，在这里缓存网站的一些静态资源，可以就近以最快速度返回给用户，如视频网站和门户网站会将用户访问量最大的热点内容缓存在CDN；而反向代理属于网站前端架构的一部分，部署在网站的前端，当用户请求到达网站的数据中心时，最先访问到就是反向代理服务器，这里缓存网站的静态资源（如js，css，图片），无需将请求继续转发给应用服务器就能返回给用户。

下面我们重点关注一下服务端应用经常涉及到的本地缓存和分布式缓存！


### Local Cache    

本地缓存即应用中的缓存组件，是相对于网络而言的，同时也叫做进程缓存。对 JVM 应用来讲又分为堆内缓存和对外缓存。

优点：
- 应用和 cache 是在同一个进程内部，请求缓存非常快速，没有网络开销等。
- 实现简单，相较于分布式缓存不需要单独维护缓存集群

缺点：
- 缓存跟应用程序耦合，多个应用程序无法直接的共享缓存，对内存是一种浪费。
- 进程内缓存可能会影响垃圾回收进而影响系统性能。
- 不能实现强一致性，但随着缓存元素的过期或被逐出，所有缓存实例间可以达到最终一致性。

使用场景：
- 在单应用不需要集群支持或者集群情况下各节点无需互相通知的场景下使用本地缓存较合适；
- 业务无关的（数据字典等）数据，没有必要搞分布式的集群缓存
- 只需要简单的缓存数据的功能，而无需关注更多存取、清空策略等深入的特性时

实现方式：
- 静态变量实现，最常用的单例实现静态资源缓存，例如将行政区域与邮政编码的对应关系放在一个 static 修饰的 map 里实现共享。
- 使用缓存框架，静态变量的方式只使用一些简单的场景，如果缓存需要考虑过期策略、空间占用等一系列问题就需要选择一些成熟的本地缓存框架来实现了，例如 Guava Cache、Ehcache、Caffeine 等。


> 这里指的注意的是本地缓存数据的实时性不是很高，如果在一些实时性要求高的场景下，可以考虑使用结合 ZooKeeper 的自动发现机制，实时变更本地静态变量缓存，也就是常见的统一配置系统，这部分将在 Zookeeper 章节详细讲解。

### Remote Cache

分布式缓存即缓存中间件，是应用程序的外部扩展，通常是集群部署在多个节点上的，共同构成一个大的逻辑缓存，如 Memcache、Redis 等。目前分布式缓存设计，在大型网站架构中是必备的架构要素。

优点：
- 自身就是一个独立的应用，与本地应用隔离，多个应用可直接的共享缓存。
- 可以缓存大量数据，不需要考虑 JVM 内存以及 GC 的限制，集群易于横向扩展

缺点：
- 网络延迟和对象序列化导致分布式缓存慢于进程内缓存
- 需要专门的缓存系统搭建和运维

使用场景：
- 大型系统，需要缓存共享、分布式部署、缓存内容很大等
- 如果你试图寻求一个多节点部署情况下的强一致性缓存解决方案，采用分布式缓存。


## 缓存的一些概念

### 缓存命中率

从缓存中读取次数/总读取次数。命中率越高越好。这是一个非常重要的监控指标。

### 缓存回收策略

1. 基于空间：指缓存设置了存储空间，如设置为 100MB，当达到存储空间上限时，按照一定的策略移除数据。以 JVM 为例，当 Eden 区空间不足时，触发 MinorGC 回收内存；当老年代空间不足时，触发 MajorGC 回收内存。
2. 基于容量：指缓存设置了最大大小，当缓存的条目超过最大大小时，按照一定的策略移除数据。如 Gauva Cache 可以通过 maximumSize 参数设置缓存容量，当超出 maximumSize 时，按照 LRU 算法进行缓存回收。
3. 基于时间：TTL（Time To Live）即缓存数据从创建开始直到到期的一个时间段，如 Gauva Cache 可以通过 expireAfterWrite 参数设置过期时间；TTI（Time To Idle）：空闲期，即缓存数据多久没被访问后移除缓存的时间，如 Gauva Cache 可以通过 expireAfterAccess 参数设置空闲时间。

### 缓存回收算法
1. FIFO（First In First Out）即先放入缓存的先被移除；
2. LRU（Least Recently Used）使用时间距离现在最久的那个被移除。
3. LFU（Least Frequently Used）一定时间段内使用次数（频率）最少的那个被移除。

### 缓存更新策略

主要有两大类：Cache-Aside 和 Cache-As-SoR（Read-Through、Write-Through、Write-Behind）。

简单解释一下上面的分类，所谓 Cache-Aside 就是常规使用 Cache 的思维将其当做一个辅助，该模式对缓存的关注点主要在于业务代码，即缓存的更新，删除与数据库的操作，以及他们之间的先后顺序都在业务代码中实现。对于这种方式，我们实现过程中就需要维护两个数据源，一个是缓存，一个是数据库，比较繁琐。一般情况下大的原则如下，当然会有一些细节问题后面会深入讨论

1. 对于读请求，先读缓存，如果未命中，再读数据库，并将数据 set 回缓存。
2. 对于写请求，先删缓存，再写数据库。


而对于 Cache-As-SoR 就是把缓存直接当做数据库，而把对数据库的操作让缓存来代理了，比如 Guava Cache 中的 CacheLoader。当缓存失效的时候（过期或LRU换出），Cache-Aside 是由业务代码负责把数据加载入缓存，而 Cache-As-SoR 则用缓存服务自己来加载，从而对业务代码是透明的。


## Guava vs Caffeine

Guava Cache 是 Google Guava 工具包中的一个非常方便易用的本地化缓存实现，基于 LRU 算法实现，支持多种缓存过期策略。其主要实现的缓存功能有：

- 自动将 entry 节点加载进缓存结构中；
- 当缓存的数据超过设置的最大值时，使用 LRU 算法移除；
- 具备根据 entry 节点上次被访问或者写入时间计算它的过期机制；
- 缓存的 key 被封装在 WeakReference 引用内；
- 缓存的 Value 被封装在 WeakReference 或 SoftReference 引用内；
- 统计缓存使用过程中命中率、异常率、未命中率等统计数据。

然而在 Spring Boot 1.5.17 Release 版本的官方文档中，Guava 却在众多支持的缓存方案中不幸被遗弃（Spring5 也不再支持）。另外相较于耳熟能详被广泛使用的 Guava，Caffeine 更像是一个新面孔，这到底发生了什么呢？

![](http://pgdgu8c3d.bkt.clouddn.com/201811071516_547.png)

看官方文档原来 Caffeine 是使用 JDK8 对 Guava 重写而来，同时在算法和内存方面做了优化，从 [对比结果](https://github.com/ben-manes/caffeine/wiki/Benchmarks) 上来看不管是并发读、并发写或者并发读写，Caffeine 比 Guava 性能都有了非常显著的提高。另外 Caffeine 的 API 的操作功能和 Guava 是基本保持一致的，并且 Caffeine 为了兼容之前是 Guava 的用户，做了一个 Guava 的 [Adapter](https://github.com/ben-manes/caffeine/wiki/Guava) 进行迁移。

后面将详细介绍 Caffeine 在 Spring Boot 中的使用。

## Memcache vs Redis

> Memcached：一款完全开源、高性能的、分布式的内存系统；    
Redis：一个开源的、Key-Value 型、基于内存运行并支持持久化的 NoSQL 数据库；

Memcached 和 Redis 都是 C 语言实现，被大量使用的开源缓存中间件，也经常被放在一起进行对比。从定义不难看出两者的定位和目标是有差异的，Memcached 追求的高性能的内存服务；而 Redis 追求的不仅仅是内存运行，还有数据持久化的需求；

- 存储方式：memecache 把数据全部存在内存之中，没有持久化机制，断电后会挂掉；数据不能超过内存大小。Redis 支持数据的持久化（有 Snapshot 和 AOF 日志两种持久化方式）。
- 数据支持类型：Redis 支持 5 种数据格式（string、hash、list、set、sortedSet），另外还支持范围查询、bitmaps、hyperloglogs 和 地理空间（geospatial） 索引半径查询。在一些业务中使用非常方便；而 memecache 仅支持基础的 key-value 键值对类型数据存储。
- 多线程：Memcache 支持多线程，Redis 支持单线程；CPU 利用方面 Memcache 优于 Redis；
- 分布式环境：memcached 的分布式由客户端实现，通过一致性哈希算法来保证访问的缓存命中率；Redis 的分布式由服务器端实现，通过服务端配置来实现分布式；
- 功能扩展：Redis 不仅仅作为缓存，也可以被用作消息、队列 (Redis 原生支持发布 / 订阅)，任何短暂的数据，应用程序，如 Web 应用程序会话，网页命中计数等。

![](http://pgdgu8c3d.bkt.clouddn.com/201811071713_203.png)

在选择上总结一句话，以往使用 Redis 的 String 类型做的事，都可以用 Memcached 替换，以此换取更好的性能提升； 除此以外，优先考虑 Redis；


## 参考

- [缓存那些事](https://tech.meituan.com/cache_about.html)
- [Design Of A Modern Cache](http://highscalability.com/blog/2016/1/25/design-of-a-modern-cache.html)
- [spring-boot-docs-1.5.17.RELEASE](https://docs.spring.io/spring-boot/docs/1.5.17.RELEASE/reference/htmlsingle/#boot-features-caching-provider-caffeine)
- [Memcached 与 Redis](https://www.imooc.com/article/23549)
- [Caffeine Cache 进程缓存之王](https://www.itcodemonkey.com/article/9498.html)