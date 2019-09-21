
# Spring Data JPA
---

## JPA 介绍

前面介绍过 JPA 是 sun 在 JDK5 中引入的 JPA ORM 规范，其目的在于整合 ORM 技术，统一规范和标准。目前比较成熟的 JPA 框架主要包括 Jboss 的 HibernateEntityManager、Oracle 捐献给 Eclipse 社区的 EclipseLink、Apache 的 OpenJPA 等。下面将以 Hibernate 为例介绍 JPA 的使用。

## 使用 Hibernate EntityManager

Hibernate 本身是独立于 spring 的，如果不使用 spring 框架，则 Dao 层的代码如下所示，这时还需要一个标准的 `persistence.xml` 配置文件

```java
public class UserDaoImpl implements UserDao {
	public AccountInfo save(AccountInfo accountInfo) { 
		EntityManagerFactory emf = 
		Persistence.createEntityManagerFactory("SimplePU"); 
		EntityManager em = emf.createEntityManager(); 
		em.getTransaction().begin(); 
		em.persist(accountInfo); 
		em.getTransaction().commit(); 
		emf.close(); 
		return accountInfo; 
	} 
}
```

## 使用 spring + Hibernate

如果在项目中引入了 spring，spring 对 JPA 提供了很友好的支持，可以使用自动注入而不是手动 new 实现类，配置也更为灵活，最主要的是 Spring 将 EntityManager 的创建与销毁、事务管理等代码抽取出来，并由其统一管理。事务管理和 EntityManager 创建、销毁的代码都不再需要开发者关心了。

持久层代码：

```java
@Repository("userDao") 
public class UserDaoImpl implements UserDao { 
	@PersistenceContext 
	private EntityManager em; 
	 
	@Transactional 
	public Long save(AccountInfo accountInfo) {
		em.persist(accountInfo); 
		return accountInfo.getAccountId();
	} 
}
```

配置文件：

```xml
<?xml version="1.0"encoding="UTF-8"?> 
 <beans...> 
 <context:component-scan base-package="footmark.springdata.jpa"/> 
 <tx:annotation-driven transaction-manager="transactionManager"/> 
 <bean id="transactionManager"class="org.springframework.orm.jpa.JpaTransactionManager"> 
 <property name="entityManagerFactory"ref="entityManagerFactory"/> 
 </bean> 
 <bean id="entityManagerFactory"class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean"> 
    </bean> 
 </beans>
```

测试代码：

```java
public class SimpleSpringJpaDemo{public static void main(String[] args){ 
		ClassPathXmlApplicationContext ctx = 
		new ClassPathXmlApplicationContext("spring-demo-cfg.xml"); 
		UserDao userDao = ctx.getBean("userDao", UserDao.class); 
		userDao.createNewAccount("ZhangJianPing", "123456", 1); 
	} 
}
```


## 使用 Spring Data JPA

从上边可以看到 Spring 对 JPA 的支持已经非常强大，开发者只需关心核心业务逻辑的实现代码，无需过多关注 EntityManager 的创建、事务处理等 JPA 相关的处理。但是你会发现持久层 dao 的实现逻辑还是需要手动书写。于是便出现了 `Spring Data JPA`，Spring Data JPA 可以根据规范的持久层方法名称自动生成想要的逻辑实现，或者通过继承提供的标准接口 `Repository`、`PagingAndSortingRepository`、`CrudRepository` 及 `JpaRepository` 等自动获得各种 crud 的基本方法，又或者通过注解 `@Query` 自定义一些复杂的实现。

值得注意的是 Spring-data-jpa 依赖于 Hibernate。

Spring Data JPA 在后台为持久层接口创建代理对象时，有三种方式实现持久层的功能：

1. 通过继承标准接口获得基本的 crud 方法
2. 自定义符合指定格式的方法命名，Spring Data JPA 可以根据命名自动实现其功能。
3. 在声明的方法上面使用 `@Query` 注解，并提供一个查询语句作为参数，Spring Data JPA 在创建代理对象时，便以提供的查询语句来实现其功能。或增加 `@Modifying` 注解将查询标识为修改查询（update 操作）


### 继承标准接口或自定义方法

对于 `Repository`、`PagingAndSortingRepository`、`CrudRepository` 及 `JpaRepository` 等这些持久层的接口开发的时候应该怎么选择，直接继承 `CrudRepository` 或 `PagingAndSortingRepository` 这些接口当然很方便的自动拥有了很多实现，但是这会带来一个问题，它可能暴露了你不希望暴露给业务层的方法。比如某些接口你只希望提供增加的操作而不希望提供删除的方法。针对这种情况，开发者只能退回到 Repository 接口，然后到 CrudRepository 中把希望保留的方法声明复制到自定义的接口中即可，当然在一般的简单系统（erp）中也没有那么讲究，怎么方便怎么来。

