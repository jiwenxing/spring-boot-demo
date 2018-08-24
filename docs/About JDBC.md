
# About JDBC
---

## 什么是JDBC

Java数据库连接（Java Database Connectivity）是Java语言中用来规范客户端程序如何来访问数据库的应用程序接口，提供了诸如查询和更新数据库中数据的方法。是面向关系型数据库的。

JDBC规范采用接口和实现分离的思想设计了Java数据库编程的框架，API主要位于JDK中的java.sql包中（之后扩展的内容位于javax.sql包中），主要包括（其中接口需要驱动提供者实现）：

- DriverManager：负责加载各种不同驱动程序（Driver），并根据不同的请求，向调用者返回相应的数据库连接（Connection）。（注：DataSource 接口是 JDBC 2.0 API 中的新增内容，它提供了连接到数据源的另一种方法以替代DriverManager。使用 DataSource 对象是连接到数据源的首选方法。）
- Driver（接口）：驱动程序
- Connection（接口）：数据库连接，负责进行与数据库间的通讯
- Statement（接口）：用以执行SQL查询和更新（针对静态SQL语句和单次执行）
- PreparedStatement（接口）：用以执行包含动态参数的SQL查询和更新（在服务器端编译，允许重复执行以提高效率）
- javax.sql.DataSource（接口）：该工厂用于提供到此DataSource对象所表示的物理数据源的连接（A factory for connections to the physical data source that this DataSource object represents.）。作为DriverManager工具的替代项，DataSource对象是获取连接的首选方法。实现DataSource接口的对象通常在基于JavaTM Naming and Directory Interface(JNDI) API的命名服务中注册。

## JDBC连接数据库

JDBC连接数据库主要分为DriverManager方式和实现了DataSource接口的连接池方式。

## 使用java.sql.DriverManager类

1. 利用Class.forName()方法来加载JDBC驱动程序（Driver），加载成功后会自动将Driver类的实例注册到DriverManager类中。
2. 从DriverManager中，通过JDBC URL、用户名、密码来获取相应的数据库连接（Connection）。
3. 在获取Connection之后，便可以创建Statement（或PreparedStatement）用以执行SQL语句。
4. 查询（SELECT）的结果存放于结果集（ResultSet）中，可以按照顺序依次访问。

```java
public static void main(String[] args) {
	Statement stmt = null;
	Connection conn = null;
	ResultSet rs = null;
	try {
		Class.forName("com.mysql.jdbc.Driver"); //加载JDBC驱动，成功后会自动将Driver类的实例注册到DriverManager中
		conn = DriverManager.getConnection( 
			      "jdbc:MySql://192.168.192.125:3358/spring_boot_test?useUnicode=true", // URL
			      "root", // 用户名
			      "123456" ); // 密码
		stmt = conn.createStatement();
//			stmt.executeUpdate( "INSERT INTO user( name, age ) VALUES ( 'iverson', 40 ) " );
		rs = stmt.executeQuery( "SELECT * FROM user" );
		while ( rs.next() ) {
		     int numColumns = rs.getMetaData().getColumnCount();
		     for ( int i = 1 ; i <= numColumns ; i++ ) {
		        // 与大部分Java API中下标的使用方法不同，字段的下标从1开始
		        System.out.println( "COLUMN " + i + " = " + rs.getObject(i) ); // 也可以使用ResultSet.getXXX()
		     }
		}
	} catch (SQLException e) {
		System.out.println("SQLException: " + e.getMessage());
	    System.out.println("SQLState: " + e.getSQLState());
	    System.out.println("VendorError: " + e.getErrorCode());
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
	} finally {
		if (stmt != null) {
			try {  
                stmt.close();  
            } catch (SQLException e) {  
                e.printStackTrace();  
                throw new RuntimeException(e);  
            }  
		}
		if(conn!=null) {
			try {  
                conn.close();  
            } catch (SQLException e) {  
                e.printStackTrace();  
                throw new RuntimeException(e);  
            } 
		}
	}
}
```

### 使用事务

