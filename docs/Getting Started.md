### 创建Spring Boot工程

创建一个Spring Boot的工程非常简单，首先使用eclipse创建一个普通的maven工程，然后在pom中添加一个`spring-boot-starter-parent`的节点，这是一个特殊的starter，有以下一些特性：

1. 提供了一些有用的Maven默认配置（默认编译级别是Java1.6，默认编码是utf-8）
2. 提供了一个`dependency-management`节点，这样的话其它的相关依赖便可以省略version标记。但是其本身并不提供任何依赖
3. 恰到好处的资源过滤
4. 恰到好处的插件配置（exec插件，surefire，Git commit ID，shade）
5. 恰到好处的对 application.properties 和 application.yml 进行筛选，包括特定profile（profile-specific）的文件


其它的Starters只简单提供开发特定类型应用所需的依赖。由于我们准备开发一个web应用，因此继续添加spring-boot-starter-web依赖。这时再看看maven的依赖树会发现Tomcat、Spring Boot等相关的依赖都已引入，并且就连这些库的版本也都是Spring Boot精选的适配做好的。注意Spring Boot每次发布都关联一个Spring框架的基础版本（这里是4.3.9），所以强烈建议你不要自己指定Spring版本。

![](http://7xry05.com1.z0.glb.clouddn.com/201707111832_191.png)

这时候的pom如下所示：

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

### 不使用parent POM的情况下引入Spring Boot

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


### 编写代码

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


### 创建可执行jar

所谓可行性jar即“fat jars”，它包含了编译后的类及运行所依赖jar的存档。Spring Boot允许在jar中直接内嵌jars。为了创建可执行jar，如上面pom所示需要将`spring-boot-maven-plugin`插件添加到pom.xml
中。

这时执行mvn package或者install便能看到target的目录如下所示：
![](http://7xry05.com1.z0.glb.clouddn.com/201707112021_277.png)

其中springboot.jar即可执行jar，而较小的springboot.jar.original文件则为Spring Boot重新打包前Maven创建的原始jar文件。

在cmd窗口执行`java -jar springboot.jar`即可启动运行程序。