Spring Data JPA 为自定义查询提供了一些表达条件查询的关键字，大致如下：

- And --- 等价于 SQL 中的 and 关键字，比如 findByUsernameAndPassword(String user, Striang pwd)；
- Or --- 等价于 SQL 中的 or 关键字，比如 findByUsernameOrAddress(String user, String addr)；
- Between --- 等价于 SQL 中的 between 关键字，比如 findBySalaryBetween(int max, int min)；
- LessThan --- 等价于 SQL 中的 "<"，比如 findBySalaryLessThan(int max)；
- GreaterThan --- 等价于 SQL 中的 ">"，比如 findBySalaryGreaterThan(int min)；
- IsNull --- 等价于 SQL 中的 "is null"，比如 findByUsernameIsNull()；
- IsNotNull --- 等价于 SQL 中的 "is not null"，比如 findByUsernameIsNotNull()；
- NotNull --- 与 IsNotNull 等价；
- Like --- 等价于 SQL 中的 "like"，比如 findByUsernameLike(String user)；
- NotLike --- 等价于 SQL 中的 "not like"，比如 findByUsernameNotLike(String user)；
- OrderBy --- 等价于 SQL 中的 "order by"，比如 findByUsernameOrderBySalaryAsc(String user)；
- Not --- 等价于 SQL 中的 "！ ="，比如 findByUsernameNot(String user)；
- In --- 等价于 SQL 中的 "in"，比如 findByUsernameIn(Collection<String> userList) ，方法的参数可以是 Collection 类型，也可以是数组或者不定长参数；
- NotIn --- 等价于 SQL 中的 "not in"，比如 findByUsernameNotIn(Collection<String> userList) ，方法的参数可以是 Collection 类型，也可以是数组或者不定长参数；

### 使用 @Query 创建查询

@Query 注解的使用非常简单，只需在声明的方法上面标注该注解，同时提供一个 JP QL 查询语句即可

```java
public interface UserDao extends Repository<AccountInfo, Long> {
	@Query("select a from AccountInfo a where a.accountId = ?1") 
	AccountInfo findByAccountId(Long accountId); 
	 
	   @Query("select a from AccountInfo a where a.balance> ?1") 
	Page<AccountInfo> findByBalanceGreaterThan(Integer balance,Pageable pageable); 
}
```

开发者也可以通过使用 @Query 来执行一个更新操作，为此，我们需要在使用 @Query 的同时，用 @Modifying 来将该操作标识为修改查询

```java
@Modifying 
@Query("update AccountInfo a set a.salary = ?1 where a.salary < ?2") 
int increaseSalary(int after, int before);
```

删除的写法稍微有些特殊：

```java
@Transactional
int deleteByCountrycode(@Param("countrycode") String countrycode); 
```



## springboot 集成 `Spring Data JPA`

springboot 官方提供了集成 Spring Data JPA 的 starter: `spring-boot-starter-data-jpa`，该依赖集成了 `Hibernate`、`Spring Data JPA` 以及 `Spring ORMs`（Core ORM support from the Spring Framework）。使用方法非常简单。

一、添加依赖

```xml
<dependency
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

二、application.properties 配置

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/test
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.jpa.properties.hibernate.hbm2ddl.auto=create-drop
```

三、创建实体

```java
@Entity
public class User {
    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private Integer age;
    // 省略构造函数
    // 省略 getter 和 setter
}
```

四、创建数据访问接口

```java
public interface UserRepository extends JpaRepository<User, Long> {
	User findByName(String name);
    User findByNameAndAge(String name, Integer age);
    @Query("from User u where u.name=:name")
    User findUser(@Param("name") String name);
}
```

[github](https://github.com/jiwenxing/spring-boot/tree/master/spring-boot-samples/spring-boot-sample-data-jpa) 中有具体的 demo 工程实现可以参考，注意 demo 中使用的是 H2 内存数据库。


## Spring Data JPA 对事务的支持

默认情况下，Spring Data JPA 实现的方法都是使用事务的。针对查询类型的方法，其等价于 @Transactional(readOnly=true)；增删改类型的方法，等价于 @Transactional。可以看出，除了将查询的方法设为只读事务外，其他事务属性均采用默认值。

当然除了在持久层使用事务注解以外，开发者也可以在业务层方法上使用 @Transactional 指定事务属性，这主要针对一个业务层方法多次调用持久层方法的情况。持久层的事务会根据设置的事务传播行为来决定是挂起业务层事务还是加入业务层的事务。


## References

- [使用 Spring Data JPA 简化 JPA 开发](https://www.ibm.com/developerworks/cn/opensource/os-cn-spring-jpa/index.html)
- [Spring Transactional](https://docs.spring.io/spring/docs/3.0.x/spring-framework-reference/html/transaction.html#transaction-declarative-annotations)
- [JPA and ‘Spring Data’](https://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/reference/htmlsingle/#boot-features-jpa-and-spring-data)