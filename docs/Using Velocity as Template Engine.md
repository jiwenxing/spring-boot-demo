
工作以来服务端的模板引擎一直使用的是Velocity，直到最近在试用Spring Boot 1.5的时候突然发现用不了了，上网一查原来是SpringBoot从1.5版本将1.4中标记为`deprecated`的方法和类直接移除掉了（移除上个版本废弃的内容也是Spring Boot的一贯做法）。

![](http://7xry05.com1.z0.glb.clouddn.com/201707211809_650.jpg)

然后再看看1.4的Release Note，发现velocity被废弃赫然在列（而且是第一条）。

![](http://7xry05.com1.z0.glb.clouddn.com/201707211812_927.jpg)

不过在使用1.4.x版本的spring boot时还是支持的，而且现有的代码基本也都是基于velocity，因此有必要熟悉一下velocity的集成方法。


### 更新Spring Boot的版本

之前的示例都是基于1.5.4版本，在本例中为了使用velocity，需要将版本改回为1.4.5（浪费了我很多时间发现这个问题），并添加velocity的依赖

```xml
<!-- Inherit defaults from Spring Boot -->
<parent>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-parent</artifactId>
	<version>1.4.5.RELEASE</version>
</parent>

<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-velocity</artifactId>
</dependency>
```

### 创建一个controller

```java
@Controller
public class HelloController {
	@RequestMapping(value="hello.html", method={RequestMethod.GET, RequestMethod.POST}, produces = { "text/html;charset=UTF-8" })
	public String Hello(Model model){
		model.addAttribute("name", "jverson");
		return "welcome";
	}
}
```

### 在src/main/resources/templates目录下创建一个vm文件

```html
<html>  
<body>  
  <h1>hello, ${name}</h1>    
</body>  
</html>  
```

![](http://7xry05.com1.z0.glb.clouddn.com/201707211829_224.png)

这时候不出意外启动程序便可以访问这个页面了。

### 其它配置

可以在`application.properties`中对velocity做一些配置（上面的示例使用默认配置即可正常工作），例如：
> -----------velocity configurations----------    
spring.velocity.charset = UTF-8    
spring.velocity.properties.input.encoding = UTF-8    
spring.velocity.properties.output.encoding = UTF-8    
spring.velocity.resource-loader-path = classpath:/templates/    
spring.velocity.suffix=.vm    
spring.velocity.toolbox-config-location = /WEB-INF/toolbox.xml    