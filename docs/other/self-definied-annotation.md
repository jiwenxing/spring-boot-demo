# Spring 自定义注解
---

平时在编码中大量的使用了 JDK 及 框架提供的注解，相较于基于 XML 配置的形式注解确实让代码变得非常简洁优雅，尽管增加了代码的耦合度，并且 Springboot 也在倡导去 XML 化。


## 使用场景举例

在平时的应用中，自定义注解可以代替一些配置文件，简化一些公用逻辑，尤其是在造轮子写一些框架或者工具代码时非常有用。例如 UIM 权限校验、登录校验等。

- 生成文档。这是最常见的，也是 java 最早提供的注解。常用的有 @param @return 等
- 在编译时进行格式检查。如 @Override 放在方法前，如果你这个方法并不是覆盖了超类方法，则编译时就能检查出
- 编译时动态处理，编译时通过代码里标识的元数据动态处理，例如动态生成代码。
- 跟踪代码依赖性，实现替代配置文件功能。比如 @EnableMVC 注解里面就引入了 Configuration 配置文件
- 运行时动态处理，运行时通过代码里标识的元数据动态处理，例如使用反射注入实例。可以灵活的实现一些切面逻辑或在 SpringMVC 中实现一些拦截器逻辑



## 自定义注解的原理

一个注解准确意义上来说，就是一种特殊的注释而已，如果没有解析它的代码，它就仅仅是个注释而已了。而解析一个类或者方法的注解往往有两种形式，一种是编译期直接的扫描，一种是运行期反射。编译器的扫描指的是编译器在对 java 代码编译字节码的过程中会检测到某个类或者方法被一些注解修饰，这时它就会对于这些注解进行某些处理。

注解本质是一个继承了 Annotation 的特殊接口，其具体实现类是 Java 运行时生成的动态代理类。而我们通过反射获取注解时，返回的是 Java 运行时生成的动态代理对象 $Proxy1。通过代理对象调用自定义注解（接口）的方法，会最终调用 AnnotationInvocationHandler 的 invoke 方法。该方法会从 memberValues 这个 Map 中索引出对应的值。而 memberValues 的来源是 Java 

例如注解 @Override 的定义和反编译代码如下所示

```Java
// Override 定义
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Override {

}
// 反编译后
public interface Override extends Annotation{
    
}
```

