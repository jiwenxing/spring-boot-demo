Spring Boot 可以将应该打包成可执行的 jar 包，该 jar 文件可以在生产环境运行，可执行 jars（也称为"fat jars"）是包含你的编译后的类和你的代码运行所需的依赖 jar 的存档。

### 构建可执行 jar

为了创建可执行的jar，需要将`spring-boot-maven-plugin`添加到我们的 pom.xml 中。在 dependencies 节点下插入以下内容，此时可以根据 package 的设置打包成 jar 或者传统的 war 包形式。

```xml
<packaging>jar</packaging>
<packaging>war</packaging>

<plugins>
  <plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
  </plugin>
  <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
      <skipTests>true</skipTests>
    </configuration>
  </plugin>
</plugins>
```

> `spring-boot-starter-parent` POM包含用于绑定 repackage 目标的`<executions>`配置。如果你不使用 parent POM，你将需要自己声明该配置。具体参考[插件文档](http://docs.spring.io/spring-boot/docs/1.3.0.BUILD-SNAPSHOT/maven-plugin/usage.html)。

此时执行 maven clean install 之后便会在 target 目录里看到 hello-world.jar 以及一个 hello-world.jar.original 的文件。其中 *.original 文件是 Spring Boot 打包之前 Maven 创建的原始 jar 文件，该文件很小是一个标准的 jar 包，其并不包含其它的依赖文件。

### 运行可执行 jar

```ba
java -jar target/hello-world.jar
```

如果服务器安装了 JDK，则直接运行上面的命令即可启动应用，不需要额外的安装 Tomcat 等，因为 Spring Boot 自带了容器并将其打包进去。当然为了构建一个即是可执行的，又能部署到一个外部容器的 war 文件，需要标记内嵌容器依赖为 "provided"。

```xm
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-tomcat</artifactId>
  <scope>provided</scope>
</dependency>
```



### 后台启动应用

按照上面的方法在 terminal 中启动应用后，关闭 terminal 时应用也会终止，如果我们想将应用以后台进程的形式启动，命令如下：

```ba
nohup java -jar shareniu.jar &
```

这时启动完成以后执行 `ps -A（ef） | grep java` 可以看到应用启动后的 pid，如果需要重新上传 jar 并重启，则依次执行以下命令

```ba
rz #上传新的 jar 包
ps -A（ef） | grep java  #查看进程 pid
kill -9 pid #直接杀死进程
nohup java -jar shareniu.jar & #再次启动
```

上面的步骤还是挺繁琐的，如果是测试环境需要频繁的更新重启。于是我们想到可以使用脚本来简化这一过程，在 jar 包目录下创建一个 `sb.sh` 的脚本文件，写入以下内容：

```bash
#! /bin/bash
# springboot的jar放同级目录下，且只能有一个jar文件
export PATH=$JAVA_HOME/bin:$PATH
CURRENT_PATH=$(cd "$(dirname "$0")"; pwd)
# 搜索当前路径下jar，得到的是JAR的完整路径（e.g. /export/App/hello-world.jar），
JAR_PATH=$(find $CURRENT_PATH -maxdepth 1 -name "*.jar")
# 这里只需要JAR包名称（e.g. hello-world.jar）
JAR=${JAR_PATH##*/}
# 得到当前的进程pid
PID=$(ps -ef | grep $JAR | grep -v grep | awk '{ print $2 }')
# echo "current pid is $PID"

case "$1" in
    start)
        if [ ! -z "$PID" ]; then
            echo "$JAR 已经启动，进程号: $PID"
        else
            echo -n -e "启动 $JAR ... \n"
            cd $CURRENT_PATH
        nohup java -jar $JAR >/dev/null 2>&1 &
            if [ "$?"="0" ]; then
                echo "启动完成，请查看日志确保成功"
            else
                echo "启动失败"
            fi
        fi
        ;;
    stop)
        if [ -z "$PID" ]; then
            echo "$JAR 没有在运行，无需关闭"
        else
            echo "关闭 $JAR ..."
              kill -9 $PID
            if [ "$?"="0" ]; then
                echo "服务已关闭"
            else
                echo "服务关闭失败"
            fi
        fi
        ;;
    restart)
        ${0} stop
        ${0} start
        ;;
    kill)
        echo "强制关闭 $JAR"
        killall $JAR
        if [ "$?"="0" ]; then
            echo "成功"
        else
            echo "失败"
        fi
        ;;
    status)
        if [ ! -z "$PID" ]; then
            echo "$JAR 正在运行"
        else
            echo "$JAR 未在运行"
        fi
        ;;
  *)
    echo "Usage: ./sb {start|stop|restart|status|kill}" >&2
        exit 1
esac
```

上面的脚本还是很容易理解，就不多解释了。创建完后执行 `chmod a+x sb.sh` 修改脚本执行权限， 这时便可以使用脚本进行各种操作了

```ba
./sb.sh start 
./sb.sh stop
./sb.sh restart
./sb.sh status
./sb.sh kill
```



### 参考

- [Springboot 启动脚本](https://www.pocketdigi.com/20180127/1592.html)

- [Create a Deployable War File](https://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#howto-create-a-deployable-war-file)

