
# Spring Boot 应用构建 war 包
---

Spring Boot 应用一般是打包成可执行 jar 运行部署，如果需要打包成 war 包的话怎么做呢？以下两种方式供参考

## 配置 `failOnMissingWebXml` 为 false

如下所示首先将打包的插件改为 `maven-war-plugin`

```xml
<build>
  <plugins>
    <!-- <plugin>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-maven-plugin</artifactId>
    </plugin> -->
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-war-plugin</artifactId>
    </plugin>
  </plugins>
</build>
```

但需要注意的是由于 Spring Boot 应用没有 web.xml，而 war 应用找不到此文件默认会报错，需要添加一行配置 `<failOnMissingWebXml>false</failOnMissingWebXml>`，但其实 Spring Boot 已经替你做了这件事，在 `spring-boot-starter-parent` 的 pom 里的 `pluginManagement` 中已经对打包插件做了配置如下，因此直接引用即可，无需单独做任何配置，eclipse 的原因可能需要执行 maven->update project 几次才能消除报错。

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-war-plugin</artifactId>
  <configuration>
    <failOnMissingWebXml>false</failOnMissingWebXml>
    <archive>
      <manifest>
        <mainClass>${start-class}</mainClass>
        <addDefaultImplementationEntries>true</addDefaultImplementationEntries>
      </manifest>
    </archive>
  </configuration>
</plugin>
```

## 升级 `maven-war-plugin` 插件版本至 `3.0.0`

版本 3.0.0 的插件 web.xml 不存在问题，所以可以通过升级插件来解决问题

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-war-plugin</artifactId>
  <version>3.0.0</version>
</plugin>
```


## 总结

** 建议使用第一种方式，不需要配版本号，springboot 已经做好了配置。**

另外不建议使用 SpringBoot 发布 war 包，

1. 默认的 SpringBoot 支持静态资源以 jar 包的方式发布部署

2. 应用前后端分离成为趋势，后端使用 SpringBoot 开发，前端完全不依赖 SpringBoot

3. 在服务端加入 Swagger 插件，直接通过接口做测试，无需 web 界面

## 参考

- [SpringBoot发布WAR启动报错：Error assembling WAR: webxml attribute is required](http://www.cnblogs.com/huahua035/p/7808176.html)