详细的原理可以参考 [JAVA 注解的基本原理](https://www.cnblogs.com/yangming1996/p/9295168.html)


## 注解基础知识

元注解：描述注解的注解

- @Target：注解的作用目标
  - ElementType.TYPE：允许被修饰的注解作用在类、接口和枚举上
  - ElementType.FIELD：允许作用在属性字段上
  - ElementType.METHOD：允许作用在方法上
  - ElementType.PARAMETER：允许作用在方法参数上
  - ElementType.CONSTRUCTOR：允许作用在构造器上
  - ElementType.LOCAL_VARIABLE：允许作用在本地局部变量上
  - ElementType.ANNOTATION_TYPE：允许作用在注解上
  - ElementType.PACKAGE：允许作用在包上
- @Retention：注解的生命周期
  - RetentionPolicy.SOURCE：当前注解编译期可见，不会写入 class 文件，e.g. @Override
  - RetentionPolicy.CLASS：类加载阶段丢弃，会写入 class 文件，
  - RetentionPolicy.RUNTIME：永久保存，可以反射获取，包括自定义的大部分都是此类型，因为需要反射获取织入逻辑
- @Documented：注解是否应当被包含在 JavaDoc 文档中
- @Inherited：是否允许子类继承该注解，即如果注解修饰了一个类，而该类的子类将自动继承父类的该注解。

对于一个类或者接口来说，Class 类中提供了以下一些方法用于反射注解，方法字段中也类似。

- getAnnotation：返回指定的注解
- isAnnotationPresent：判定当前元素是否被指定注解修饰
- getAnnotations：返回所有的注解
- getDeclaredAnnotation：返回本元素的指定注解
- getDeclaredAnnotations：返回本元素的所有注解，不包含父类继承而来的


## 实现过程及案例介绍

自定义注解其实很简单，按照注解的规范定义个一个注解类（@interface）即可，注解的切面逻辑则可以通过拦截器（web请求）或者AOP去实现。

下面简单介绍一下自定义注解的两个实际使用案例

- 利用自定义注解 + 拦截器实现登录校验/权限校验（Web 请求的相关逻辑都可以通过该组合实现）
- 利用自定义注解 + AOP 实现切面逻辑织入如日志打印（任何地方都可以使用）
- 自定义 Field 注解实现变量名替换

### AOP + Annotation

实现过程

首先自定义一个注解类型，注意注解类型是`@interface`

```java
@Target({ElementType.TYPE, ElementType.METHOD}) //方法、类都可以用
@Retention(RetentionPolicy.RUNTIME) //运行时
@Documented
public @interface MyAnnotation {
    String value() default "";
    String key() default "default key";
}
```

定义一个切面类获取标注了自定义注解的方法，织入切面逻辑，注意这里切点的定义

```java
@Component
@Aspect
public class AuthAspect {
    /**
     * 定义切点，即标注了该注解的方法
     */
    @Pointcut("@annotation(com.jverson.springboot.annotation.MyAnnotation)")
    public void authPointCut(){
    }

    @Around("authPointCut()")
    public void around(ProceedingJoinPoint joinPoint){
        System.out.println("before ..............");
        try {
        	StopWatch sw = new StopWatch();
        	sw.start();
            //获取方法参数
            Object[] args = joinPoint.getArgs();
            MethodSignature sign = (MethodSignature) joinPoint.getSignature();
            String[] parameterNames = sign.getParameterNames();
            System.out.println("parameter list: ");
            for (int i = 0; i < parameterNames.length; i++) {
                System.out.println(parameterNames[i] + " = " + args[i]);
            }
            //获取注解参数
            MyAnnotation annotation =  sign.getMethod().getAnnotation(MyAnnotation.class);
            System.out.println("value = " + annotation.value());
            System.out.println("key = " + annotation.key());

            //执行方法
            joinPoint.proceed();

            //方法执行后逻辑
            sw.stop();
            System.out.println(sw.prettyPrint());

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
```

写一个测试的 service，其中方法使用自定义注解

```java
@Component
public class TestAnnotationService {
    @MyAnnotation(value = "LALALALA", key = "special key")
    public void test(String str, Integer num){
        System.out.println("method excute....");
        Thread.sleep(1000);
    }
}
```

单元测试如下

```java
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringbootApplication.class)
public class SpringbootApplicationTests {
	@Autowired
	private TestAnnotationService testAnnotationService;
	@Test
	public void contextLoads() {
		testAnnotationService.test("param1", 2);
	}
}
```

可以看到输出

> parameter list: 
str = param1
num = 2
value = LALALALA
key = special key
method excute....


### Interceptor + Annotation

如果只是针对 web 请求的自定义注解，也可以用拦截器实现，例如权限拦截/登录拦截的注解实现，只需要在对应 controller 的类或方法上进行注解并传入校验码即可。

```java
public class MyInterceptor implements HandlerInterceptor {
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("MyInterceptor begin ...");
        MyAnnotation annotation = null;
        if (handler instanceof HandlerMethod){
            System.out.println("method name = " + ((HandlerMethod) handler).getMethod().getName());
            annotation = (MyAnnotation)((HandlerMethod) handler).getMethodAnnotation(MyAnnotation.class);
            if (annotation == null){
                return true;
            }
            String value = annotation.value();
            String key = annotation.key();
            System.out.println("MyAnnotation value = " + value + ", key = " + key);
            if (!StringUtils.isEmpty(value)){
                //这里可以实现例如校验权限等业务逻辑
            }
        }
        return true;
    }
}
```

controller 方法或者类上即可使用注解实现权限校验

```java
@GetMapping("/hello")
@MyAnnotation(value = "hello controller method", key = "hello special key")
public  String hello(){
	System.out.println("hello begin...");
	testAnnotationService.test("controller", 5);
	return "hello";
}
```

另外别忘了拦截器需要注册一下才生效

```java
@Configuration
public class WebMvcConf implements WebMvcConfigurer {
    @Bean
    public MyInterceptor myInterceptor() {
        MyInterceptor myInterceptor = new MyInterceptor();
        return myInterceptor;
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(myInterceptor());
    }
}
```


其实上面的自定义注解都可以通过 AOP 的切点规则或者拦截器的拦截规则代替，这里的自定义注解本质上相当于定义切点，即定义哪些方法或类需要织入切面逻辑。

### 自定义 Field Annotation

属性上的自定义注解，例如可以给属性起个别名，或者添加校验逻辑等，这在 Hibernate、Mybatis 及一些序列化框架中普遍使用

```Java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyField {
    String description();
    int length();
}

public class MyFieldTest {
    //使用我们的自定义注解
    @MyField(description = "用户名", length = 12)
    private String username;

    @Test
    public void testMyField(){
        // 获取类模板
        Class c = MyFieldTest.class;
        // 获取所有字段
        for(Field f : c.getDeclaredFields()){
            // 判断这个字段是否有MyField注解
            if(f.isAnnotationPresent(MyField.class)){
                MyField annotation = f.getAnnotation(MyField.class);
                System.out.println("字段:[" + f.getName() + "], 描述:[" + annotation.description() + "], 长度:[" + annotation.length() +"]");
            }
        }
    }
}
```


## 总结一下反射注解工作原理

1. 通过键值对的形式可以为注解属性赋值，像这样：@Hello（value = "hello"）

2. 用注解修饰某个元素，编译器将在编译期扫描每个类或者方法上的注解，会做一个基本的检查，你的这个注解是否允许作用在当前位置，最后会将注解信息写入元素的属性表

3. 虚拟机把生命周期在 RUNTIME 的注解取出并通过动态代理机制生成一个实现注解接口的代理类


> 注解的本质就是一个继承了 Annotation 接口的接口





