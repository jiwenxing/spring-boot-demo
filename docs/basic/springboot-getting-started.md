# Getting Started
---

## 创建多 module 工程

### 创建 module 方式

右键点击父 project，new module 直接点击 next，不需要勾 create from archetype，填 artifactId 后点击 next，最关键的在这一步，直接点击 next 肯定不行。这里的 content root 默认是父 project 的目录，这里要改为 module 的目录，只需要在 content root 这一行再添加一级 module 路径即可（不需要提前创建文件夹）

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/4f016e86-1bc4-4908-bf9e-d241d08631cf)

### 版本管理

一般父pom版本号和各个子module版本保持一致，在父 module 的 properties 中定义版本号

父 pom 这样写

```xml
    <groupId>com.jverson.demo</groupId>
    <artifactId>projectA</artifactId>
    <packaging>pom</packaging>
    <version>${revision}</version>

    <properties>
        <revision>1.0.0.4.3-SNAPSHOT</revision>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <guava.version>29.0-jre</guava.version>
        <spring.boot.version>2.3.4.RELEASE</spring.boot.version>
    </properties>
```

子 pom 中不需要显式写版本号，子 module 有互相依赖的话直接使用 maven 内置变量 ${project.version}

```xml
<parent>
    <artifactId>projectA</artifactId>
    <groupId>com.sankuai.datapp.orchestration</groupId>
    <version>${revision}</version>
</parent>
<modelVersion>4.0.0</modelVersion>

<artifactId>module0</artifactId>

<dependencies>
    <dependency>
        <groupId>com.jverson.demo</groupId>
        <artifactId>module1</artifactId>
        <version>${project.version}</version>
    </dependency>
</dependencies>
```

这样当我们需要修改版本号是只需要修改一个地方即可。

但是这样会存在一个问题，deploy 之后的父 pom 文件里版本号没打进去还是个变量

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/3b0a24af-c877-4f2f-8621-d6c2c54ec0de)

这样就会导致其它依赖这些 module 的服务构建失败

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/cac6efd5-9b09-4589-93ea-de7f84b1e245)

