# MyBatis 深入解析
---


有了 JDBC 为什么还需要 Mybatis？ JDBC 是怎么演变到 Mybatis，为什么要将 JDBC 封装成 Mybaits 这样一个持久层框架。从这几个问题出发我们来探讨一下 MyBatis 的实现原理。

先来看看 JDBC 的使用过程，也就是最原始的使用方式。

1. 加载 JDBC 驱动；

2. 建立并获取数据库连接；

3. 创建 JDBC Statements 对象；

4. 设置 SQL 语句的传入参数；

5. 执行 SQL 语句并获得查询结果；

6. 对查询结果进行转换处理并将处理结果返回；

7. 释放相关资源（关闭 Connection，关闭 Statement，关闭 ResultSet）；


每一次数据库交互都要循环上述过程，但这个过程中只有 4、5、6 这三步是业务相关的，其它步骤的操作都是固定的代码。下面我们来看看 Mybatis 是怎么做的。

## 连接的获取和释放

获取和释放连接其实是一个比较耗资源的过程，和多线程一样，为了避免频繁的创建和销毁操作，应尽量使用池化的技术，也就是使用数据库连接池。

由前面的内容我们知道，javax.sql.DataSource 作为 DriverManager 的替代者，是获取连接的首先方式，并且目前有很多基于连接池的实现，例如 SpringBoot 默认的 jdbc-pool，Alibaba 开源的 Durid 德鲁伊等。DataSource 配置如下，它将用于构建 sqlSessionFactory 对象。

```Java
@Bean(name ="singleDataSource")
public DataSource singleDataSource() {
	DataSourceBuilder builder =  DataSourceBuilder.create();
	builder.type(DruidDataSource.class);
	builder.url(url);
	builder.username(username);
	builder.password(password);
	builder.driverClassName(driverClassName);
	DruidDataSource dataSource = (DruidDataSource)builder.build();
	dataSource.configFromPropety(initDataSorucceProperties());
	return dataSource;
}

@Bean(name ="singleSqlSessionFactory")
public SqlSessionFactory singleSqlSessionFactory(@Qualifier("singleDataSource") DataSource dataSource) throws Exception {
	SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
	bean.setDataSource(dataSource);
	Resource[] singleLocation = new PathMatchingResourcePatternResolver().getResources("classpath:sqlmap/single/*.xml");
	Properties properties = new Properties();
	properties.setProperty("reasonable","false");
	Interceptor pagePlugin = new PageInterceptor();
	pagePlugin.setProperties(properties);
	Interceptor[] plugins = new Interceptor[]{pagePlugin};
	bean.setPlugins(plugins);
	bean.setMapperLocations(singleLocation);
	return bean.getObject();
}
```

## SQL 统一存取

使用 JDBC 进行操作数据库时，SQL 语句基本都散落在各个 JAVA 类中，这样可读性很差，不利于维护以及做性能调优。而 Mybatis 将这些 SQL 语句统一集中放到配置文件中，然后通过 key 值去获取对应的 SQL 语句。

## 传入参数映射和动态 SQL







## 参考

- [MyBatis 原理深入解析](https://www.jianshu.com/p/ec40a82cae28)