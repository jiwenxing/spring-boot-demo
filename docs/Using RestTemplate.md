# RestTemplate 介绍
---

spring 框架提供的 RestTemplate 类可用于在应用中调用 rest 服务，它简化了与 http 服务的通信方式，统一了 RESTful 的标准，封装了 http 链接，我们只需要传入 url 及返回值类型即可。相较于之前常用的 httpClient，RestTemplate 是一种更优雅的调用 RESTful 服务的方式。

RestTemplate 默认依赖 JDK 提供 http 连接的能力（HttpURLConnection），如果有需要的话也可以通过 `setRequestFactory` 方法替换为例如 `Apache HttpComponents`、`Netty` 或 `OkHttp` 等其它 HTTP library。

## RestTemplate 使用

### 直接使用

`RestTemplateTest.java`
```java
public class RestTemplateTest {
	public static void main(String[] args) {
		RestTemplate restT = new RestTemplate();
		// 通过 Jackson JSON processing library 直接将返回值绑定到对象
		Quote quote = restT.getForObject("http://gturnquist-quoters.cfapps.io/api/random", Quote.class);  
		String quoteString = restT.getForObject("http://gturnquist-quoters.cfapps.io/api/random", String.class);
		System.out.println(quoteString);
	}
}
```

> Because the Jackson JSON processing library is in the classpath, RestTemplate will use it (via a message converter) to convert the incoming JSON data into a Quote object.

可以看到可以将返回结果放在一个 String 对象中，也可以直接绑定到一个自定义的对象上，其中 Quote 如下：

`Quote.java`
```java
@JsonIgnoreProperties(ignoreUnknown = true) // indicate that any properties not bound in this type should be ignored.
public class Quote {

	private String type;
    private Value value;

    //getters & setters
}
```

这里有如下两点需要注意（懒得翻译），@JsonIgnoreProperties 如果定义了返回结果中没有的属性则忽略，另外属性名需要和返回结果的属性名一致，否则需要使用 @JsonProperty 注解进行匹配。

- It’s annotated with @JsonIgnoreProperties from the Jackson JSON processing library to indicate that any properties not bound in this type should be ignored.
- In order for you to directly bind your data to your custom types, you need to specify the variable name exact same as the key in the JSON Document returned from the API. In case your variable name and key in JSON doc are not matching, you need to use @JsonProperty annotation to specify the exact key of JSON document.


### 在 Spring boot 中使用 RestTemplate

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
注意 spring boot 并不会自动装配 `RestTemplate` 类，因为通常用户都需要一个定制的 RestTemplate，因此 springboot 自动装配了一个 `RestTemplateBuilder` 类方便用户定制创建自己的 RestTemplate 类。

> Since RestTemplate instances often need to be customized before being used, Spring Boot does not provide any single auto-configured RestTemplate bean. It does, however, auto-configure a RestTemplateBuilder which can be used to create RestTemplate instances when needed. The auto-configured RestTemplateBuilder will ensure that sensible HttpMessageConverters are applied to RestTemplate instances.

`RestTemplateService.java`
```java
@Service
public class RestTemplateService {
	@Autowired RestTemplate restTemplate;
	
	public Quote someRestCall(){return restTemplate.getForObject("http://gturnquist-quoters.cfapps.io/api/random", Quote.class);
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
	public Object index() {return restTemplateService.someRestCall();
	}
}
```

这是访问 `http://localhost/api/rest` 返回以下结果：
```json
{"type": "success",
"value": {"id": 9,
	"quote": "So easy it is to switch container in #springboot."
	}
}
```

>  `http://gturnquist-quoters.cfapps.io/api/random` is a RESTful service that randomly fetches quotes about Spring Boot and returns them as a JSON document.

## RestTemplate 定制
 
由于不同的 rest 服务调用可能需要不同的 RestTemplate 配置，根据适用范围通常有两种方式进行配置。

一、单类定制

`RestTemplateService.java`
```java
@Service
public class RestTemplateService {
	private final RestTemplate restTemplate;
	public RestTemplateService(RestTemplateBuilder builder){  //RestTemplateBuilder will be auto-configured
		this.restTemplate = builder.setConnectTimeout(1000).setReadTimeout(1000).build();}
	
	public Quote someRestCall(){return restTemplate.getForObject("http://gturnquist-quoters.cfapps.io/api/random", Quote.class);
	}	
}
```

二、跨类定制

`BeanConf.class`
```java
@Configuration
public class BeanConf {@Bean(name ="restTemplateA")
	public RestTemplate restTemplateA(RestTemplateBuilder builder) {return builder.basicAuthorization("username","password")  
            .setConnectTimeout(3000)  
            .setReadTimeout(5000)  
            .rootUri("http://api1.example.com/")  
            .errorHandler(new CustomResponseErrorHandler())  
            .additionalMessageConverters(new CustomHttpMessageConverter())  
            .uriTemplateHandler(new OkHttp3ClientHttpRequestFactory())  
            .build();}
	
	@Bean(name ="restTemplateB")
	public RestTemplate restTemplateB(RestTemplateBuilder builder) {return builder.basicAuthorization("username","password")  
            .setConnectTimeout(1000)  
            .setReadTimeout(1000)  
            .rootUri("http://api2.example.com/")  
            .errorHandler(new CustomResponseErrorHandler())  
            .additionalMessageConverters(new CustomHttpMessageConverter())  
            .uriTemplateHandler(new OkHttp3ClientHttpRequestFactory())  
            .build();}
}
```