这个问题可以使用一个插件解决 [flatten-maven-plugin](https://www.mojohaus.org/flatten-maven-plugin/usage.html)

还可以参考 [maven 版本管理与 flatten-maven-plugin](https://zhuanlan.zhihu.com/p/270574226)

### 加载资源

第一种情况，我们在做一个三方工具 jar，里面可能需要获取使用方 classpath 下的一些配置，在 Springboot 工程里可以直接写一个 starter。也可以直接这样引用

```xml
<context:property-placeholder location="classpath:config/config.properties"/>
```


第二种情况子 module 的 jar 里可能也会提供一些配置文件，使用方在使用的时候需要加载使用，举个例子

```Java
InputStream inStream = Thread.currentThread().getClassLoader().getResourceAsStream("tracer.properties"); // tracer.properties 是放在 jar 的 classpath 下
Properties properties = new Properties();
properties.load(inStream);
```

如果是 xml 配置

```xml
<import resource="classpath*:config_from_jar_classpath.xml" />
```

## ClassLoader.getResourceAsStream() 与 Class.getResourceAsStream() 区别

ClassLoader.getResourceAsStream() 类加载器默认是从 classPath 路径加载资源，并且放在 resources 下的文件加载时不能加（“/”）。例如

![](https://jverson.oss-cn-beijing.aliyuncs.com/a1107b4e3ce3d3f3352f7b91ae5bbc0c.jpg)

```Java
InputStream in = PropertiesUtil.class.getClassLoader().getResourceAsStream("xx.properties");
```

而 Class.getResourceAsStream() 默认要加载的资源路径与当前类所在包的路径一致

```Java
InputStream in = PropertiesUtil.class.getResourceAsStream("xx.properties");
```

![](https://jverson.oss-cn-beijing.aliyuncs.com/6db03f5f9fe25fc745b27972a2087303.jpg)


如果想要加载 classpath 路径下资源，则需要路径以 '/' 开头

```Java
InputStream in = PropertiesUtil.class.getResourceAsStream("/xx.properties");
```

总结：ClassLoader 直接从 classpath 下查询，不能以/开头，不能相对路径。Class 如果以/开头，直接截取/之后的路径，不以/开头，转换成包含 package 的全路径，内部还是调用的 ClassLoader 的方法。


## 给 bean name 起别名

spring xml 里可以给 bean 配置别名，这样我可以使用不同的 name 对其进行注入

```xml
<alias name="aaClient" alias="bbClient"/>
```

## 创建 Spring Boot 工程

创建一个 Spring Boot 的工程非常简单，首先使用 eclipse 创建一个普通的 maven 工程，然后在 pom 中添加一个`spring-boot-starter-parent`的节点，这是一个特殊的 starter，有以下一些特性：

1. 提供了一些有用的Maven默认配置（默认编译级别是Java1.6，默认编码是utf-8）
2. 提供了一个`dependency-management`节点，这样的话其它的相关依赖便可以省略version标记。但是其本身并不提供任何依赖
3. 恰到好处的资源过滤
4. 恰到好处的插件配置（exec插件，surefire，Git commit ID，shade）
5. 恰到好处的对 application.properties 和 application.yml 进行筛选，包括特定 profile（profile-specific）的文件


其它的Starters只简单提供开发特定类型应用所需的依赖。由于我们准备开发一个web应用，因此继续添加 spring-boot-starter-web 依赖。这时再看看 maven 的依赖树会发现 Tomcat、Spring Boot 等相关的依赖都已引入，并且就连这些库的版本也都是 Spring Boot 精选的适配做好的。注意 Spring Boot 每次发布都关联一个 Spring 框架的基础版本（这里是4.3.9），所以强烈建议你不要自己指定 Spring 版本。

![](https://jverson.oss-cn-beijing.aliyuncs.com/201707111832_191.png)

这时候的 pom 如下所示：

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<groupId>com.jverson</groupId>
	<artifactId>springboot</artifactId>
	<packaging>jar</packaging>
	<version>0.0.1-SNAPSHOT</version>
	<!-- Inherit defaults from Spring Boot -->
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>1.5.4.RELEASE</version>
	</parent>
	<name>springboot Maven Webapp</name>
	<url>http://maven.apache.org</url>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
	</dependencies>
	<!-- Package as an executable jar -->
	<build>
		<finalName>springboot</finalName>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>
</project>
```

## 不使用parent POM的情况下引入Spring Boot

处于某些原因你不能继承spring-boot-starter-parent的POM，这时候可以在pom中通过设置`scope=import`的依赖达到同样的效果如下所示：
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


## 编写代码

在`src/main/java`下创建一个helloworld.java测试类，内容如下，这时便可以直接run java application来运行程序了。

```java
@RestController
@EnableAutoConfiguration
//@SpringBootApplication 也可以，包含了 @EnableAutoConfiguration 注解
public class HelloWorld {
	@RequestMapping("/")
	String home(){
		return "hello world!";
	}
	public static void main(String[] args) {
		SpringApplication.run(HelloWorld.class, args);
	}
}
```

这里重点关注一下`@EnableAutoConfiguration`这个注解，它会根据添加的jar依赖测试你想如何配置Spring，比如spring-boot-starter-web添加了Tomcat和Spring MVC，所以auto-configuration会推测你可能想开发一个web应用，并对Spring进行相应的配置。

main方法是java标准的程序入口方法，在main中通过调用run将业务委托给了Spring Boot的SpringApplication类，SpringApplication类将引导应用启动Spring，并传递args数组以暴露所有的命令行参数。关于SpringApplication源码给出的注释是：
> Classes that can be used to bootstrap and launch a Spring application from a Java main method.By default class will perform the following steps to bootstrap your application:
> 1. Create an appropriate ApplicationContext instance
> 2. Register a CommandLinePropertySource to expose command line arguments as Spring properties
> 3. Refresh the application context, loading all singleton beans
> 4. Trigger any CommandLineRunner beans


## 创建可执行jar

所谓可行性jar即“fat jars”，它包含了编译后的类及运行所依赖jar的存档。Spring Boot允许在jar中直接内嵌jars。为了创建可执行jar，如上面pom所示需要将`spring-boot-maven-plugin`插件添加到pom.xml
中。

这时执行mvn package或者install便能看到target的目录如下所示：
![](https://jverson.oss-cn-beijing.aliyuncs.com/201707112021_277.png)

其中springboot.jar即可执行jar，而较小的springboot.jar.original文件则为Spring Boot重新打包前Maven创建的原始jar文件。

在cmd窗口执行`java -jar springboot.jar`即可启动运行程序。
