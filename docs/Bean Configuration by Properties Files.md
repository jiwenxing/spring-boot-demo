使用外部properties配置文件配置Bean有多种方式，这里主要介绍两种方式：@Value注解方式及@ConfigurationProperties注解方式

### @Value注解方式

这也是之前常用的一种方式，代码示例如下：

```java
@PropertySource("classpath:person.properties")
@Component
public class Person2 {
	@Value("${person.firstName}")
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

### @ConfigurationProperties注解方式

@Value方式代码感觉有些凌乱，下面这种方式会更简洁一些

```java
@PropertySource("classpath:person.properties")
@ConfigurationProperties(prefix = "person", ignoreUnknownFields = false)
@Component
public class Person {
	String firstName;
	String lastName;
	int age;
	double salary;

	//getters and setters...
}
```

注意：两种方式的properties文件都位于classpath根路径下，内容如下：

> person.age = 22    
person.firstName = jverson    
person.lastName = jj    
person.salary = 11111111    

其中`ignoreUnknownFields = false`表示当属性不匹配时抛出异常

### 使用基于java的配置

以上两种方式都是直接在Bean的定义中注入属性配置，如果多个Bean的定义在同一个配置文件中，可以使用基于Java的配置定义实现Bean的定义如下：

```java
@Configuration
@PropertySource("classpath:person.properties")
public class BeanConf {
	@Bean("person")
	@ConfigurationProperties(prefix = "person")
	public Person person(){
		return new Person();
	}
	
	@Bean("person2")
	@ConfigurationProperties(prefix = "person2")
	public Person2 person2() {
		return new Person2();
	}
}
```

当然这里也可以是同一个类的两个实例，这样就只需要在配置类中引入配置文件即可，基于java的配置也是spring boot鼓励的形式。