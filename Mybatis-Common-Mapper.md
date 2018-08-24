### 通用mapper（common mapper）
在前面只是用`mybatis-spring-boot-starter`的时候需要通过注解或者配置文件将sql和对应的方法映射起来，并且两种方式都需要手动写sql，注解的方式不够灵活，配置文件的形式又太繁琐。于是便出现了通用mapper，通用mapper本身已经提供单表的增删改查通用方法，无需每次都去写sql或者配置xml，只需要简单继承它提供的通用mapper接口即可轻松获得所有方法。

需要注意的是，通用mapper目前还只支持单表操作，不支持通用的多表联合查询。


### spring boot 集成通用mapper

通用mapper的作者为了便于spring boot用户集成开发了一个专门的[`mapper-spring-boot-starter`](https://github.com/abel533/mapper-boot-starter)用于集成到springboot，只需要添加以下依赖即可，该依赖集成了`mybatis-spring-boot-starter`和`通用mapper`依赖。另外如果需要用到分页插件还需要添加`pagehelper-spring-boot-starter`依赖。

common-mapper & pagehelper & mysql的完成依赖如下。

```xml
<!-- MySQL JDBC Type 4 driver -->
<dependency>
	<groupId>mysql</groupId>
	<artifactId>mysql-connector-java</artifactId>
</dependency>
<!--common mapper （包含了mybatis-spring-boot-starter依赖）-->
<dependency>
    <groupId>tk.mybatis</groupId>
    <artifactId>mapper-spring-boot-starter</artifactId>
    <version>1.1.4</version>
</dependency>
<!--pagehelper 通用mapper分页插件-->
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper-spring-boot-starter</artifactId>
    <version>1.1.3</version>
</dependency>
```

### 使用通用mapper

#### 配置

如果在没有集成pagehelper的情况下，通用mapper的starter自动配置除数据库账号信息以外无需任何额外配置即可启动成功并使用，如果添加了分页插件则需要在配置文件中对其指定数据库类型才能正常使用`pagehelper.helperDialect=mysql`。

因此在以上依赖的情况下配置文件如下即可使用

```properties
#-----------database configurations----------
spring.datasource.url=jdbc:MySql://192.168.192.125:3358/spring_boot_test?useUnicode=true
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
#-----------mybatis common-mapper configurations----------
pagehelper.helperDialect=mysql
```

#### 继承通用的Mapper<T>获得所有通用方法

```java
public interface CountryMapper extends MyMapper<Country>{
	//其他手写的接口...
}

//其中MyMapper接口如下，MySqlMapper包含了MySql独有的通用方法
public interface MyMapper<T> extends Mapper<T>, MySqlMapper<T> {

}
```

*这里要特别注意 MyMapper 不要和别的 Mapper 放在一个包下，要不然启动会报错*

> sun.reflect.generics.reflectiveObjects.TypeVariableImpl cannot be cast to java.lang.Class

#### 创建泛型实体

```java
@Table(name = "country")
public class Country extends BaseEntity {
    private String countryname;
    private String countrycode;

    public String getCountryname() {
        return countryname;
    }

    public void setCountryname(String countryname) {
        this.countryname = countryname;
    }

    public String getCountrycode() {
        return countrycode;
    }

    public void setCountrycode(String countrycode) {
        this.countrycode = countrycode;
    }
}

//其中BaseEntity将一些共用的字段抽离，定义如下
public class BaseEntity {
    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Transient
    private Integer page = 1;

    @Transient
    private Integer rows = 10;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }
}

```

这里要特别注意的是：
1. 使用@Transient注解可以忽略字段，添加该注解的字段不会作为表字段使用.
2. 由于基本类型,如int作为实体类字段时会有默认值0,而且无法消除,所以实体类中建议不要使用基本类型.
3. 表名可以使用@Table(name = "tableName")进行指定

#### 扫描继承的mapper接口

传统的xml配置中可以添加以下配置到配置文件

```xml
<bean class="tk.mybatis.spring.mapper.MapperScannerConfigurer">
  <property name="basePackage" value="com.isea533.mybatis.mapper"/>
</bean>
```

但是springboot提倡零xml配置，因此只需要在主配置类上添加一个注解扫描所有接口即可。

```java
@MapperScan(basePackages = "com.jverson.springboot.mapper")
```

#### 代码中使用

直接在需要的地方注入Mapper继承的接口即可,和一般情况下的使用没有区别.

```java
@Service
public class CountryService {

	@Autowired CountryMapper countryMapper;
	
	public List<Country> getAll(Country country){
		if (country.getPage()!=null && country.getRows()!=null) {
			PageHelper.startPage(country.getPage(), country.getRows());
		}
		Example example = new Example(Country.class);
		Example.Criteria criteria = example.createCriteria();
		if (StringUtil.isNotEmpty(country.getCountryname())) {
			criteria.andLike("countryname", "%"+country.getCountryname());
		}
		if (StringUtil.isNotEmpty(country.getCountrycode())) {
			criteria.andLike("countrycode", "%"+country.getCountrycode());
		}
        return countryMapper.selectByExample(example);
	}
	
}
```

### references

关于mapper的详细用法就不搬运了，在下面的参考文档中都能找到。

- [官方文档地址](https://github.com/abel533/mapper-boot-starter)
- [github代码示例](https://github.com/abel533/MyBatis-Spring-Boot)
- [如何使用分页插件PageHelper](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/HowToUse.md)
- [Mapper3通用接口大全](http://git.oschina.net/free/Mapper/blob/master/wiki/mapper3/5.Mappers.md#mapper3%E9%80%9A%E7%94%A8%E6%8E%A5%E5%8F%A3%E5%A4%A7%E5%85%A8)