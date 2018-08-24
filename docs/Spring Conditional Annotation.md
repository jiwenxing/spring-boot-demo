
# Spring Conditional Annotation
---

`@Conditional` 注解是 Spring 4 提供的基于条件的Bean的创建方式，Spring Boot 大量利用了这个特定来实现自动配置。比如，当某一个 jar 包在一个类路径下时，自动配置一个或者多个Bean；或者只有一个Bean创建时，才会创建另一个Bean。总的来说，就是根据特定条件来控制Bean的创建行为，这样就可以利用这个特性进行一些自动配置。

<!-- more -->

## 自定义 Condition 实例

下面的示例将以不同的操作系统作为条件，通过实现Condition接口，并重写其matches方法来构造判断条件，获取在不同操作系统下的操作命令。如在Windows系统下运行程序调用获取文件列表命名的方法则输出 `dir`，如果在Linux下则输出 `ls`。

### 通过实现 Spring 提供的 `Condition` 接口创建两个 Condition 类

自定义 Condition 需要实现 `org.springframework.context.annotation.Condition` 接口中的 `boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata)` 方法，我们的条件判断逻辑则应该放在此方法中。

```java
public class WindowsCondition implements Condition {
	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		System.out.println("os.name:" + context.getEnvironment().getProperty("os.name"));
		return context.getEnvironment().getProperty("os.name").contains("Windows");
	}
}

public class LinuxCondition implements Condition {
	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		System.out.println("os.name:" + context.getEnvironment().getProperty("os.name"));
		return context.getEnvironment().getProperty("os.name").contains("Linux");
	}
}
```

### 创建获取命令的接口并分别创建  Linux 和 Windows 下的实现

```java
public interface CmdService {
	String getListCmd();	
}

public class WindowsCmdServiceImpl implements CmdService {
	@Override
	public String getListCmd() {
		return "dir";
	}
}

public class LinuxCmdServiceImpl implements CmdService {
	@Override
	public String getListCmd() {
		return "ls";
	}
}
```

### 创建配置类并使用 `@Conditional` 注解将 Bean 定义为条件创建

```java
@Configuration
public class SpringConditionalConf {
	
	@Bean
	@Conditional(WindowsCondition.class) //WindowsCondition 条件成立时创建此 Bean
	public CmdService windowsCmdService(){
		return new WindowsCmdServiceImpl();
	}
	
	@Bean
	@Conditional(LinuxCondition.class) //LinuxCondition 条件成立时创建此 Bean
	public CmdService linuxCmdService(){
		return new LinuxCmdServiceImpl();
	}
	
}
```

### 测试类

```java
@RunWith(SpringRunner.class)
@SpringBootTest(classes = HelloSpringBoot.class)
public class SpringConditionalDemoTest {

	@Autowired
	private CmdService cmdService;
	
	@Test
	public void testConditional(){
		System.out.println(cmdService.getListCmd());
	}
	
}
```

在 windows 主机上运行测试得到输出 `dir`，说明只有 `WindowsCmdServiceImpl` 实例被创建并且得到了正确的指令。


## `@Conditional` 常见使用方法

在上例中 `@Conditional` 注解标注到 `@Bean` 的方法上；除此之外还可以作为类级别的注解放在注标识有@Component（包含@Configuration）的类上，这时所有标识了 `@Bean` 的方法和 `@Import` 注解导入的相关类将遵从这些条件；最后还可以作为一个 meta-annotation，组成自定义注解。

## Spring 内置 Conditional

除了类似上例的自定义 Condition 之外，Spring 还内置了一些 Condition 给我们使用：

- `@ConditionalOnBean` 仅仅在当前上下文中存在某个对象时，才会实例化一个Bean
- `@ConditionalOnClass` 某个class位于类路径上，才会实例化一个Bean
- `@ConditionalOnExpression` 当表达式为true的时候，才会实例化一个Bean
- `@ConditionalOnMissingBean` 仅仅在当前上下文中不存在某个对象时，才会实例化一个Bean
- `@ConditionalOnMissingClass` 某个class类路径上不存在的时候，才会实例化一个Bean
- `@ConditionalOnNotWebApplication` 不是web应用时才会实例化一个Bean

## `@Conditional` 与 `@Profile ` 区别

Spring 3.1 推出的 @Profiles 注解功能与 @Conditional 注解类似，都是提供一种 “If-Then-Else” 能力，即条件配置功能。但是更早出现的 @Profiles 主要是用来根据不同的运行环境加载不同的应用配置；@Conditional 注解是一种更高层次的实现，他没有 @Profile 注解的一些限制，是 @profile 的一种更加通用的版本，其主要被用作 Bean 的条件加载。

@Profile 注解使用举例

```java
@Profile("Development")
@Configuration
public class DevDatabaseConfig implements DatabaseConfig {
    @Override
    @Bean
    public DataSource createDataSource() {
        System.out.println("Creating DEV database");
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        /*
         * Set MySQL specific properties for Development Environment
         */
        return dataSource;
    }
}

@Profile("Production")
@Configuration
public class ProductionDatabaseConfig implements DatabaseConfig {
    @Override
    @Bean
    public DataSource createDataSource() {
        System.out.println("Creating Production database");
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        /*
         * Set ORACLE specific properties for Production environment
         */
        return dataSource;
    }
}
```

以上两个配置类都实现了 DatabaseConfig 接口，特殊的地方在于它们都用 @Profile 标注，被 @Profile 标注的组件只有当指定profile值匹配时才生效。可以通过以下方式设置profile值：

1. 设置spring.profiles.active属性（通过JVM参数、环境变量或者web.xml中的Servlet context参数）
2. ApplicationContext.getEnvironment().setActiveProfiles(“ProfileName”)

在 Spring 3.x 里 @Profiles 注解只能用在类级别，但是在 Spring 4.0 以后则既可以用在类级别也可以用在方法级别，主要是因为在 4.0 中 Spring 使用 @Conditional 对其做了重构：

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional(ProfileCondition.class)  // 使用 @Conditional 实现 @Profile
public @interface Profile {
	String[] value();
}
```


## `@Conditional` 注解使用场景

我们知道在 Spring Boot 中大量使用了 `@Conditional` 注解，我们在平时的开发中如果遇到以下一些场景则可以考虑使用该注解:

- Condition whether a property is available or not using Environment variables, irrespective of its value.
- Like Profiles, Condition whether a property value is available or not using Environment variables.
- Conditions based on a Bean definition are present in Spring Application context.
- Conditions based on a Bean object are present in Spring Application context.
- Conditions based on some or all Bean properties values.
- Conditions based on some Resources are present in current Spring Application Context or not.
- Conditions based on Bean’s Annotations
- Conditions Bean’s Method’s Annotations.
- Conditions based on Bean’s Annotation’s parameter values
- Conditions based on  Bean’s Method’s Annotation’s parameter values.

## 参考

- [Spring @Conditional Annotation](https://javapapers.com/spring/spring-conditional-annotation/)
- [Spring4.0系列5-@Conditional](http://wiselyman.iteye.com/blog/2002449)

