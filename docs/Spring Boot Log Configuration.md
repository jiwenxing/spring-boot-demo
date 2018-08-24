# Spring Boot Log Configuration
---

## Spring Boot 默认日志

如果是继承Starter使用Spring Boot，则默认使用的是logback，不过不用担心，不管依赖的那些库使用的是什么日志框架，通常你不需要改变任何日志依赖，Springboot可以确保那些框架都能正常的工作。

### Log Level

默认情况下springboot会将info及以上级别的log回显在console里，可以使用`debug=true`或`trace=true`显示更加详细的日志。

也可以指定某些特定类的日志输出级别，例如之前使用的ucc依赖，心跳日志是info级别特别烦人。在配置文件中设置`logging.level.com.jverson.springboot.HelloWorld = WARN`即可将HelloWorld类的日志输出限制为WARN级别。

> logging.level.root=WARN    
logging.level.org.springframework.web=DEBUG    
logging.level.org.hibernate=ERROR    

### File output

默认情况下Springboot只会将日志输出到console，并不会输出为文件。如果需要记录为文件则需要在配置文件中指定日志输出路径（logging.file）和名称（logging.path）。
例如在配置文件中设置`logging.file = /export/Logs/mylog.log`，则日志便会输出到该目录下，并且文件大小达到10MB时便会自动分割。但是发现这样并没有办法将日志按照Level输出到不同的文件中，如果想实现则需要在classpath下添加`logback-spring.xml`（推荐）或`logback.xml`配置文件。


## Custom log configuration

如果不想使用默认的logback日志框架，可以添加其它框架的依赖（例如log4j2），Log4j2.0基于LMAX Disruptor的异步日志在多线程环境下性能会远远优于Log4j1.x和logback（官方数据是10倍以上）。具体的使用流程如下：

首先添加log4j2的依赖，并将springboot自带的log框架排除掉

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-web</artifactId>
	<exclusions><!-- 去掉默认配置 -->
		<exclusion>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-logging</artifactId>
		</exclusion>
	</exclusions>
</dependency>
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-test</artifactId>
	<scope>test</scope>
</dependency>

<!-- use log4j2 -->
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-log4j2</artifactId>
</dependency>
<!--使用log4j2的AsyncLogger需要包含disruptor-->
<dependency>
	<groupId>com.lmax</groupId>
	<artifactId>disruptor</artifactId>
	<version>3.3.4</version>
</dependency>
```

然后添加log4j2的配置文件`log4j2-spring.xml`（按照命名规范命名的话可以不需要再properties文件中设置`logging.config = classpath:log4j2-spring.xml`），典型的配置内容如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="OFF" monitorInterval="1800">
	<properties>
		<property name="LOG_HOME">/export/Logs/jverson.com</property>
		<property name="RUNNING_FILE_NAME">spring-boot-test</property>
		<property name="ERROR_FILE_NAME">spring-boot-test-error</property>
	</properties>
	<Appenders>
		<Console name="Console" target="SYSTEM_OUT">
			<ThresholdFilter level="INFO" onMatch="ACCEPT" onMismatch="DENY"/>
			<PatternLayout pattern="%d{yyyy-MM-dd 'at' HH:mm:ss.SSS} [%t] %-5level %class{36} [%L] [%M] - %msg%xEx%n" />
		</Console>
		<RollingRandomAccessFile name="RUNNING-LOG" fileName="${LOG_HOME}/${RUNNING_FILE_NAME}.log" filePattern="${LOG_HOME}/${RUNNING_FILE_NAME}_%d{yyyy-MM-dd}_%i.log">
			<ThresholdFilter level="INFO" onMatch="ACCEPT" onMismatch="DENY"/>
			<PatternLayout pattern="%d{yyyy-MM-dd 'at' HH:mm:ss.SSS} [%t] %-5level %class{36} [%L] [%M] - %msg%xEx%n" />
			<Policies>
				<TimeBasedTriggeringPolicy modulate="true" interval="24" />
				<SizeBasedTriggeringPolicy size="51200 KB" />
			</Policies>
			<DefaultRolloverStrategy max="20" />
		</RollingRandomAccessFile>
		<RollingRandomAccessFile name="ERROR-LOG" fileName="${LOG_HOME}/${ERROR_FILE_NAME}.log" filePattern="${LOG_HOME}/${ERROR_FILE_NAME}_%d{yyyy-MM-dd}_%i.log">
			<ThresholdFilter level="ERROR" onMatch="ACCEPT" onMismatch="DENY"/>
			<PatternLayout pattern="%d{yyyy-MM-dd 'at' HH:mm:ss.SSS} [%t] %-5level %class{36} [%L] [%M] - %msg%xEx%n" />
			<Policies>
				<TimeBasedTriggeringPolicy modulate="true" interval="24" />
				<SizeBasedTriggeringPolicy size="51200 KB" />
			</Policies>
			<DefaultRolloverStrategy max="20" />
		</RollingRandomAccessFile>
	</Appenders>
	<Loggers>
		<AsyncRoot level="INFO" includeLocation="true">
			<appender-ref ref="Console" />
			<appender-ref ref="RUNNING-LOG" />
			<appender-ref ref="ERROR-LOG" />
		</AsyncRoot>
	</Loggers>
</Configuration>
```

## 参考

1. [Apache Log4j 2](https://logging.apache.org/log4j/2.x/)
2. [Configure Log4j for logging](http://docs.spring.io/spring-boot/docs/1.5.1.RELEASE/reference/htmlsingle/#howto-configure-log4j-for-logging)