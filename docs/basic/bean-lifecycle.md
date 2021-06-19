# Spring 中 Bean 生命周期
---

Bean 生命周期主要涉及到了5个接口，分别是 XxxAware、BeanPostProcessor、InitiailizingBean、Destruction、DisposableBean，另外还有 init-method、destroy-method 等。他们的执行顺序大概如下图所示：

![](https://jverson.oss-cn-beijing.aliyuncs.com/539ea4c261c9b2408cceeda0a8f549d5.jpg)


## Aware 接口

Aware 接口有很多实现类，主要就是 BeanNameAware、BeanFactoryAware、ApplicationContextAware 等：

![](https://jverson.oss-cn-beijing.aliyuncs.com/3a0d26bd44104cb267842ffc09a55f37.jpg)

 
1. 实现BeanNameAware接口后，重写setBeanName方法，可以对单个Bean进行扩展修改；

2. 实现BeanFactoryAware接口后，重写setBeanFactory方法，可以对bean工厂中的所有Bean进行扩展修改；

3. 实现ApplicationContextAware接口后，重写setApplicationContext方法后，可以对整个容器进行扩展修改；

4. 这几个接口的执行顺序分别是BeanNameAware->BeanFactoryAware->ApplicationContextAware；

## BeanPostProcessor 接口

1. BeanPostProcessor 接口的前置和后置处理，是在Aware接口之后调用；

2. 很重要的一点，需要将 BeanPostProcessor 接口实现类声明为 bean，使用 `<bean>`配置或者使用 @Component 注解，不然 BeanPostProcessor 不起作用。

```Java
@Slf4j
public class MyBeanPostProcessor implements BeanPostProcessor {
 
    /**
     * 实现了BeanPostProcessor接口后，重写postProcessBeforeInitialization，在各种Aware接口执行完毕后执行该方法
     *
     * @param bean     本次处理的bean
     * @param beanName 本次处理的beanName（bean id）
     * @return 返回的是在本方法中处理后的bean
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        log.info("MyBeanPostProcessor.postProcessBeforeInitialization, beanName:{}, bean:{}", beanName, bean);
        return bean;
    }
 
    /**
     * 实现了BeanPostProcessor接口后，重写postProcessBeforeInitialization，在initMethod方法执行完毕后执行该方法
     *
     * @param bean     本次处理的bean
     * @param beanName 本次处理的beanName（bean id）
     * @return 返回的是在本方法中处理后的bean
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        log.info("MyBeanPostProcessor.postProcessAfterInitialization, beanName:{}, bean:{}", beanName, bean);
        return bean;
    }
}
```


## InitializingBean 接口

实现 InitializingBean 接口，然后重写 afterPropertiesSet 方法：

```Java
@Data
@Slf4j
public class Student implements InitializingBean {
 
    private Integer id;
    private String name;
 
    @Override
    public void afterPropertiesSet() throws Exception {
        // 同样可以在这里修改bean的属性值
        log.info("InitialingBean.afterPropertiesSet, this:{}", this);
    }
}
```

## init-method

设置 bean 的时候调用的

## DestructionAwareBeanPostProcessor


## DisposableBean 接口　

对单独的 Bean 进行 destrction 后的处理，也就是说不是应用到所有的bean上。