`RestTemplateService.java`
```java
@Service
public class RestTemplateService {@Resource(name ="restTemplateB")
	private RestTemplate restTemplate;
	
	public Quote someRestCall(){return restTemplate.getForObject("http://gturnquist-quoters.cfapps.io/api/random", Quote.class);
	}
}
```

三、应用内定制

通过实现 `RestTemplateCustomizer` 接口，其中的设置在所有通过 `RestTemplateBuilder` 创建的 RestTemplate 都将生效。

```java
@Component  
public class CustomRestTemplateCustomizer implements RestTemplateCustomizer {  
    @Override  
    public void customize(RestTemplate restTemplate) {new RestTemplateBuilder()  
                .detectRequestFactory(false)  
                .basicAuthorization("username", "password")  
                .uriTemplateHandler(new OkHttp3ClientHttpRequestFactory())  
                .errorHandler(new CustomResponseErrorHandler())  
                .configure(restTemplate);  
    }  
} 
```

## http 连接池

> By default RestTemplate creates new Httpconnection every time and closes the connection once done.If you need to have a connection pooling under rest template then you may use different implementation of the ClientHttpRequestFactory that pools the connections.

RestTemplate 默认不使用连接池，如果想使用则需要一个 `ClientHttpRequestFactory` 接口的实现类来池化连接。例如使用 `HttpComponentsClientHttpRequestFactory`。

```java
RestTemplate restT = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
```

注意 `HttpComponentsClientHttpRequestFactory` 是 `org.springframework.http.client.ClientHttpRequestFactory` 的实现类，它底层使用了 [Apache HttpComponents HttpClient](http://hc.apache.org/httpcomponents-client-ga/) to create requests.

## 实战

和上面的 demo 不一样的是，下面的代码是在真实业务中使用的，使用了连接池、设置了超时时间、对中文乱码问题进行了处理，同时还使用拦截器统一了日志打印。

restTemplate 配置

```java
@Configuration
public class RestTemplateUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestTemplateUtil.class);

    @Bean("restTemplate")
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpComponentsClientHttpRequestFactory.setConnectTimeout(2000);
        httpComponentsClientHttpRequestFactory.setReadTimeout(2000);
        RestTemplate restTemplate = new RestTemplate(httpComponentsClientHttpRequestFactory);
        //乱码处理
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters().stream().map(httpMessageConverter -> {
            if (httpMessageConverter instanceof StringHttpMessageConverter) {
                return new StringHttpMessageConverter(Charset.forName("UTF-8"));
            }
            return httpMessageConverter;
        }).collect(Collectors.toList());
        restTemplate.setMessageConverters(messageConverters);
        //拦截器统一打印日志
        restTemplate.setInterceptors(Lists.newArrayList(new RestTemplateUtil.MyRequestInterceptor()));
        return restTemplate;
    }

    class MyRequestInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] bytes, ClientHttpRequestExecution execution) {
            HttpMethod method = request.getMethod();
            java.net.URI uri = request.getURI();
            String host = uri.getHost();
            String path = uri.getPath();
            String query = uri.getQuery();
            String body = new String(bytes, StandardCharsets.UTF_8);
            LOGGER.info("Request URL = [{}] http://{}{}?{}", method, host, path, query);
            if (HttpMethod.POST == method) {
                LOGGER.info("Request Body = {}", body);
            }
            ClientHttpResponse response = null;
            try {
                response = execution.execute(request, bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
    }
}
```

服务中调用

```java
@Service
public class ProductSearchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductSearchService.class);
    private static final String baseUrl = "http://jverson.com/product";
    @Autowired
    private RestTemplate restTemplate;

    public JSONObject search(MultiValueMap<String, String> paramMap) throws URISyntaxException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl).queryParams(paramMap);
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                entity,
                String.class);
        if (HttpStatus.OK != responseEntity.getStatusCode()) {
            throw new BizException(ResultEnum.EXCEPTION, "商品搜索接口http请求异常！");
        }
        LOGGER.info("response = {}", responseEntity.getBody());
        return JSON.parseObject(responseEntity.getBody());
    }

}
```

其中 get 请求参数可以封装为一个 LinkedMultiValueMap 对象

```java
private LinkedMultiValueMap wrapGetParams(final ProductRcmdReq req) {
    LinkedMultiValueMap<String, String> paramMap = new LinkedMultiValueMap();

    paramMap.add("uuid", UUID.randomUUID().toString());
    paramMap.add("user_pin", req.getPin());
    paramMap.add("page", req.getPage().toString());
    paramMap.add("pagesize", req.getPageSize().toString());

    StringBuilder sb = new StringBuilder("ids,,").append(req.getShopId());//指定店铺id
    paramMap.add("key", sb.toString());

    // 排序规则
    paramMap.add("sort_type", SortTypeEnum.getNameByType(req.getSortType()));

    return paramMap;
}
```


## References

- [spring-boot docs Calling REST services](https://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/reference/htmlsingle/#boot-features-restclient)
- [an application that uses Spring’s RestTemplate to retrieve a random Spring Boot quotation](https://github.com/spring-guides/gs-consuming-rest)
- [Consuming a RESTful Web Service](https://spring.io/guides/gs/consuming-rest/)