
# Different Environment Different Profile
---

## 如何实现不同环境读取不同profile？

开发过程中需要针对不同的环境配置不同的参数，之前都是通过maven提供的Profile功能在打包的时候不同环境打不同的包，现在springboot提供了运行时加载不同Profile的功能，具体的用法如下。

假设需要区分开发环境和线上环境，则创建以下三个配置文件，其中`application-dev.properties`和`application-prod.properties`中分别配置了不同环境下的一些参数，而`application.properties`则是springboot的默认配置文件，其中可以配置使用哪个环境的Profile，也可以配一些公用的配置。

![](http://
pgdgu8c3d.bkt.clouddn.com/201707182114_190.png)

其中，

application-dev.properties内容：

> car.brand = ford    
car.color = ${car.brand} red    
car.price = 30.8    

application-prod.properties内容：

> car.brand = bmw    
car.color = ${car.brand} yellow    
car.price = 55.3      

而application.properties中指定Profile：

> spring.profiles.active = prod

这时候运行便会发现car类被prod配置中的属性实例化。


## 经验

- 各个环境公共的配置写在application.properties中（也可以写一些默认的配置，如果在指定的Profile读不到可以当做默认值使用）
- 各个模块独有的配置配置在自己的application-{xxx}.properties文件中
- 程序读取的时候优先读取application.properties中选中的profile的配置，若读不到才会从application.properties去读

## 最佳实践

一般部署都是通过maven进行打包，maven的打包命令可以带参数指定Profile，因此最佳的实践是结合maven的Profile功能和springboot提供的Profiles功能，这样便不需要修改配置即可按照环境不同打不同的包，具体的实现如下：

### 首先在pom中定义不同的Profile

```xml
<profiles>
	<profile>
		<id>dev</id>
		<activation>
			<activeByDefault>true</activeByDefault>
		</activation>
		<properties>
			<spring.profiles.active>dev</spring.profiles.active>
		</properties>
	</profile>
	<profile>
		<id>prod</id>
		<properties>
			<spring.profiles.active>prod</spring.profiles.active>
		</properties>
	</profile>
</profiles>
```

### 和上图一样依然创建相应的`application-{xxx}.properties`文件，只是将application.properties的内容改为下面这样即可

> spring.profiles.active = @spring.profiles.active@

### 这时在eclipse的`Run Configuration`中新建打包命令如下，运行即可按照dev的配置打包，同理将其中的命令改为`clean install -Pprod`即可按照prod的配置打成生产包。

![](http://
pgdgu8c3d.bkt.clouddn.com/201707191521_661.png)


## 启动时打印当前执行环境

开发部署的过程中有时候可能对当前的执行环境不是很确定导致浪费一些时间，那么在启动的时候将当前的环境打印出来可以很方便的做到心里有底，具体的实现方式是在启动过程中添加以下一些代码：

```java
public static void main(String[] args) {
		ApplicationContext ctx = new SpringApplicationBuilder().sources(HelloWorld.class)
		    .bannerMode(Banner.Mode.LOG).run(args);
		String[] activeProfiles = ctx.getEnvironment().getActiveProfiles();
		for (String string : activeProfiles) {
			logger.warn("the active profile is: " + string);
		}
}
```

再次启动项目便会看到console中打印如下

![](http://
pgdgu8c3d.bkt.clouddn.com/201707191529_714.png)

## 开发环境标识

在开发或者预发布环境中一般我们需要对一些拦截器进行屏蔽，最简单的方式是在拦截器中添加

```java
// 是否开发模式，默认false
@Value(value = "${devMode:false}")
private boolean devMode;
```

然后讲需要屏蔽的逻辑写到下面

```java
if (!devMode) {
	//...
}
```

最后只需要在预发布或测试环境的配置文件中添加`devMode=true`配置即可。

## 参考

- [SPRING BOOT PROPERTIES PER MAVEN PROFILE](http://dolszewski.com/spring/spring-boot-properties-per-maven-profile/)