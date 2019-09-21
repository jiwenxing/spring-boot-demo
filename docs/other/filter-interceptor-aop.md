# filter vs interceptor vs aop
--- 

在上一篇 [Annotation - 自定义注解](self-definied-annotation.md) 中发现一个问题，对注解的处理使用 aop 或者 拦截器都可以实现，甚至也可以在 filter 中。不光是自定义注解，在处理一些公共逻辑时，很多情况下这三种方式都可以实现，那么不同的方式到底有什么区别呢？

## filter vs interceptor

首先来看看 filter 和 interceptor，因为两者都是在 web 应用中大量使用，都是 aop 思想的体现，都能实现一些诸如权限校验、日志记录等功能，他们的区别是什么呢？

- 使用范围不同：filter 是 servlet 规范，只能在 web 程序中，而 interceptor 是 spring 支持的，即可用于 web 也能应用于其它基于 spring 的应用如 swing 等。
- 能够使用的资源不同：interceptor 本身就是 spring 管理的 bean，因此在拦截器中使用 spring 管理的任何其它资源，例如可以通过 ioc 注入其它 bean 等，这一点 filter 做不到。
- 作用范围和时机不同：filter 只在 servlet 前后起作用，而拦截器则可以深入到方法前后、甚至异常前后。
- 实现原理不同：filter 基于回调函数，而 interceptor 基于反射和动态代理。


![](https://jverson.oss-cn-beijing.aliyuncs.com/70ec407740195d6ea2c310c4a0fb8533.jpg)

所以一个 web 请求的处理流程是这样的

`过滤前 - 拦截前 - Action处理 - 拦截后 - 过滤后`




## filter vs interceptor vs aop

从下面的表格可以看到他们之前还有一些重要区别，filter 只能获取 ServletRequest、ServletResponse，并不能知道搜拦截的方法及方法的参数等信息，而 interceptor 和 aop 则可以，但在 web 应用中 aop 取不到请求和相应。

![](https://jverson.oss-cn-beijing.aliyuncs.com/79be2eef2469f155808f5cf958ba55ef.jpg)

所以上面如果再加上 aop 的话顺序是这样的

`过滤前 - 拦截前 - ControllerAdvie - Aspect 开始执行- controller - Aspect 结束 - ControllerAdvie - 拦截后 - 过滤后`

其中的 ControllerAdvie 是 controller 的增强，和 ExceptionHandler 一起用来做全局异常，在之前的文章 [Error Handling](https://jverson.com/spring-boot-demo/web/error-handling.html) 中有介绍过使用方法。

## 小结

小结一下，其实会发现 filter 能做到的 interceptor 基本都能实现，并且还能更方便的使用 spring 容器管理的其它资源，因此只要是基于 spring 的应用建议优先使用拦截器！aop 主要做一些 service 层的切面增强处理，比如对外提供的 rpc 接口就可以使用 aop 统一进行日志打印、异常处理以及性能监控等，见之前文章 [利用 AOP 实现接口统一日志打印、异常处理及方法监控](https://jverson.com/spring-boot-demo/common/aop.html) 介绍。

