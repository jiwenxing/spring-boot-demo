# Bean Configuration by Properties Files
---

使用外部 properties 配置文件配置 Bean 有多种方式，这里主要介绍两种方式：@Value 注解方式及 @ConfigurationProperties 注解方式

## @Value 注解方式

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

## 给枚举属性赋值

枚举其实类似于静态属性，无法直接通过注解给枚举的某个属性赋值，如果希望枚举的属性通过配置文件来配置，可以创建一个 spring 管理的静态内部类，通过 `@PostConstruct` 将 `@Value` 注入的属性设置到枚举中，其实和上面提到的使用 postConstruct 给静态变量赋值是一个道理。

下面的例子中就希望枚举的各级审核对应的金额上限能够通过配置文件配置的方式抽取出来，实现如下：

```java
public enum AppendStatusEnum {

    AUDIT_STEP1(10,"待初审"),
    AUDIT_STEP2(12,"待二审"),
    AUDIT_STEP3(13,"待三审"),
    ;

    //通过配置文件设置审核金额阈值
    @Component
    public static class PriceConfig{
        @Value("${auditStep1MaxPrice}")
        private Integer auditStep1MaxPrice;
        @Value("${auditStep2MaxPrice}")
        private Integer auditStep2MaxPrice;
        @PostConstruct
        public void postConstruct() {
            AUDIT_STEP1.setPriceThreshold(auditStep1MaxPrice);
            AUDIT_STEP2.setPriceThreshold(auditStep2MaxPrice);
            System.out.println("审核金额阈值设置完成！auditStep1MaxPrice = "
                    + auditStep1MaxPrice + ", auditStep2MaxPrice = " + auditStep2MaxPrice);
        }
    }

    private Integer value = null;
    private String name = null;
    private Integer priceThreshold = null; //当前流程处理的金额上限，希望能抽取到配置文件中

    private AppendStatusEnum(Integer value, String name){
        this.value = value;
        this.name = name;
    }
    public Integer getPriceThreshold() {
        return priceThreshold;
    }

    public void setPriceThreshold(Integer priceThreshold) {
        this.priceThreshold = priceThreshold;
    }

    //getter & setter

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