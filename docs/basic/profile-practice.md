# Springboot 中 Profiles 最佳实践
---

上一篇 [Profiles + Maven 不同环境读取不同配置](https://jverson.com/spring-boot-demo/profile-maven.html) 中已经大概介绍了基本的 Profiles 用法，可以胜任大多数场景。我们可能在实际应用中面临更加复杂的情况，比如最近在做的组件化就要求同一套代码能够支持多站点，不同站点不同分组（或不同机房），存在以下情况

- 每个站点之间既有共同的配置又有独立的配置
- 不同站点不同 jar 依赖，不同的 Bean 实例化配置，部署 Site A 不要有 Site B 的依赖，也不要初始化只有 Site B 才使用的 Bean
- 区分测试、预发、线上配置
- properties 文件需要拆分维护，之前的 application-{xxx}.properties 方式不再适用


## properties 文件组织方式

一般我们会将不同类型的 properties 文件分开维护，例如 alias.properties 负责维护别名，而 important.properties 则负责维护数据库配置。不同站点配置不同，不同环境配置也不同。

我们可以在 classpath 下新建 profiles 文件夹，分别在不同目录下维护不同的配置，目录结构如下所示：

```
/src/main/resources
├── application.properties
├── TEST
│   ├── alias.properties
│   ├── reply.properties
│   ├── sso.properties
│   └── ucc.properties
├── TH
│   ├── alias.properties
│   ├── reply.properties
│   ├── sso.properties
│   └── ucc.properties
└── US
    ├── alias.properties
    ├── reply.properties
    ├── sso.properties
    └── ucc.properties  
```

然后我们使用 xml 的方式配置 PropertyPlaceholderConfigurer，使其按照指定环境加载不同配置如下

```xml
	<bean class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
        <property name="locations">
            <list>
                <value>classpath:profile/${spring.profiles.active}/alias.properties</value>
				<value>classpath:profile/${spring.profiles.active}/sso.properties</value>
				<value>classpath:profile/${spring.profiles.active}/uim.properties</value>
				<value>classpath:profile/${spring.profiles.active}/ucc.properties</value>
            </list>
        </property>
    </bean>
```


最后在 application.properties 中指定当前环境的的 profile 即可，例如测试环境指定为 TEST，然后在泰国预发环境配置中指定为 TH，泰国的线上同样指定为 TH，只需要将预发和线上有区分的配置文件在实例分组配置中配置一下覆盖当前 profile 配置即可。

```
spring.profiles.active=TEST
```

## 不同 profile 加载不同 Bean

可以借助 Spring 自带的 profile 功能实现 Bean 的条件加载，在 JavaConfig 和 xml 中实现分别如下

### JavaConfig 配置 Bean

指定环境配置指定bean，使用注解`@Profile({"TH","TEST"})`，示例如下


```java
/**
 * 泰国站 独有 配置服务
 */
@Configuration
@Profile({"TH","TEST"})
public class THConf {

	@Value("${ucc.env}")
	private String env;
	
	@Bean("thConfigService")
	public ConfigService UCCConf() {
		ConfigServiceImpl configService = new ConfigServiceImpl();
		configService.setEnv(env);
		configService.init();
		return configService;
	}
}
```

`@Profile` 其实就是 Spring 的 Condition 注解， 之前的文章 [Spring Conditional Annotation](https://jverson.com/spring-boot-demo/Spring%20Conditional%20Annotation.html) 有介绍过，里面包含`@Conditional(ProfileCondition.class)` 注解，其中 ProfileCondition 实现如下

```java
class ProfileCondition implements Condition {

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		MultiValueMap<String, Object> attrs = metadata.getAllAnnotationAttributes(Profile.class.getName());
		if (attrs != null) {
			for (Object value : attrs.get("value")) {
				if (context.getEnvironment().acceptsProfiles(Profiles.of((String[]) value))) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

}
```

这里 Condition 接口是一个函数式接口，即 FunctionInterface

```java
@FunctionalInterface
public interface Condition {

	/**
	 * Determine if the condition matches.
	 * @param context the condition context
	 * @param metadata metadata of the {@link org.springframework.core.type.AnnotationMetadata class}
	 * or {@link org.springframework.core.type.MethodMetadata method} being checked
	 * @return {@code true} if the condition matches and the component can be registered,
	 * or {@code false} to veto the annotated component's registration
	 */
	boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata);

}
```

### xml 配置 Bean

根据 profile 加载指定实例

```xml
<!-- 泰国站独有配置 -->
<beans profile="TH,TEST">
    <bean id="configService" class="com.jverson.web.conf.ConfigServiceImpl"
		init-method="init">
		<property name="env" value="${ucc.env}"></property>
	</bean>		
</beans>
```

## 不同 profile 加载不同 jar 依赖

这个实现就只能依赖 Maven 提供的 profile 能力，这个在上一篇中介绍过了，就是具体的 profile 不再 application.properties 中指定，而是通过 Maven package 时参数指定 profile 来指定，这时 application.properties 中配置变成这样

> spring.profiles.active = @spring.profiles.active@

maven 配置类似这样


```xml
<profiles>
	<profile>
		<id>TEST</id>
		<activation>
			<activeByDefault>true</activeByDefault>
		</activation>
		<properties>
			<spring.profiles.active>dev</spring.profiles.active>
		</properties>
		<dependencies>
			<dependency>
				<groupId>com.jverson</groupId>
				<artifactId>ucc-test</artifactId>
			</dependency>
	    </dependencies>
	</profile>
	<profile>
		<id>TH</id>
		<properties>
			<spring.profiles.active>prod</spring.profiles.active>
		</properties>
		<dependencies>
			<dependency>
				<groupId>com.jverson</groupId>
				<artifactId>ucc-th</artifactId>
			</dependency>
	    </dependencies>		
	</profile>
	<profile>
		<id>US</id>
		<properties>
			<spring.profiles.active>prod</spring.profiles.active>
		</properties>
		<dependencies>
			<dependency>
				<groupId>com.jverson</groupId>
				<artifactId>ucc-us</artifactId>
			</dependency>
	    </dependencies>		
	</profile>	
</profiles>
```

打包时将其中的命令改为 `clean install -PTH` 即可按照 TH 的配置打成生产包。

![](https://jverson.oss-cn-beijing.aliyuncs.com/201707191521_661.png)


## 总结

没有哪一种形式可以作为万金油适用任何场景，我们在面对具体业务的时候根据情况选择合适的方式即可，前提是需要对这些方式有充分的了解，才能做出更合理的选择。
