
# 什么是 ORM
---

对象 - 关系映射（OBJECT/RELATIONAL MAPPING，简称 ORM），是随着面向对象的软件开发方法发展而产生的。用来把对象模型表示的对象映射到基于 SQL 的关系模型数据库结构中去。这样，我们在具体的操作实体对象的时候，就不需要再去和复杂的 SQL 语句打交道，只需简单的操作实体对象的属性和方法。ORM 技术是在面向对象和面向关系之间提供了一条桥梁，前台的对象型数据和数据库中的关系型的数据通过这个桥梁来相互转化。

> ORM is to create, in effect, a "virtual object database" that can be used from within the programming language.

orm 框架的本质是简化编程中操作数据库的编码，目前最流行的主要有两个，一个是宣称可以不用写一句 SQL 的 hibernate，一个是可以灵活调试动态 sql 的 mybatis, 两者各有特点，在企业级系统开发中可以根据需求灵活使用。一个有趣的现象是传统企业大都喜欢使用 hibernate, 互联网行业通常使用 mybatis。

## 什么是 JPA

JPA 的全称是 Java Persistence API， 即 JAVA 持久化 API，可以理解为是一个 JAVA 的标准规范，这个规范为对 JAVA 对象的持久化制定了一些标准的接口，也可以说，JPA 是一个标准的 ORM（对象关系映射）规范。由于市面上 ORM 框架很多，不同的 ORM 框架相互之间并不兼容，SUN 希望通过制定统一规范，达到一统 ORM 标准的目的。但要注意 JPA 只是一个接口规范，而不是实现，具体实现由各供应商来完成，目前的话，**Hibernate，TopLink,OpenJPA 都很好地实现了 JPA 接口**。 

## 什么是 hibernate

hibernate 特点就是所有的 sql 都用 Java 代码来生成，不用跳出程序去写（看）sql，Spring Data JPA 就是 Spring 基于 ORM 框架 JPA 规范的基础上封装的一套 JPA 应用框架，可使开发者用极简的代码即可实现对数据的访问和操作。它提供了包括增删改查等在内的常用功能，且易于扩展！ 在 spring boot 中 `spring-boot-starter-data-jpa` 提供了一个快速的方法集成 Spring Data JPA、Hibernate、Spring ORMs(Core ORM support from the Spring Framework)。

spring data jpa、jpa 以及 ORM 框架之间的关系
![](https://jverson.oss-cn-beijing.aliyuncs.com/201709181701_866.png)


## 什么是 MyBatis

mybatis 的前身是 ibatis，源于 Apache 的一个开源项目。是一个持久层的框架（并没有实现 JPA 的规范，也不是一个完全意义上的 ORM 框架），这个框架最大的特点有三点：
- 面向接口编程
- 可以在配置文件中定义 SQL 语句
- 支持动态 SQL, 这是其独特的一面。

mybatis 初期使用比较麻烦，需要各种配置文件、实体类、dao 层映射关联、还有一大推其它配置。当然 mybatis 也发现了这种弊端，初期开发了 generator 可以根据表结果自动生产实体类、配置文件和 dao 层代码，可以减轻一部分开发量；后期也进行了大量的优化可以使用注解了，自动管理 dao 层和配置文件等。后来在 springboot 中出现了 mybatis-spring-boot-starter，它就是 springboot+mybatis，可以完全注解不用配置文件，也可以简单配置轻松上手，但是两种方式还是各有利弊。再到最后就有了 `mapper-spring-boot-starter`，就是集成通用 Mapper 到 Spring Boot，大部分场景无需注解，通过继承而来的通用方法即可实现，复杂的场景可通过注解手动 sql 实现。

## MyBatis 和 Hibernate 比较

hibernate 属于全自动的 ORM 框架，着力点在于 POJO 和数据库表之间的映射，完成映射即可自动生成和执行 sql。而 mybatis 相对来说属于半自动的 ORM 框架着力点在于 POJO 和 SQL 之间的映射，自己编写 sql 语句，然后通过配置文件将所需的参数和返回的字段映射到 POJO。**因此从理念上讲，如果说 hibernate 属于 ORM Mapping, 那么 mybatis 相当于 SQL Mapping**。这句话总结的很到位，hibernate 中操作对象相当于操作表，Mybatis 中操作对象相当于操作 sql，这里体现的设计理念的不同，但是最终达到的效果是一样的。

Hibernate 与 MyBatis 都可以通过 SessionFactoryBuider 由 XML 配置文件生成 SessionFactory，然后由 SessionFactory 生成 Session，最后由 Session 来开启执行事务和 SQL 语句。其中 SessionFactoryBuider，SessionFactory，Session 的生命周期都是差不多的。


## 如何选择

Mybatis 门槛低容易上手；手写 sql（更可控、更细粒度的优化，当然也更繁琐）。Hibernate：门槛较高，精通不易；复杂场景使用难度大。最终结论：团队大部分人熟悉哪个框架就用哪个，互联网公司就选 mybatis 就行了。