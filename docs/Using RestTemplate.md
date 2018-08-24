# RestTemplate介绍
---

spring框架提供的RestTemplate类可用于在应用中调用rest服务，它简化了与http服务的通信方式，统一了RESTful的标准，封装了http链接，我们只需要传入url及返回值类型即可。相较于之前常用的httpClient，RestTemplate是一种更优雅的调用RESTful服务的方式。

RestTemplate默认依赖JDK提供http连接的能力（HttpURLConnection），如果有需要的话也可以通过`setRequestFactory`方法替换为例如`Apache HttpComponents`、`Netty`或`OkHttp`等其它HTTP library。

## RestTemplate使用

### 直接使用

`RestTemplateTest.java`
```java
public class RestTemplateTest {
	public static void main(String[] args) {
		RestTemplate restT = new RestTemplate();
		//通过Jackson JSON processing library直接将返回值绑定到对象
		Quote quote = restT.getForObject("http://gturnquist-quoters.cfapps.io/api/random", Quote.class);  
		String quoteString = restT.getForObject("http://gturnquist-quoters.cfapps.io/api/random", String.class);
		System.out.println(quoteString);
	}
}
```

> Because the Jackson JSON processing library is in the classpath, RestTemplate will use it (via a message converter) to convert the incoming JSON data into a Quote object.

可以看到可以将返回结果放在一个String对象中，也可以直接绑定到一个自定义的对象上，其中Quote如下：

`Quote.java`
```java
@JsonIgnoreProperties(ignoreUnknown = true) // indicate that any properties not bound in this type should be ignored.
public class Quote {

	private String type;
    private Value value;

    //getters & setters
}
```

这里有如下两点需要注意（懒得翻译），@JsonIgnoreProperties如果定义了返回结果中没有的属性则忽略，另外属性名需要和返回结果的属性名一致，否则需要使用@JsonProperty注解进行匹配。

- It’s annotated with @JsonIgnoreProperties from the Jackson JSON processing library to indicate that any properties not bound in this type should be ignored.
- In order for you to directly bind your data to your custom types, you need to specify the variable name exact same as the key in the JSON Document returned from the API. In case your variable name and key in JSON doc are not matching, you need to use @JsonProperty annotation to specify the exact key of JSON document.


### 在Spring boot中使用RestTemplate

`application.java`
```java
@SpringBootApplication 
public class HelloSpringBoot {
	public static void main(String[] args) {
	    SpringApplication.run(HelloWorld.class, args);
	}
	
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}	
}
```
注意spring boot并不会自动装配`RestTemplate`类，因为通常用户都需要一个定制的RestTemplate，因此springboot自动装配了一个`RestTemplateBuilder`类方便用户定制创建自己的RestTemplate类。

> Since RestTemplate instances often need to be customized before being used, Spring Boot does not provide any single auto-configured RestTemplate bean. It does, however, auto-configure a RestTemplateBuilder which can be used to create RestTemplate instances when needed. The auto-configured RestTemplateBuilder will ensure that sensible HttpMessageConverters are applied to RestTemplate instances.

`RestTemplateService.java`
```java
@Service
public class RestTemplateService {
	@Autowired RestTemplate restTemplate;
	
	public Quote someRestCall(){
		return restTemplate.getForObject("http://gturnquist-quoters.cfapps.io/api/random", Quote.class);
	}	
}
```

`RestTemplateController.java`
```java
@RestController
@RequestMapping("/api/rest")
public class RestTemplateController {
	@Autowired 
	private RestTemplateService restTemplateService;
	
	@RequestMapping
	public Object index() {
		return restTemplateService.someRestCall();
	}
}
```

这是访问`http://localhost/api/rest`返回以下结果：
```json
{
"type": "success",
"value": {
	"id": 9,
	"quote": "So easy it is to switch container in #springboot."
	}
}
```

>  `http://gturnquist-quoters.cfapps.io/api/random` is a RESTful service that randomly fetches quotes about Spring Boot and returns them as a JSON document.

