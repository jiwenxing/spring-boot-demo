# 兜底缓存方案设计
---

在一些很核心的系统里，我们往往会做各种各样的应急预案SOP，例如对上游的限流，对下游的熔断，对非核心链路的降级等等，往往这些还不够，在遇到一些极端情况下核心链路故障我们也需要有办法进行应对而不是让用户出现白屏。这个最后一道防线就是我们今天要说的静态兜底缓存。

下面会从几个方面介绍一下怎么设计一个兜底缓存的功能，结合业务进行需求分析、方案设计及工程实现。

# 需求分析

## 使用场景

每个业务特点不同，以酒店搜索的业务为例，核心链路例如召回、排序服务挂掉之后，用户搜索将会无结果，这样会严重影响到用户体验和公司形象。因此在这种极端情况下我们可以返回一些提前缓存的数据，尽管结果的效果不够理想（例如会出现距离较远、满房停业的poi等），那也比无结果造成的影响要小！

## key 设计

key 的设计和业务紧密相关，不仅关系到缓存量、命中率，还关系到缓存结果的效果，需要做一些平衡。因此 key 需要支持定制化，还可以支持多级 key，这样可以一定程度的提升缓存的效果的同时保证命中率。例如酒店搜索的 key 设计

> 三级 key 设计，依次如下，查询时先查询 level2 未命中再依次查询 level1、level0  
level0：platform(点评或美团) + query + accommodationType + city  
level1：platform + query + accommodationType（全日房或钟点房） + city + dateRange（入离时间，这里用 hashcode）  
level2：platform + query + accommodationType + city + dateRange + geohash5

## 写缓存

什么样的结果需要进到缓存里，这个应该也是和业务强相关的，例如我们缓存不区分排序类型，因此最好是缓存智能排序的结果效果更好一些，另外可能还需要对结果做截断再缓存

## 读缓存

当我们打开缓存开关的时候能够允许我们控制走缓存的比例，便于我们在服务恢复的情况下逐步放开正常流量。

# 技术方案

## 架构设计

基于这样的使用场景和需求，我们的缓存需要做到尽量代码侵入性低，方便集成。例如可以通过简单的注解和配置即可完成接入，或者通过简单实现一个接口或继承一个类简单实现一些业务定制功能即可。

另外可以做的通用一些，不同的业务都可以简单实用，例如以 jar 的方式进行集成，springboot 的话直接做成一个 starter。

结合 hystrix 注解方式示例

```Java
@com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand(
    fallbackMethod ="demoViaCacheAnnotationFallback", // fallback方法，可以从其中拿到缓存的结果。必须实现。
    commandKey = "demoViaCacheAnnotation",
    storeKeyMethod = "getStoreKey",    // 获取缓存 key 的方法。必须实现。
    checkResultMethod = "checkResult",  // 检查执行结果是否正确的方法，返回true才会对结果缓存。可以不实现，不实现的话默认返回true。
    commandProperties = {
        // 其他参数同熔断降级一致
        // 开启静态数据缓存兜底功能
        @HystrixProperty(name = HystrixPropertiesManager.STATIC_DATA_ENABLED, value = "true")
    }
)
public Object demoViaCacheAnnotation(@RequestParam(value = "param1", required = false, defaultValue = "0") int param1,
    @RequestParam(value = "param2", required = false, defaultValue = "0") int param2) {
    Map<Object, Object> map = Maps.newHashMap();
    map.put("result", Lists.newArrayList("ok", "not ok"));
    map.put("num", 3432);
    Set set = Sets.newHashSet(222, 333);
    return Lists.newArrayList(map, set);
}
```


另外还需要对原系统性能几乎无影响，因此我们需要将写缓存异步化，可以采用单独的线程来完成或者直接将其加入一个延时队列进行异步消费。

## 缓存选型

一般缓存我们都会想到用 redis，鉴于缓存量可能会比较大，且99%的时间并不会真正去读，因此为了节省成本可以选用 tair 这类比较廉价的存储介质，性能也不会有啥问题！

当然在设计的时候可以将 cache 这块抽象出来以便扩展不同的存储类型。

## 序列化方式

一般这些都需要设计成可扩展的形式，一般可以使用 protostuff 或者当前 rpc 自带的一些序列化工具。

## 监控

正常需要的监控项有写缓存流量/耗时、缓存hit/miss比例（包含各级key miss/hit 比例）、读缓存耗时等

# 实现

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/e3c69ac1-6eb6-4902-99d3-ad4eec443621)

jar 里带一个默认的 xml 配置，使用方只需要导入即可使用。




我们定义一个核心类 CacheManager（RespCahce），这个类提供我们所需的所有接口如下
![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/aa4272b0-fd16-437f-8d13-00146d7d7dbb)

其中最主要的是

## 异步缓存结果

entry 直接存入一个延迟队列。延迟队列是为了避免并发操作同一份数据可能造成的并发问题。注意这个队列是有界的，超出的直接丢弃即可！同时可以控制写入的采样率，因为没有必要每条结果都去缓存

```Java
private DelayQueue<CacheEntry> queue = new DelayQueue(); // 注意 DelayQueue 队列中的对象必须实现 Delayed 接口的 getDelay 方法

public boolean setResponseAsync(CacheEntry<T> entry) {
    // 有界队列，确保挤压数据不会压垮服务
    if (!entry.check() || queue.size() > QUEUE_LIMIT || config.getSampleRation() < Constants.MIN_RATIO) {
        return false;
    }

    // 控制写入缓存的采样率
    Random random = ThreadLocalRandom.current();
    int ratio = random.nextInt(Constants.MAX_RATIO);
    if (ratio <= config.getSampleRation()) {
        return queue.offer(entry);
    }
    return false;
}
```

然后启动几个线程去消费这个队列即可

```Java
// 初始化写缓存数据的任务
for (int i = 0; i < 5; i++) {
       executor.submit(new BatchSetRunnable());
}

private class BatchSetRunnable implements Runnable {
    public void run() {
        while (true) {
            try {
                CacheEntry item = queue.take();
                long ts0 = System.currentTimeMillis();
                setResponse(item);
                long cost = System.currentTimeMillis() - ts0;
                Cat.newCompletedTransactionWithDuration("downgradeRespCache", "setResponse", cost);
            } catch (Throwable e) {
                logger.info("interrupted when wait pair in the queue!", e);
            }
        }
    }
}
```

## 读取缓存结果

这里支持缓存 miss 的情况下执行一个 fallback 方法来获取结果，感觉其实没多大必要，缓存开启的时候服务其实已经挂了
。
```Java
public T getResponse(CacheEntry<T> entry, long timeout, Supplier<T> fallbackSupplier) {
    T resp = null;
    try {
        long ts0 = System.currentTimeMillis();
        resp = this.getResponse(entry, timeout);
        if (resp == null) {
            if (config.enableFallback() && fallbackSupplier != null) {
                long ts1 = System.currentTimeMillis();
                resp = fallbackSupplier.get();
                Cat.newCompletedTransactionWithDuration("downgradeRespCache", "getResponse_fallback", System.currentTimeMillis() - ts1);
            }
        }
        Cat.newCompletedTransactionWithDuration("downgradeRespCache", "getResponse", System.currentTimeMillis() - ts0);
    } catch (Exception ex) {
        logger.error("fail to getResponse!", ex);
    }

    return resp;
}
```

## 读取一些开关配置

isDowngradeCacheRequest 判断该条请求是否需要降级到读 cache，可以通过配置中心控制比例

```Java
public boolean isDowngradeCacheRequest() {
    Random random = ThreadLocalRandom.current();
    int ratio = random.nextInt(Constants.MAX_RATIO);
    return ratio <= config.getReadCacheRatio();
}
```

## CacheEntry 对象

前面缓存和读取操作的对象都是 CacheEntry，CacheEntry 可以是一个抽象类或接口，其中持有 Request 和 Response 对象，使用方可以继承或实现其中的接口和方法从实现业务逻辑的定制如下图。其中 check 方法用于判断结构是否需要写入缓存（例如结果为空，或带有一些特殊筛选项的请求）；`List<String> getKeys()` 用户可以自定义 key 的拼接，支持多级 key；getHolder 返回一个空的T类型 holder，用于反序列化（不同序列化方式要求不一样）；processHolder 对缓存的数据做一些处理，例如截断等；getExpireInSeconds 获取缓存过期时间（可配置）。

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/6aedc687-fbd9-45e4-a205-64fee49d923d)

# 如何使用

我们在 jar 里提供默认配置的 xml，业务没有特殊需要直接引用即可。这里需要读取使用方的一些配置如 tair 等

```xml
<context:property-placeholder location="classpath:config/config.properties"/>

<bean id="downgradRespCacheConfig"
      class="com.meituan.search.cache.LionConfig">
    <property name="mtConfigClient" ref="downgradeConfigClient"/> <!-- 这里用到了配置中心的客户端，使用方只需要在配置里给客户端起一个叫做  downgradeConfigClient 的别名即可-->
</bean>

<alias name="mccClient" alias="downgradeConfigClient"/>  <!-- 使用方的别名配置 -->

<!-- RespCache(KvStore store, Codec<T> codec, Compressor compressor, Config config) -->
<bean id="downgradRespCache"
      class="com.meituan.search.cache.RespCache">
    <constructor-arg ref="downgradTairStore"/>
    <constructor-arg ref="thriftCodec"/>
    <constructor-arg ref="snappyCompressor"/>
    <constructor-arg ref="downgradRespCacheConfig"/>
</bean>
```

然后业务需要自己实现一下 CacheEntry 抽象类或接口，按照业务特点实现其中的 getKeys、getExpireInSeconds 方法即可

最后在服务的入口处添加兜底逻辑

```Java
@Resource
private RespCache<RankResponse> downgradRespCache;

public RankResponse rank(RankRequest rankRequest) throws TException {
    // 兜底缓存逻辑
    if (downgradRespCache.isDowngradeCacheRequest()) {
        // 确定本次请求走兜底缓存时，无论是否有无结果，都返回
        RankResponse downgradeResponse = downgradRespCache.getResponse(
                new HotelCacheEntry(rankRequest, new RankResponse()),
                300,
                () -> {
                    RankResponse fallbackResp = this.hotelSearch(rankRequest); // fallback 逻辑
                    downgradRespCache.setResponseAsync(new HotelCacheEntry(rankRequest, fallbackResp));
                    return fallbackResp;
                });
        if (downgradeResponse == null) {
            Cat.logEvent("hotelSearchDowngrade", "Response_miss");
            downgradeResponse = new RankResponse();
            downgradeResponse.setStatus(ResponseStatus.OK);
            downgradeResponse.setPairContext(PairContextUtils.buildPairContext());
        } else {
            Cat.logEvent("hotelSearchDowngrade", "Response_hit");
        }
        return downgradeResponse;
    }
    return hotelSearch(rankRequest);
}
```
