# Classpath in Java Web
---


## CLASSPATH

classpath 顾名思义就是类文件的 path，即 `*.class` 的路径。

一谈到文件的路径，我们就很有必要了解一个java项目（通常也是web项目）它在真正运行时候，这个项目内部的目录、文件的结构；这样，我们才好分析、理解 classpath。

一般我们在 IDE 中开发的项目目录组织结构和编译后实际运行的项目目录结构是这样一个关系，src/main/ 下面的 java 和 resources 文件夹都被(编译)打包到了生产包的 WEB-INF/classes/ 目录下；而原来 WEB-INF 下面的 views 和 web.xml 则仍然还是在 WEB-INF 下面。同时由 maven 引入的依赖都被放入到了 WEB-INF/lib/ 下面。最后，编译后的 class 文件和资源文件都放在了 classes 目录下。

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/b8a596a9-0d71-448a-93d9-4f4c6f063b5e)

可以看到在编译打包后的项目中，根目录是 META-INF 和 WEB-INF 。**而 WEB-INF 下的 classes 这个目录就是我们常说的 classpath**。

例如我们在项目中常用的 xml 配置 `classpath:spring-mvc.xml` 代表的就是在 spring-mvc.xml 文件在 WEB-INF/classes/ 目录下获取！

## 扩展内容之启动脚本

在当前大部分项目的启动脚本中都能看到有这么一段设置 classpath 环境变量的脚本，这块内容的理解可以参考这篇文章：[廖雪峰讲 classpath](https://www.liaoxuefeng.com/wiki/1252599548343744/1260466914339296)

```bash
CLASSPATH=WEB-INF/classes
    for i in WEB-INF/lib/*; do
        CLASSPATH=${CLASSPATH}:${i}
    done
    export CLASSPATH
```

我们找一台机器运行上面的脚本， `echo $CLASSPATH` 会看到输出如下，除了 WEB-INF/classes 目录外还有所有 jar 的目录

> WEB-INF/classes:WEB-INF/lib/activation-1.1.jar:WEB-INF/lib/annotations-2.0.1.jar:WEB-INF/lib/antlr-runtime-3.5.jar:WEB-INF/lib/api-annotations-1.0.7.jar:WEB-INF/lib/archaius-core-0.5.4.jar:WEB-INF/lib/arts-client-3.2.6.9.jar:WEB-INF/lib/arts-lucene-core-3.2.6.9.jar:WEB-INF/lib/arts-utils-3.2.6.9.jar:WEB-INF/lib/aspectjrt-1.9.2.jar:WEB-INF/lib/aspectjweaver-1.7.3.jar:WEB-INF/lib/avatar-cache-2.8.0.jar:WEB-INF/lib/avatar-cache-remote-2.8.0.jar:WEB-INF/lib/avatar-core-2.0.1.jar:WEB-INF/lib/avatar-logger-2.0.1.jar:WEB-INF/lib/avatar-tracker-2.2.5.jar ...(省略)

## 扩展内容之 `classpath` vs `classpath*`

classpath：只会到你的class路径中查找找文件; `classpath*`：不仅包含class路径，还包括jar文件中(class路径)进行查找。

在多个 classpath 中存在同名资源，都需要加载时，那么用 classpath: 只会加载第一个，这种情况下需要用 `classpath*`: 前缀。

## 扩展内容之 META-INF

上面我们看到在项目运行根目录下还有一个 META-INF 的文件夹，这个又是做什么的呢？官方一点的说法：META-INF 相当于一个信息包，目录中的文件和目录获得 Java 2 平台的认可与解释，用来配置应用程序、扩展程序、类加载器和服务manifest.mf文件，在打包时自动生成。简单的说法：就是存储了项目的元信息，其中文件manifest.mf仅此一份，描述了程序的基本信息、Main-Class的入口、jar依赖路径Class-Path。

上面说了一大堆其实还是不太了解，因为这个目录下的信息大多是项目构建时自动生成，大部分情况下我们也不需要关心或者修改这个目录里的文件。但是当我们学习或使用 spi 模式时，会用到 META-INFO 下的 services 目录，我们会在这个目录下建一个接口全限定名，内容为实现类的全限定的类名。这时我们算是直接使用了这个目录，在这种情况下打出 war 包，解析包后我们会发现 META-INFO/services 这个目录其实是放在了WEB 应用的安全目录 WEB-INF 下。这里具体在另一篇专门讲解 spi 的文章中在详细探讨！


## 参考

- https://segmentfault.com/a/1190000015802324
- [廖雪峰讲 classpath](https://www.liaoxuefeng.com/wiki/1252599548343744/1260466914339296)