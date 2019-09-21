# Using Spring Boot
---

由于 Spring Boot 的自动配置特性以及一些约定俗称的默认规则，在使用 Spring Boot 的时候如果能够遵循的话将会达到事半功倍的效果。

## 依赖管理

Spring Boot 所依赖的jar包版本已经在parent中的dependency-management中替你管理好了，当更新Spring Boot时，那些依赖也会一起更新，因此除非确实有必要，一般不需要在构建配置里指定这些依赖的版本。


## 如何在不使用 parent POM 的情况下使用 Spring Boot

如果因为一些原因你不想继承spring-boot-starter-parent，那么通过以下方式依然可以使用Spring Boot。
```xml
<dependencyManagement>
	<dependencies>
		<dependency>
		<!-- Import dependency management from Spring Boot -->
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-dependencies</artifactId>
		<version>1.4.1.BUILD-SNAPSHOT</version>
		<type>pom</type>
		<scope>import</scope>
		</dependency>
	</dependencies>
</dependencyManagement>
```

这里的`spring-boot-dependencies`其实就是`spring-boot-starter-parent`的parent，其中维护了一个非常全的dependencyManagement。

在`spring-boot-starter-parent`的POM中可以看到以下设置，它选择了相当保守的Java兼容策略，依然使用的java1.6，如果想换为最新的1.8版本，只需要在工程的POM添加一个属性即可。

![](https://jverson.oss-cn-beijing.aliyuncs.com/201707122206_37.png)

```xml
<properties>
	<java.version>1.8</java.version>
</properties>
```


## 代码的组织结构

Spring Boot 并没用强制的代码结构要求，但是如果按照一些默认规则可以节省很多代码。

一般root package使用一个反转的域名（com.jverson.demo e.g.），然后将main类（主配置文件及程序入口）放在这个root package中，并将`@EnableAutoConfiguration`注解到你的main类上，这样就隐式的定义了一个包搜索路径以搜索特定的注解实体（从main根目录出发搜索所有的子目录），这时`@ComponentScan`注解便不需要指定basePackage属性了，当使用`@SpringBootApplication`注解时一切保持默认即可。典型的结构如下所示：

![](https://jverson.oss-cn-beijing.aliyuncs.com/201707122221_463.png)

## 配置类

Spring Boot建议尽量使用基于java的配置（使用@Configuration注解标记为配置类），但是你并不需要将所有的@Configuration都放进一个单独的类里， 通常main方法所在的类会被定义为主配置类，通过@Import注解将其它配置类导入即可，如果你按照标准的包路径规划代码，只需要配置@ComponentScan注解即可自动收集所有的Spring组件。

有时使用第三方库不得不使用xml配置的情况下，Spring Boot提供了@ImportResource注解将xml配置文件注入到主配置类中。

Spring Boot的自动配置是其一大特点，它会根据你添加的jar依赖自动配置应用。而自动配置是通过`@EnableAutoConfiguration`开启的（@SpringBootApplication注解包含了此注解），一般只需将其添加到主配置类上即可。如果想禁用某个自动配置，可以使用该注解的exclude属性如下：
`@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})`

> 注意：@SpringBootApplication same as @Configuration @EnableAutoConfiguration @ComponentScan


## 热交换

开发的时候希望改动能够马上得以体现，`spring-boot-devtools`模块便可以提供这些development-time特性，只要classpath下的文件有变动，它就会自动重启。使用它的方法也很简单，只需要添加pom以来即可：

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-devtools</artifactId>
	<optional>true</optional>
</dependency>
```

你可能会发现devtools的热启动会比自己手动冷启动更快，这是因为它是通过两个类加载器实现的，一个是加载那些不会变动的基础类（第三方jar e.g.）加载器basic classloader，另一个是正在开发的类加载器restart classloader，热启动时只是重新加载restart classloader而已。

默认情况下classpath下的任何文件改动都会被监控并重启，而一些资源的变化不需要重启，例如vm的修改等。