[JDK文档](https://docs.oracle.com/javase/tutorial/jdbc/basics/transactions.html)中对事务有比较详细的解释。when you do not want one statement to take effect unless another one completes, The way to be sure that either both actions occur or neither action occurs is to use a transaction. A transaction is a set of one or more statements that is executed as a unit, so either all of the statements are executed, or none of the statements is executed.

使用事务原理很简单，When a connection is created, it is in auto-commit mode.The way to allow two or more statements to be grouped into a transaction is to disable the auto-commit mode（conn.setAutoCommit(false)）。在所有操作执行完成以后统一进行提交`conn.commit();`。

下面的代码有三个update操作，其中第二个试图去更新一个不存在的表user1会抛出异常。在不使用事务的时候，会发现第一条更新操作生效而后两条失败，如果使用了事务控制，则三条更新语句都不会生效。

在spring中使用事务只需要一个注解即可，原理都一样。具体可参考[Spring的参考文档](https://docs.spring.io/spring/docs/3.0.x/spring-framework-reference/html/transaction.html#transaction-declarative-annotations)

```java
Class.forName("com.mysql.jdbc.Driver"); //加载JDBC驱动，成功后会自动将Driver类的实例注册到DriverManager中
conn = DriverManager.getConnection( 
	      "jdbc:MySql://192.168.192.125:3358/spring_boot_test?useUnicode=true", // URL
	      "root", // 用户名
	      "123456" ); // 密码
conn.setAutoCommit(false); //關閉自動提交，默認開啟

stmt = conn.createStatement();
stmt.executeUpdate("UPDATE user SET age = 222 WHERE Name = 'james'");
stmt.executeUpdate("UPDATE user1 SET age = 2 WHERE Name = 'kobe'");
stmt.executeUpdate("UPDATE user SET age = 123 WHERE Name = 'iverson'");
conn.commit();
rs = stmt.executeQuery( "SELECT * FROM user" );
while ( rs.next() ) {
     int numColumns = rs.getMetaData().getColumnCount();
     for ( int i = 1 ; i <= numColumns ; i++ ) {
        // 与大部分Java API中下标的使用方法不同，字段的下标从1开始
        System.out.println( "COLUMN " + i + " = " + rs.getObject(i) ); // 也可以使用ResultSet.getXXX()
     }
}
```

Using Transactions：https://docs.oracle.com/javase/tutorial/jdbc/basics/transactions.html


## 使用实现了javax.sql.DataSource接口的子类（连接池实现）

作为DriverManager工具的替代项，DataSource对象是获取连接的首选方法，并且如果使用连接池技术，都需要实现javax.sql.DataSource接口。DataSource接口由驱动程序供应商实现。共有三种类型的实现：

1. 基本实现，生成标准的Connection对象
通过DataSource对象访问的驱动程序本身不会向DriverManager注册。通过查找操作获取DataSource对象，然后使用该对象创建Connection对象。使用基本的实现，通过 DataSource 对象获取的连接与通过 DriverManager 设施获取的连接相同。由于和DriverManager存在同样的性能问题这里不再讨论。

2. 连接池实现，生成自动参与连接池的Connection对象。此实现与中间层连接池管理器一起使用。

3. 分布式事务实现，生成一个Connection对象，该对象可用于分布式事务，大多数情况下总是参与连接池。此实现与中间层事务管理器一起使用，大多数情况下总是与连接池管理器一起使用。

DataSource是一个接口，那么它的实现者有哪些呢？大多数Web服务器都实现了DataSource接口，如WebLogic、Tomcat（7.0版本以前默认使用commons-dbcp，因为性能问题从7.0开始换成jdbc-pool（Tomcat connection pool），spring boot也是默认使用jdbc-pool），另外，一些开源组织也提供了DataSource的实现，如DBCP、C3P0和Druid（阿里巴巴）。由于dbcp和c3p0都是单线程的，在高并发的环境下性能会比较低下，jdbc-pool（Tomcat connection pool）则使用非常广泛，spring boot默认使用的就是jdbc-pool。

### Tomcat connection pool 编码方式实现

```java
public static void main(String[] args) {
	PoolProperties p = new PoolProperties();
    p.setUrl("jdbc:MySql://192.168.192.125:3358/spring_boot_test?useUnicode=true");
    p.setDriverClassName("com.mysql.jdbc.Driver");
    p.setUsername("root");
    p.setPassword("123456");
    p.setJmxEnabled(true);
    p.setTestWhileIdle(false);
    p.setTestOnBorrow(true);
    p.setValidationQuery("SELECT 1");
    p.setTestOnReturn(false);
    p.setValidationInterval(30000);
    p.setTimeBetweenEvictionRunsMillis(30000);
    p.setMaxActive(100);
    p.setInitialSize(10);
    p.setMaxWait(10000);
    p.setRemoveAbandonedTimeout(60);
    p.setMinEvictableIdleTimeMillis(30000);
    p.setMinIdle(10);
    p.setLogAbandoned(true);
    p.setRemoveAbandoned(true);
    p.setJdbcInterceptors(
      "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"+
      "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
    DataSource datasource = new DataSource();
    datasource.setPoolProperties(p);

    Connection con = null;
    try {
      con = datasource.getConnection();
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery("select * from user");
      int cnt = 1;
      while (rs.next()) {
          System.out.println((cnt++)+". name:" +rs.getString("name")+
            " age:"+rs.getString("age"));
      }
      rs.close();
      st.close();
    } catch (SQLException e) {
		e.printStackTrace();
	} finally {
      if (con!=null) try {con.close();}catch (Exception ignore) {}
    }
}
```


## References

- [The Tomcat JDBC Connection Pool](https://tomcat.apache.org/tomcat-7.0-doc/jdbc-pool.html)
- [JDBC连接池、监控组件 Druid](https://www.oschina.net/p/druid)
- [Using Transactions](https://docs.oracle.com/javase/tutorial/jdbc/basics/transactions.html)