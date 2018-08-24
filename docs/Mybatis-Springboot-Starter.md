# MyBatis-Spring-Boot-Starter
---

[MyBatis-Spring-Boot-Starter](http://www.mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)是mybatis为springboot提供的快速集成的方案（因为springboot太火了），原话是The MyBatis-Spring-Boot-Starter help you build quickly MyBatis applications on top of the Spring Boot。因此如果项目中使用springboot和mybatis的话，这个starter可以大大的简化你的工作。

## 添加依赖

用法如同其它的starter一样，添加starter的依赖以后，关于数据库的几乎所有必要依赖都已注入（包括tomcat-jdbc连接池、mybatis自动配置等），如果使用mysql的话还需要单独添加相关依赖，如下：
```xml
<!-- Spring Boot mybatis 依赖(包含了jdbc、tomcat-jdbc连接池、mybatis等各种所需的依赖) -->
<dependency>
	<groupId>org.mybatis.spring.boot</groupId>
	<artifactId>mybatis-spring-boot-starter</artifactId>
	<version>1.3.0</version>
	<exclusions>
		<exclusion>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter</artifactId>
		</exclusion>
	</exclusions>
</dependency>
<!-- MySQL JDBC Type 4 driver -->
<dependency>
	<groupId>mysql</groupId>
	<artifactId>mysql-connector-java</artifactId>
</dependency>
```

## 添加配置

> spring.datasource.url=jdbc:MySql://192.168.192.125:3358/spring_boot_test?useUnicode=true    
spring.datasource.username=root  
spring.datasource.password=root  
spring.datasource.driver-class-name=com.mysql.jdbc.Driver  

springboot会自动使用配置创建DataSource，然后通过SqlSessionFactoryBean将DataSource传入构造SqlSessionFactory实例。而SqlSessionFactory是mybatis的核心，类似一种连接池管理类，每次的数据操作都将由连接池来分配连接后进行。

## 创建mapper（dao）
```java
@Mapper
public interface UserMapper {
	@Select("SELECT * FROM USER WHERE name = #{name}")
	List<User> findByName(@Param("name") String name);

	@Insert("INSERT INTO user(name,age) VALUES(#{name}, #{age})")
	void insert(User user);
	
	@Update("UPDATE user SET name = #{name}, age = #{age} WHERE id = #{id}")
	void update(User user);
}
```

注意这里有两种方式实现对象方法与数据库操作sql之间的映射，注解方式和xml配置方式，相对传统的xml配置方式，注解方式要清爽很多。但是这只针对于简单语句来说，Java 注解对于稍微复杂的语句就会力不从心并且会显得更加混乱。因此，如果你需要做很复杂的事情，那么最好使用 XML 来映射语句。

## 单元测试

对于注解方式spring注入Mapper便可以直接调用了。
```java
@RunWith(SpringRunner.class)
@SpringBootTest(classes = HelloSpringBoot.class)
public class MybatisTest {

	@Autowired UserMapper userMapper;
	
	@Test
	public void testInsert(){
		User user = new User();
		user.setName("kobe");
		user.setAge(41);
		userMapper.insert(user);
	}
	
	@Test
	public void testQuery(){
		assertTrue(userMapper.findByName("kobe").size()==1);
	}
	
	@Test
	public void testQueryAll(){
		assertTrue(userMapper.getAll().size()>0);
	}
	
	@Test
	public void testUpdate(){
		User user = new User();
		user.setId(1L);
		user.setName("james");
		userMapper.update(user);
		assertTrue(userMapper.findByName("james").size()==1);
	}
	
}
```
其中HelloSpringBoot为程序的主入口类。


## xml配置方式

对于传统的xml配置方式，需要创建一个xml配置文件将mapper接口方法与对应的sql映射起来，之前的项目一直用这种方式，已经很熟悉了。
`UserMapper.xml`
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="com.jverson.springboot.mapper.UserMapper" >
    <resultMap id="BaseResultMap" type="com.jverson.springboot.bean.User" >
        <id column="id" property="id" jdbcType="BIGINT" />
        <result column="name" property="name" jdbcType="VARCHAR" />
        <result column="age" property="age" jdbcType="INTEGER" />
    </resultMap>
    
    <sql id="Base_Column_List" >
        id, name, age
    </sql>

    <select id="getAll" resultMap="BaseResultMap"  >
       SELECT 
       <include refid="Base_Column_List" />
	   FROM user
    </select>

    <select id="getOne" parameterType="java.lang.Long" resultMap="BaseResultMap" >
        SELECT 
       <include refid="Base_Column_List" />
	   FROM user
	   WHERE id = #{id}
    </select>

    <insert id="insert" parameterType="com.jverson.springboot.bean.User" >
       INSERT INTO 
       		user
       		(name,age) 
       	VALUES
       		(#{name}, #{age})
    </insert>
    
    <update id="update" parameterType="com.jverson.springboot.bean.User" >
       UPDATE 
       		user 
       SET 
       	<if test="name != null">name = #{name},</if>
       	<if test="age != null">age = #{age}</if>
       WHERE 
       		id = #{id}
    </update>
    
    <delete id="delete" parameterType="java.lang.Long" >
       DELETE FROM
       		 user 
       WHERE 
       		 id =#{id}
    </delete>
</mapper>
```

另外对于xml方式配置文件中还需要增加一些mybatis的配置以指定配置文件
> mybatis.config-locations=classpath:mybatis/mybatis-config.xml  
mybatis.mapper-locations=classpath:mapper/*.xml  
mybatis.type-aliases-package=com.jverson  

其中mybatis-config.xml中定义了一些别名
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN" "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
	<typeAliases>
		<typeAlias alias="Integer" type="java.lang.Integer" />
		<typeAlias alias="Long" type="java.lang.Long" />
		<typeAlias alias="HashMap" type="java.util.HashMap" />
		<typeAlias alias="LinkedHashMap" type="java.util.LinkedHashMap" />
		<typeAlias alias="ArrayList" type="java.util.ArrayList" />
		<typeAlias alias="LinkedList" type="java.util.LinkedList" />
	</typeAliases>
</configuration>
```



## 更新操作属性为空的情况
当更新操作传入的对象某些属性为空时希望该字段不会被更新，及updateSelective的功能，在传统使用配置文件的方式中使用判断既可以解决，但是在注解的方式中还不知道要怎么去写，不过后面会介绍更加好用的通用mapper则封装有现成的方法`updateSelective`以供使用。

## 日志显示控制

日志会自动打印执行的sql语句及参数，但是为debug级别，如果开发的时候需要观察sql的执行过程可以在配置文件中将Mapper所在的包的日志级别指定为debug

> logging.level.com.jverson.springboot.mapper=DEBUG

这时的日志显示如下所示，可以看到其他部分的日志依然是配置文件中配的INFO级别，但是mapper包中执行sql的debug日志也显示出来了。

![](http://7xry05.com1.z0.glb.clouddn.com/201708211740_408.pn，可以看到其他部分的日志依然是配置文件中配的INFO级别，但是mapper包中执行sql的debug日志也显示出来了。)

## 扩展-关于Mybatis的SqlSessionFactory

每个基于 MyBatis 的应用都是以一个 SqlSessionFactory 的实例为中心的。SqlSessionFactory 的实例可以通过 SqlSessionFactoryBuilder 获得。而 SqlSessionFactoryBuilder 则可以从 XML 配置文件或一个预先定制的 Configuration 的实例构建出 SqlSessionFactory 的实例。

### 使用xml构建SqlSessionFactory

```java
String resource = "org/mybatis/example/mybatis-config.xml";
InputStream inputStream = Resources.getResourceAsStream(resource);
SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
```
 xml简单配置示例，其中mappers 元素则是包含一组 mapper 映射器（这些 mapper 的 XML 文件包含了 SQL 代码和映射定义信息）。

 ```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
  PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
  <environments default="development">
    <environment id="development">
      <transactionManager type="JDBC"/>
      <dataSource type="POOLED">
        <property name="driver" value="${driver}"/>
        <property name="url" value="${url}"/>
        <property name="username" value="${username}"/>
        <property name="password" value="${password}"/>
      </dataSource>
    </environment>
  </environments>
  <mappers>
    <mapper resource="org/mybatis/example/BlogMapper.xml"/>
  </mappers>
</configuration>
```

### 不使用xml构建SqlSessionFactory

```java
DataSource dataSource = BlogDataSourceFactory.getBlogDataSource();
TransactionFactory transactionFactory = new JdbcTransactionFactory();
Environment environment = new Environment("development", transactionFactory, dataSource);
Configuration configuration = new Configuration(environment);
configuration.addMapper(BlogMapper.class);
SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
```

但需要注意的是，configuration 添加了一个映射器类（mapper class）。映射器类是 Java 类，它们包含 SQL 映射语句的注解从而避免了 XML 文件的依赖。但是**由于 Java 注解的一些限制加之某些 MyBatis 映射的复杂性，XML 映射对于大多数高级映射（比如：嵌套 Join 映射）来说仍然是必须的。有鉴于此，如果存在一个对等的 XML 配置文件的话，MyBatis 会自动查找并加载它。**


有了 SqlSessionFactory ，顾名思义，我们就可以从中获得 SqlSession 的实例了。SqlSession 完全包含了面向数据库执行 SQL 命令所需的所有方法。你可以通过 SqlSession 实例来直接执行已映射的 SQL 语句。

```java
@Autowired SqlSessionFactory sqlSessionFactory;
	
@Test
public void testInsert(){
	SqlSession session = sqlSessionFactory.openSession();
	UserMapper userMapper = session.getMapper(UserMapper.class);
	User user = new User();
	user.setName("kobe");
	user.setAge(41);
	try {
		userMapper.insert(user);
	} finally {
		session.close();
	}
}
```

通常通过上面的形式从sqlSession中获取mapper来执行相应的方法，这样相对来说依然比较繁琐，而在MyBatis-Spring-Boot-Starter中，它会自动通过SqlSessionFactory创建一个SqlSessionTemplate实例，扫描所有的mapper将其与SqlSessionTemplate关联，并**将所有的mapper注册到spring的容器中**，这样在使用MyBatis-Spring-Boot-Starter时，就可以不用像上面那么麻烦，直接注入mapper即可。


## 如何选择

注解方式对于简单的场景开发更加高效，不需要映射配置文件，但是对于稍微复杂的sql场景则不够灵活，力不从心。xml方式可以满足各种使用场景，但是繁琐的映射配置真的烦人，好在后面会介绍基于mybatis的通用mapper结合了两种方式的有点，灵活并且不需要配置。

选择何种方式以及映射语句的定义的一致性对你来说有多重要这些完全取决于你和你的团队。换句话说，永远不要拘泥于一种方式，你可以很轻松的在基于注解和 XML 的语句映射方式间自由移植和切换。

## 参考

- [Examples for MyBatis-Spring-Boot-Starter](https://github.com/ityouknow/spring-boot-examples)
- [mybatis-spring-boot-autoconfigure](http://www.mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)
- [mybatis-getting-started](http://www.mybatis.org/mybatis-3/zh/getting-started.html)