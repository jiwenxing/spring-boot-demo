# Using FreeMaker as Template Engine
---

## velocity 废弃之后怎么办

Velocity被spring boot废弃以后我们要做的不是去寻找额外的方法使其再次支持velocity（这样显然会花费更多的功夫并不会有好的效果），而应该积极的寻找一个最好的替代方案。从springboot1.5.4官方文档及网上的[一些资料](https://dzone.com/articles/template-engines-review-after-deprecated-velocity)来看FreeMaker都是一个很不错的选择，其受到Apache软件基金会的资助并且在Apache的众多项目中广泛应用，起码不会很快又被废弃掉。

![](http://
pgdgu8c3d.bkt.clouddn.com/201707241025_560.png)

## 使用 FreeMaker

FreeMaker在springboot中的用法几乎一样

1. pom中添加FreeMaker依赖
```xml
<!-- Spring Boot Freemarker 依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-freemarker</artifactId>
</dependency>
```

2. 创建一个*.ftl模板文件
```html
<html>  
<body>  
  <h1>亲爱的${name}，你好！您正在使用Freemaker模板</h1>    
</body>  
</html>  
```

3. Controller 写法和vm完全一样


这时候启动工程应该就已经能够访问了，其它的工作springboot已经帮你配置好了。如果需要自定义一些配置只需在application.properties中设置即可。

> spring.freemarker.template-loader-path=classpath:/templates/     
spring.freemarker.cache=false    
spring.freemarker.charset=UTF-8    
spring.freemarker.check-template-location=true    
spring.freemarker.content-type=text/html    
spring.freemarker.expose-request-attributes=true    
spring.freemarker.expose-session-attributes=true    
spring.freemarker.request-context-attribute=request    
spring.freemarker.suffix=.ftl    


## 参考

- [A Review of Template Engines: What Next After Velocity?](https://dzone.com/articles/template-engines-review-after-deprecated-velocity)