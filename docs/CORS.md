
## 跨域资源共享 CORS

跨域资源访问是经常会遇到的场景，当一个资源从与该资源本身所在的服务器不同的域或端口请求一个资源时，资源便会发起一个跨域 HTTP 请求。出于安全考虑，浏览器会限制从脚本内发起的跨域HTTP请求。例如，XMLHttpRequest（ajax） 和 Fetch 遵循同源策略。跨域资源共享（ CORS ）机制允许 Web 应用服务器进行跨域访问控制，从而使跨域数据传输得以安全进行。浏览器支持在 API 容器中（例如 XMLHttpRequest 或 Fetch ）使用 CORS，以降低跨域 HTTP 请求所带来的风险。

> The same-origin security policy forbids "cross-domain" requests by default. CORS gives web servers cross-domain access controls, which enable secure cross-domain data transfers.


## 非CORS方式实现跨域请求

由于CORS跨域需要服务端支持（设置指定的header），对于一些第三方的或不方便修改服务端的场景可以使用JSONP方式实现跨域请求。

### JSONP方式

jsonp的原理是动态添加`<script>`标签来调用服务器提供的js脚本（script标签本身就不受跨域限制），而ajax的核心是通过XmlHttpRequest获取非本页内容。但是JSONP只支持GET请求，CORS支持所有类型的HTTP请求。JSONP的优势在于支持老式浏览器，以及可以向不支持CORS的网站请求数据。因此ajax和jsonp其实本质上是不同的东西。

> Web页面上调用js文件时则不受是否跨域的影响，不仅如此，我们还发现凡是拥有"src"这个属性的标签都拥有跨域的能力，比如`<script>`、`<img>`、`<iframe>`

随着浏览器对cors的支持和新技术的出现，感觉jsonp现在使用的不多了，这里不再深究，关于jsonp的详细原理详细可参考：[JSONP是什么](https://segmentfault.com/a/1190000007935557)

## CORS方式实现跨域

### 服务端配置

由于CORS方式实现跨域需要服务端配合设置Header，在springboot中只需要添加以下配置即可，或者在需要支持跨域的方法中直接对response设置header，以下三种方式效果相同。

方式1：

```java
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {
    /**
     * 重写addCorsMappings方法实现跨域的设置
     * 当然跨域还可以通过在Controller或方法上添加‘@CrossOrigin("http://domain2.com")’的注解实现，不过下面这种方便统一管理
     * 参考：https://docs.spring.io/spring/docs/current/spring-framework-reference/html/cors.html
     */
    @Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
			.allowedOrigins("http://a.test.com")  //允许的origin
			.allowedMethods("GET", "POST", "DELETE") //允许的方法
			.allowCredentials(true) //是否允许携带cookie
			.maxAge(3600);
	}
    
    //全局跨域，Enabling CORS for the whole application is as simple as:
    /*@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**");
	}*/   
}
```

方式2：

```java
@RequestMapping
public Object index(HttpServletRequest request, HttpServletResponse response, @CookieValue(value = "sid", required = false) String sid) {
	response.setHeader("Access-Control-Allow-Origin","http://a.test.com"); //允许跨域的Origin设置
	response.setHeader("Access-Control-Allow-Credentials","true"); //允许携带cookie
	logger.info("cookie sid = " + sid);
	return restTemplateService.someRestCall();
}
```

方式3：

```java
@RequestMapping
@CrossOrigin(origins = "http://a.test.com", allowCredentials = "true")
public Object index(HttpServletRequest request, @CookieValue(value = "sid", required = false) String sid) {
	logger.info("cookie sid = " + sid);
	return restTemplateService.someRestCall();
}
```

### 前端调用方式

1. 原生ajax调用示例：

```JavaScript
var xhr = new XMLHttpRequest();  
xhr.open("POST", "http://b.test.com/api/rest", true);  
xhr.withCredentials = true; //支持跨域发送cookies
xhr.send();
```

2. jQuery调用示例：

```JavaScript
$.ajax({
    url: 'http://b.test.com/api/rest',
    dataType: 'json',
    type : 'POST',
    xhrFields: {
        withCredentials: true //是否携带cookie
    },
    crossDomain: true,
    contentType: "application/json",
    success: (res) => {
      console.log(res);
    }
  });
```

3. fetch方式

```JavaScript
fetch('http://b.test.com/api/rest', 
  {credentials: 'include'}  //注意这里的设置，支持跨域发送cookies
).then(function(res) {
  if (res.ok) {
    res.json().then(function(data) {
      console.log(data.value);
    });
  } else {
    console.log("Looks like the response wasn't perfect, got status", res.status);
  }
}, function(e) {
  console.log("Fetch failed!", e);
});
```

值得注意的一点是：服务器端 Access-Control-Allow-Credentials = true时，参数Access-Control-Allow-Origin 的值不能为 '*'，必须为具体的origin。

另外还需要注意：试了一下，cookie的域必须是两个子域的顶级域，才能实现跨域传输。即如果网站A是：a.test.cn,网站B是：b.test.com，那么无论如何都不能实现A携带会话cookie发送跨域请求到网站B上。

## References

- [Cross-Origin Resource Sharing (CORS)](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Access_control_CORS)
- [Server-Side Access Control](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Server-Side_Access_Control)
- [Using Fetch](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch)