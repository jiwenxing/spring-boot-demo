# Springboot 国际化
---

最近做了一个简单国外站管理系统，需要支持页面的中英文切换，即国际化。在此简单介绍一下 Springboot + Freemarker 的页面国际化实现。[点击查看源码及demo](https://github.com/jiwenxing/springboot-freemarker)

由于类似 Freemarker 的模板技术是在服务端渲染的，因此国际化也主要是在服务端完成，具体的原理也很简单：页面需要国际化的部分使用变量占位，服务端分别配置有不同语言的变量表，渲染时通过前端传来的 lang 参数（如 `?lang=zh_CN`）加载对应的语言配置（如 `messages_zh_CN.properties`）替换页面的占位变量即可。

## 创建国际化配置文件

SpringBoot 默认国际化文件位于 `classpath:message.properties`，这里将其放在 i18n 文件夹下，同时在 application.properties 文件中指定路径即可 `spring.messages.basename=i18n/messages`

分别创建以下三个文件，注意文件名需要以 messages 开头

- messages.properties （默认，当找不到语言的配置的时候，使用该文件进行展示）。
- messages_zh_CN.properties（中文）
- messages_en_US.properties（英文）

为什么这么配可以参考源码 `MessageSourceAutoConfiguration.class`，部分复制如下：

```java
@ConfigurationProperties(prefix = "spring.messages")
public class MessageSourceAutoConfiguration {
    private static final Resource[] NO_RESOURCES = {};
    /**
     * Comma-separated list of basenames, each following the ResourceBundle convention.
     * Essentially a fully-qualified classpath location. If it doesn't contain a package
     * qualifier (such as "org.mypackage"), it will be resolved from the classpath root.
     */
    private String basename = "messages";

    /**
     * Message bundles encoding.
     */
    private Charset encoding = Charset.forName("UTF-8");

    /**
     * Loaded resource bundle files cache expiration, in seconds. When set to -1, bundles
     * are cached forever.
     */
    private int cacheSeconds = -1;
 
    /**
     * Set whether to fall back to the system Locale if no files for a specific Locale
     * have been found. if this is turned off, the only fallback will be the default file
     * (e.g. "messages.properties" for basename "messages").
     */

    private boolean fallbackToSystemLocale = true;

```

## 动态改变本地语言

想要实现动态改变语言，一种简单的方式是从用户的请求中获取到语言的参数，在返回页面之前动态设置当前语言属性。 

这里最核心的就是 `LocaleResolver`，即区域解析器，它负责解析用户的区域从而根据这个区域显示对应的国际化内容。当一个请求到达，DispatcherServlet 就会自动寻找 LocaleResolver，如果找到就会试图通过它进行本地化。通过 RequestContext.getLocale() 方法我们可以得到 locale resolver 解释的本地化语言。

Spring MVC 提供了几个 LocaleResolver 的实现

- AcceptHeaderLocaleResolve 通过检验HTTP请求的accept-language头部来解析区域。
- SessionLocaleResolver 它通过检验用户会话中预置的属性来解析区域。
- CookieLocaleResolver 通过 Cookie 来解析区域。如果Cookie不存在，它会根据accept-language HTTP头部确定默认区域。

然后通过配置 LocaleChangeInterceptor 可以动态改变本地语言，它会检测请求中的参数并且改变地区信息，它调用 LoacalResolver.setLocal() 进行配置。


```java
@Configuration
public class LocaleConf implements WebMvcConfigurer {
	@Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        // 默认语言
        slr.setDefaultLocale(Locale.US);
        return slr;
    }
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        // 参数名，从请求的lang参数获取区域信息
        lci.setParamName("lang");
        return lci;
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }	
}
```

## 在 Freemarker 模板中使用

需要通过一定的语法将国际化内容设置到模板中，以 
Freemarker 为例，首先需要在 FreeMarker 的 ftl 模板的 head 标签中引入 `<#import "/spring.ftl" as spring>`

其中 `spring.ftl` 文件主要是定义了一些 Freemarker 的宏，利用这些宏可以方便的在 Freemarker 中处理国际化信息，其文件位于 `org/springframework/spring-webmvc/5.0.6.RELEASE/spring-webmvc-5.0.6.RELEASE.jar!/org/springframework/web/servlet/view/freemarker/spring.ftl` 中，注意引入时不要少了前面的 `/`。

常用的宏有：messageText、messageArgs、messageArgsText 等。

下面是 Freemarker 中使用示例

```html
<!DOCTYPE html>
<html lang="en">
<head>
<#import "/spring.ftl" as spring>
<meta charset="utf-8">
<title>Springboot Freemarker</title>
<link href="//jverson.com/favicon.ico" type="image/x-icon"
	rel="shortcut icon">
</head>
<body>
	<div class="container-fluid" style="padding-top: 15px;">
		<div class="panel panel-default">
		  <div class="panel-heading"><@spring.message "index.title"/>  
			<span class="language">
			   <a href="?lang=en_US">English(US)</a>
			   <a href="?lang=zh_CN">简体中文</a>
			</span>
		  </div>
		</div>
	</div>
</body>
</html>
```


