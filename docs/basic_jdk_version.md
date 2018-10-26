# 指定应用及子应用编译的  JDK 版本
---

有时候应用的实现需要用到高版本 JDK 的一些特性，比如说 lambda 表达式需要 1.8 版本以上，而对外提供的 API 则需要设置较低的 JDK 版本便于兼容外部较老的应用，这就需要分别单独设置编译版本。在 Spring Boot 的 `1.5.17.RELEASE` 版本的 pom 文件中可以看到其默认的版本是 1.6 如下所示，因此在不做另行设置的情况下，将按照 1.6 版本编译。

```xml
<properties>
  <java.version>1.6</java.version>
  <resource.delimiter>@</resource.delimiter> <!-- delimiter that doesn't clash with Spring ${} placeholders -->
  <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
  <maven.compiler.source>${java.version}</maven.compiler.source>
  <maven.compiler.target>${java.version}</maven.compiler.target>
</properties>
```

如果我们想将应用中的 API 部分使用 1.7 版本，而实现部分用 1.8 版本，应该怎么实现呢？

- 首先我们在应用的根 pom 中将默认编译版本修改为 1.8

```xml
<properties>
  <!-- 修改默认版本编译为 1.8 -->
  <java.version>1.8</java.version>
  <!-- API 统一使用 1.7 编译 -->
  <api.jdk.version>1.7</api.jdk.version>
</properties>
```

- 然后对 api 的 pom 文件进行单独进行配置

```xml
<build>
  <plugins>
    <!-- 指定编译 JDK 版本 -->
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <configuration>
        <source>${api.jdk.version}</source>
        <target>${api.jdk.version}</target>
        <encoding>${project.build.sourceEncoding}</encoding>
      </configuration>
    </plugin>
  </plugins>
</build>
```