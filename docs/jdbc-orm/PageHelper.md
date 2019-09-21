# Use PageHelper
---

这一篇讲一下 Mybatis Common Mapper 中的 PageHelper 用法。PageHelper 确实使分页简单了很多，它自动实现了分页查询逻辑及返回结果的封装，具体的使用方法可以参考 [官方文档](https://github.com/pagehelper/pagehelper-spring-boot)。下面主要介绍一下自己的实践，另外会附带介绍一下打印 sql、打印 sql 运行时间等一些小技巧。

## 使用方法

在 spring boot 中使用 mysql 和 common mapper，首先在 pom 中添加以下依赖，其中 `pagehelper-spring-boot-starter` 即是 pagehelper 插件。

```xml
<!-- mysql 连接器 -->
<dependency>
  <groupId>mysql</groupId>
  <artifactId>mysql-connector-java</artifactId>
</dependency>
<!--common mapper （包含了 mybatis-spring-boot-starter 依赖） -->
<dependency>
  <groupId>tk.mybatis</groupId>
  <artifactId>mapper-spring-boot-starter</artifactId>
  <version>1.1.4</version>
</dependency>
<!--pagehelper 通用 mapper 分页插件 -->
<dependency>
  <groupId>com.github.pagehelper</groupId>
  <artifactId>pagehelper-spring-boot-starter</artifactId>
  <version>1.1.3</version>
</dependency>
```

配置文件添加以下配置

```properties
-----------mybatis common-mapper configurations----------
pagehelper.helperDialect=mysql
```

代码中实现分页

```java
/**
	 * 分页查找
	 * @param wrap 
	 *     通过 wrap 属性设置查找筛选条件
	 * @param params
	 *     设置分页属性：pageNum（当前页数，默认 1），pageSize（页面行数，默认 10）
	 * @return
	 *     见 PageWrap 说明
	 */
public PageWrap<SkillWrap> getAll(SkillWrap wrap, Map<String, Object> params) {
  // 此处进行分页参数设置
  PageUtil.handlePage(params);
  Example example = new Example(Skill.class);
  example.setOrderByClause("state, create_time desc");
  Example.Criteria criteria = example.createCriteria();
  criteria.andEqualTo("yn", "N");
  if (wrap.getState() != null) {criteria.andEqualTo("state", wrap.getState());
  }
  List<Skill> skills = mapper.selectByExample(example);
  // 将 sql 查询结果转换为分页对象
  PageInfo<Skill> page = new PageInfo<Skill>(skills);
  // 将分页对象实体从 model 转换为 view
  PageWrap<SkillWrap> pageWrap = PageUtil.transfer(page, SkillWrap.class);
  return pageWrap;
}

// 其中 PageUtil 类的实现如下
public class PageUtil {
	/**
	 * 将 PageInfo 转换为自定义的 PageWrap
	 * @param page
	 * @param clazz
	 * @return
	 */
	public static <T> PageWrap<T> transfer(PageInfo<? extends BaseEntity> page, Class<T> clazz) {PageWrap<T> pageWrap = null;
		if (page != null) {
			pageWrap = new PageWrap<T>();
			List<? extends BaseEntity> lists = page.getList();
			List<T> wraps = lists.stream().map(model -> {
				T t = null;
				try {
					t = clazz.newInstance();
					BeanUtils.copyProperties(model, t);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return t;
			}).collect(Collectors.toList());
			pageWrap.setList(wraps);
			pageWrap.setPageNum(page.getPageNum());
			pageWrap.setPageSize(page.getPageSize());
			pageWrap.setTotal(Long.valueOf(page.getTotal()).intValue());
			pageWrap.setHasNextPage(page.isHasNextPage());
			pageWrap.setHasPreviousPage(page.isHasPreviousPage());
		}
		return pageWrap;
	}
	/**
	 * 分页逻辑抽取
	 * @param params
	 */
	public static void handlePage(Map<String, Object> params) {
		int pageNum = 1;
		int pageSize = 10;
		int numSet = MapUtils.getInteger(params,"pageNum", 1);
		int sizeSet = MapUtils.getInteger(params,"pageSize", 10);
		if (params != null && numSet> 0 && sizeSet > 0) {
			pageNum = numSet;
			pageSize = sizeSet;
		}
		PageHelper.startPage(pageNum, pageSize); // 这里使用了 threadlocal 变量
	}
}
```

刚开始觉得很神奇，使用一个静态方法设置了一下 pageNum 和 pageSize 怎么就能实现分页呢，跟到源码里面看了一下，原来分页的参数是一个静态的 ThreadLocal 变量

```java
protected static final ThreadLocal<Page> LOCAL_PAGE = new ThreadLocal<Page>();
/**
     * 设置 Page 参数
     *
     * @param page
     */
protected static void setLocalPage(Page page) {
	LOCAL_PAGE.set(page);
}

public static <E> Page<E> startPage(int pageNum, int pageSize, boolean count) {
	Page<E> page = new Page<E>(pageNum, pageSize, count);
  setLocalPage(page);
  return page;
}
```



## 打印 sql

由于使用 Common Mapper 不用手写 sql，有时候需要知道真正运行 sql 长什么样，便于优化和排查问题，因此最好能将 当前执行的 sql 在 console 中打印出来，网上说的一些设置 mysql 的日志级别等我这里都没有生效，由于框架的 sql 日志以 debug 级别输出，最后无奈将 console 全局日志级别设置为 debug，然后排除了一些不想看到的类日志。

```xml
<Loggers>
  <!-- 可以使用 OFF 关闭一些日志 -->
  <logger name="org.springframework"level="OFF"/>
  <!-- 可以对特定类单独设置日志级别 -->
  <logger name="org.hibernate"level="INFO"/>
  <logger name="org.mybatis"level="INFO"/>
  <logger name="io.netty"level="INFO"/>
  <logger name="org.apache.http"level="INFO"/>
  
  <Root level="DEBUG"includeLocation="true">
    <appender-ref ref="CONSOLE"/>
    <appender-ref ref="INFO-LOG"/>
    <appender-ref ref="ERROR-LOG"/>
  </Root>
</Loggers>
```



## 打印 sql 执行时间

有时候会比较关心程序运行的效率，尤其是数据库操作的效率，以便于后期优化，这时打印 sql 的执行时间即可监控影响性能的一些数据库操作进行针对性的优化。打印 sql 运行时间是一个典型的 AOP 插入日志的问题，很容易想到动态代理什么的，这里通过实现 ibatis 的拦截器 `org.apache.ibatis.plugin.Interceptor` 来实现。

另外注意这里并不针对 Common Mapper 或者 PageHelper，使用了 Mybatis 都可以通过这种方式实现。

```java
@Override
public Object intercept(Invocation invocation) throws Throwable {Object target = invocation.getTarget();
  long startTime = System.currentTimeMillis();
  StatementHandler statementHandler = (StatementHandler) target;
  try {return invocation.proceed();
  } finally {long endTime = System.currentTimeMillis();
    long sqlCost = endTime - startTime;
    BoundSql boundSql = statementHandler.getBoundSql();
    String sql = boundSql.getSql();
    Object parameterObject = boundSql.getParameterObject();
    List<ParameterMapping> parameterMappingList = boundSql.getParameterMappings();
    // 格式化 Sql 语句，去除换行符，替换参数
    sql = formatSql(sql, parameterObject, parameterMappingList);
    System.out.println("SQL：[" + sql + "] cost [" + sqlCost + "ms]");
  }
}
```

需要在配置类中将改拦截器加入到 SqlSessionFactory 的插件列表中才能生效

```java
@Configuration
public class MyBatisConf implements TransactionManagementConfigurer {
	@Autowired
	private DataSource dataSource;
	@Override
	public PlatformTransactionManager annotationDrivenTransactionManager() {
		return new DataSourceTransactionManager(dataSource);
	}

	@Bean(name ="sqlSessionFactory")
	public SqlSessionFactory sqlSessionFactoryBean() throws IOException {SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
		bean.setDataSource(dataSource);
		// 配置打印 sql 执行时间的拦截器插件
		bean.setPlugins(new Interceptor[] {new SqlCostInterceptor() });
		try {return bean.getObject();
		} catch (Exception e) {e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
```



## 参考

- [How to Use Mybatis-PageHelper](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/HowToUse.md)
- [spring boot mybatis 打印 sql 执行时间](https://blog.csdn.net/zdyueguanyun/article/details/78980067)