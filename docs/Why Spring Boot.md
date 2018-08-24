Spring Boot 是由 Pivotal 团队提供的全新框架，其设计目的是用来简化新 Spring 应用的初始搭建以及开发过程。该框架使用了特定的方式来进行配置，从而使开发人员不再需要定义样板化的配置。通过这种方式，Spring Boot 致力于在蓬勃发展的快速应用开发领域 (rapid application development) 成为领导者。


### Spring Boot 的主要目标

- 为所有 Spring 应用开发提供一个更快并且随处可得的入门体验
- 提供了很多开箱即用的特性
- 提供了一些大型项目常用的非功能性特征，例如内嵌服务器、安全、健康监测、外部化配置等
- 绝对没有代码生成，也不需要 xml 配置



### Spring Boot 的核心功能

- 可以以 jar 的形式构建独立运行的 Spring 项目，`java -jar xx.jar`
- 内嵌 servlet 容器，包括 Tomcat、Jetty 以及 Undertow
- 提供 starter 简化 Maven 配置
- 自动配置。Spring Boot 会根据类路径中 jar 包，为 jar 包中的类自动配置Bean。
- 准生产的应用监控。提供基于 http、ssh 及 Telnet 的方式对运行的项目进行监控。

### Spring Boot 的优点

- 快速构建项目
- 对主流开发框架的无配置集成
- 项目可独立运行，无需依赖外部容器
- 提供运行时的应用监控
- 极大的提高了开发、部署效率
- 与云计算天然集成


多年以来，Spring IO 平台饱受非议的一点就是大量的 XML 配置以及复杂的依赖管理。事实上 Spring 每个新版本的推出都以减少配置作为自己的主要目标（例如增加 @Service、@Configuration 等注解），而 Spring Boot 所实现的功能则超出了这个任务的描述，开发人员不仅不再需要编写XML，而且在一些场景中甚至不需要编写繁琐的import语句。Spring Boot 的目标不在于为已解决的问题域提供新的解决方案，而是为 Spring 平台带来另一种开发体验，从而简化对这些已有技术的使用。对于已经熟悉 Spring 生态系统的开发人员来说，Spring Boot 是一个很理想的选择，不过对于采用 Spring 技术的新人来说，Spring Boot 提供一种更简洁的方式来使用这些技术。