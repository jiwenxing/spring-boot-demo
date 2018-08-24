spring boot 的 rest 接口返回格式可以通过 RequestMapping 注解的 produces 进行指定，如果项目需要同时既能满足 json 与 xml 的返回格式，该怎么实现呢？

## 引入依赖

我们知道 spring boot 默认使用 jackson 处理 json 的序列化工作，而 `spring-boot-starter-web` 中默认引入的 `jackson-databind` 依赖只能实现 json 格式的自动转换，如果要想同时支持 XML 格式的转换需要添加以下依赖：

```xml
<dependency>
	<groupId>com.fasterxml.jackson.dataformat</groupId>
	<artifactId>jackson-dataformat-xml</artifactId>
</dependency>
```

此依赖同样包含 `jackson-databind` 的依赖。因此添加了此依赖后将即支持 Json 也支持 XML 格式的返回值。

## Controller

```java
@RestController
public class HelloController {	
	@RequestMapping(value = "/getCar", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.APPLICATION_XML_VALUE})
	public Object getCar() throws PageException{
		Car car = new Car();
		car.setBrand("bmw");
		car.setColor("red");
		car.setPrice(33.88);
		return car;
	}	
}
```

当 produces 设置为 `produces = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.APPLICATION_XML_VALUE}` 的时候该方法将同时支持 Json 和 XML 的返回类型。

## 测试

分别在浏览器输入 `http://127.0.0.1/getCar.json` 将看到

```json
{
    "brand": "bmw",
    "color": "red",
    "price": 33.88
}
```

当在浏览器输入 `http://127.0.0.1/getCar.xml` 时将看到

```xml
<Car>
	<brand>bmw</brand>
	<color>red</color>
	<price>33.88</price>
</Car>
```

我们看到请求可以通过不同的扩展名很方便的指定返回值的格式。