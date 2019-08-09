# Bean Configuration by Properties Files
---

使用外部 properties 配置文件配置 Bean 有多种方式，这里主要介绍两种方式：@Value 注解方式及 @ConfigurationProperties 注解方式

## @Value 注解方式

这也是之前常用的一种方式，代码示例如下：

```java
@PropertySource("classpath:person.properties")
@Component
public class Person2 {@Value("${person.firstName}")
	String firstName;
	@Value("${person.lastName}")
	String lastName;
	@Value("${person.age}")
	int age;
	@Value("${person.salary}")
	double salary;

    //getters and setters...
}
```

## @Value 给静态变量赋值

如果变量是 static 修饰的，使用上面介绍的方法无法进行赋值，因为 Spring Boot 不允许/不支持把值注入到静态变量中，但是却支持通过给 set 方法注解的方式给静态变量赋值。

```Java
public static boolean devMode;

@Value(value = "${devMode:false}")
public void setDevMode(final boolean devMode) {
    ApplicationConf.devMode = devMode;
}
```

这里有两个关键的注意点：

1. 配置文件类必须使用 @Component 注解，纳入 spring 容器，这样注解才能生效
2. 使用 @Value 在 set 方法上添加注解，**特别注意 set 方法不能使用 static 进行修饰**

这种方式相对于之前使用 postConstruct 的方式稍微更优雅一些，之前是这样的：

```Java
@Component
public class ApplicationConf {	
	/**
	 * 是否开发模式
	 */
	@Value(value = "${devMode:false}")
	private boolean devModeConf;
	public static boolean devMode;
	
	@PostConstruct
	public void init() {
		devMode = devModeConf;
	}
	
}
```


## @ConfigurationProperties 注解方式

@Value 方式代码感觉有些凌乱，下面这种方式会更简洁一些

```java
@PropertySource("classpath:person.properties")
@ConfigurationProperties(prefix ="person", ignoreUnknownFields = false)
@Component
public class Person {
	String firstName;
	String lastName;
	int age;
	double salary;

	//getters and setters...
}
```

注意：两种方式的 properties 文件都位于 classpath 根路径下，内容如下：

> person.age = 22    
person.firstName = jverson    
person.lastName = jj    
person.salary = 11111111    

其中 `ignoreUnknownFields = false` 表示当属性不匹配时抛出异常

## 使用基于 java 的配置

以上两种方式都是直接在 Bean 的定义中注入属性配置，如果多个 Bean 的定义在同一个配置文件中，可以使用基于 Java 的配置定义实现 Bean 的定义如下：

```java
@Configuration
@PropertySource("classpath:person.properties")
public class BeanConf {@Bean("person")
	@ConfigurationProperties(prefix ="person")
	public Person person(){return new Person();
	}
	
	@Bean("person2")
	@ConfigurationProperties(prefix ="person2")
	public Person2 person2() {return new Person2();
	}
}
```

当然这里也可以是同一个类的两个实例，这样就只需要在配置类中引入配置文件即可，基于 java 的配置也是 spring boot 鼓励的形式。