## RestTemplate定制
 
由于不同的rest服务调用可能需要不同的RestTemplate配置，根据适用范围通常有两种方式进行配置。

一、单类定制

`RestTemplateService.java`
```java
@Service
public class RestTemplateService {
	private final RestTemplate restTemplate;
	public RestTemplateService(RestTemplateBuilder builder){  //RestTemplateBuilder will be auto-configured
		this.restTemplate = builder.setConnectTimeout(1000).setReadTimeout(1000).build();
	}
	
	public Quote someRestCall(){
		return restTemplate.getForObject("http://gturnquist-quoters.cfapps.io/api/random", Quote.class);
	}	
}
```

二、跨类定制

`BeanConf.class`
```java
@Configuration
public class BeanConf {

	@Bean(name = "restTemplateA")
	public RestTemplate restTemplateA(RestTemplateBuilder builder) {
		return builder.basicAuthorization("username", "password")  
            .setConnectTimeout(3000)  
            .setReadTimeout(5000)  
            .rootUri("http://api1.example.com/")  
            .errorHandler(new CustomResponseErrorHandler())  
            .additionalMessageConverters(new CustomHttpMessageConverter())  
            .uriTemplateHandler(new OkHttp3ClientHttpRequestFactory())  
            .build();
	}
	
	@Bean(name = "restTemplateB")
	public RestTemplate restTemplateB(RestTemplateBuilder builder) {
		return builder.basicAuthorization("username", "password")  
            .setConnectTimeout(1000)  
            .setReadTimeout(1000)  
            .rootUri("http://api2.example.com/")  
            .errorHandler(new CustomResponseErrorHandler())  
            .additionalMessageConverters(new CustomHttpMessageConverter())  
            .uriTemplateHandler(new OkHttp3ClientHttpRequestFactory())  
            .build();
	}
}
```

`RestTemplateService.java`
```java
@Service
public class RestTemplateService {
	@Resource(name = "restTemplateB")
	private RestTemplate restTemplate;
	
	public Quote someRestCall(){
		return restTemplate.getForObject("http://gturnquist-quoters.cfapps.io/api/random", Quote.class);
	}
}
```

三、应用内定制

通过实现`RestTemplateCustomizer`接口，其中的设置在所有通过`RestTemplateBuilder`创建的RestTemplate都将生效。

```java
@Component  
public class CustomRestTemplateCustomizer implements RestTemplateCustomizer {  
    @Override  
    public void customize(RestTemplate restTemplate) {  
        new RestTemplateBuilder()  
                .detectRequestFactory(false)  
                .basicAuthorization("username", "password")  
                .uriTemplateHandler(new OkHttp3ClientHttpRequestFactory())  
                .errorHandler(new CustomResponseErrorHandler())  
                .configure(restTemplate);  
    }  
} 
```

## http连接池

> By default RestTemplate creates new Httpconnection every time and closes the connection once done.If you need to have a connection pooling under rest template then you may use different implementation of the ClientHttpRequestFactory that pools the connections.

RestTemplate默认不使用连接池，如果想使用则需要一个`ClientHttpRequestFactory`接口的实现类来池化连接。例如使用`HttpComponentsClientHttpRequestFactory`。

```java
RestTemplate restT = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
```

注意`HttpComponentsClientHttpRequestFactory` 是 `org.springframework.http.client.ClientHttpRequestFactory`的实现类，它底层使用了[Apache HttpComponents HttpClient](http://hc.apache.org/httpcomponents-client-ga/) to create requests.



## References

- [spring-boot docs Calling REST services](https://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/reference/htmlsingle/#boot-features-restclient)
- [an application that uses Spring’s RestTemplate to retrieve a random Spring Boot quotation](https://github.com/spring-guides/gs-consuming-rest)
- [Consuming a RESTful Web Service](https://spring.io/guides/gs/consuming-rest/)