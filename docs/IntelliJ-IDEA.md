# IntelliJ IDEA 的一些使用技巧
---

最近刚刚从 Eclipse 切换到 IntelliJ IDEA，确实很好用，但还不太熟练，一些使用技巧得总结一下

## 本地启动加载 scope 为 provided 的 jar 依赖

因为在 springboot 项目中一般 Tomcat 的配置是 provided 的，也就是说只参与编译、测试、运行等阶段，相当于 compile，但是打包阶段做了 exclude 的动作。也就是说打 war 包是并不会打进去，认为目标容器会提供。

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-tomcat</artifactId>
    <scope>provided</scope>
</dependency>
```

但问题是 idea 本地运行也是默认不加载 provided 的依赖，就会出现在 eclipse 中启动没有问题的应用放在 idea 中就会报找不到类，需要专门设置一下：run - edit configuration - 选中应用 - 右侧勾选 include dependence with Provided scope 即可。另外 maven 中默认的 scope 是 compile，代表项目在编译、测试、运行阶段都需要这个 artifact 对应的 jar 包在 classpath 中。

![](https://jverson.oss-cn-beijing.aliyuncs.com/5a27d173f0e74a41f4832768d3e3a32f.jpg)


## IDEA 中热启动失效

之前在 Eclipse 中使用 spring-boot-devtools 热启动功能很好用，但是在 IDEA 中发现不生效，原来是因为 eclipse 默认开启了自动编译，而 idea 默认没有开启，而 devtools 就是检测用户代码的字节码文件是否有更新决定是否重新加载的。

有两种方式解决：

1. IDEA 开启自动编译  settings - compile - build project automatically
2. 更改完代码手动 ctrl + F9（cmd + B） 触发编译


