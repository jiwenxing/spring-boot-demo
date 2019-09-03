# MySQL 索引及其优化
---

## 索引概述

索引是对数据库表中一列或多列的值进行排序的一种结构，使用索引可快速访问数据库表中的特定信息。索引就像是书的目录，可以通过目录快速查找书中指定内容的位置。

索引一般以文件形式存在磁盘中（也可以存于内存中），数据库在未添加索引的时候进行查询默认是全局扫描，而建立索引之后，会将索引字段 key 值放在一个 n 叉树上（BTree）。因为 B 树的特点就是适合在磁盘等直接存储设备上组织动态查找表，每次以索引进行条件查询时，会去树上根据 key 值直接进行搜索。

索引的好处很显然，可以显著提升查询、分组及排序的效率。但它同样会带来一些副作用，比如说占用更多的空间（索引其实就是空间换时间）、降低表增删改的效率等（同时要维护索引）。下面主要以 MySQL 为例讲解索引的相关知识。

## MySQL 索引类型

- **PRIMARY**，主键索引，索引列唯一且不能为空；一张表只能有一个主键索引（主键索引通常在建表的时候就指定）   
```sql
# 建表时指定主键即可创建主键索引（聚簇索引）
CREATE TABLE T_USER(ID INT NOT NULL,USERNAME VARCHAR(16) NOT NULL,PRIMARY KEY(ID))
```

- **NORMAL**，普通索引，索引列没什么限制。可以建表时创建也可随时添加，是最常用的索引    
```sql
# 建表时创建
CREATE TABLE T_USER(ID INT NOT NULL,USERNAME VARCHAR(16) NOT NULL,INDEX USERNAME_INDEX(USERNAME(16)))
# alter 后期添加
ALTER TABLE T_USER ADD INDEX U_INDEX(USERNAME)
# 删除索引
DROP INDEX U_INDEX ON t_user
```

- **UNIQUE**，唯一索引，索引列的值必须是唯一的，但允许有空值    
```sql
# 建表时创建
CREATE TABLE t_user(ID INT NOT NULL,USERNAME VARCHAR(16) NOT NULL,UNIQUE U_INDEX(USERNAME))
# alter 后期添加
ALTER TABLE t_user ADD UNIQUE u_index(USERNAME)
# 删除索引
DROP INDEX U_INDEX ON t_user
```

- **FULLTEXT**，全文索引，一般用的比较少，可以使用 solr 或者 ElasticSearch 等专门的全文搜索引擎替代

- ** 复合索引 **，复合索引是在多个字段上创建的索引。复合索引遵守 **“最左前缀” 原则**，即在查询条件中使用了复合索引的第一个字段，索引才会被使用。因此，在复合索引中索引列的顺序至关重要。    
```sql
# 方式 1
create index index_name on table_name(col_name1,col_name2,...);
# 方式 2
alter table table_name add index index_name(col_name,col_name2,...);
```

抛开不怎么用的 FULLTEXT 索引，可以说唯一索引是一种特殊的普通索引（唯一性约束），而主键索引则是一种特殊的唯一索引（非空约束）。平时最常接触的还是主键索引和普通索引，主键索引在我们创建表时指定了主键以后就自动存在了。而需要说明的可能就是普通索引和唯一索引的区别，两者的区别就是是否有唯一性约束，在可以确定字段不可能重复的情况下可以选择唯一索引，其它情况都选普通索引即可。实际上在许多场合，人们创建唯一索引的目的往往不是为了提高访问速度，而只是为了避免数据出现重复。

## MySQL 索引结构

1. HASH（用于对等比较，如 "=" 和 "<=>"）   //<=> 安全的比对，对 null 值比较，语义类似 is null（）
2. BTREE（用于非对等比较，比如范围查询）>，>=，<，<=、BETWEEN、Like

Innodb 和 MyISAM 默认的索引是 Btree 索引

## 索引的基本操作

```sql
# 查看表索引
show index from lj_house;
show keys from table_name; # 等同上面

# 创建索引
CREATE TABLE table_name[col_name data type][unique|fulltext][index|key][index_name](col_name[length])[asc|desc]

# 查看索引使用情况，
show status like 'Handler_read%';

# 删除索引
DROP INDEX index_name ON table_name

# 查看执行计划
explain select * from lj_house where house_url_id=10310179316;

```


## Explain 命令使用

使用 explain 查询和分析 SQl 的执行计划，可以进行 sql 的性能优化！关于 Explain 这部分详解可以参考 - [MySQL 5.7 - EXPLAIN Output Format](https://dev.mysql.com/doc/refman/5.7/en/explain-output.html)，写的很详细。

![](https://jverson.oss-cn-beijing.aliyuncs.com/f213eed6fc75476f360fdff37f963b1c.jpg)


通过上面的示例可以看到 explain 命令可以给出这条 sql 的很多信息，下面就来一一解释一下。

**select_type**，SELECT 类型，可以为以下任何一种:

- SIMPLE：简单 SELECT(不使用 UNION 或子查询)
- PRIMARY：最外面的 SELECT
- UNION：UNION 中的第二个或后面的 SELECT 语句
- DEPENDENT UNION：UNION 中的第二个或后面的 SELECT 语句, 取决于外面的查询
- UNION RESULT：UNION 的结果
- SUBQUERY：子查询中的第一个 SELECT
- DEPENDENT SUBQUERY：子查询中的第一个 SELECT, 取决于外面的查询
- DERIVED：导出表的 SELECT(FROM 子句的子查询)

**type**，联接类型。这是重要的列，显示连接使用了何种类型。从最好到最差的连接类型为 const、eq_ref、ref、range、index 和 ALL。下面给出各种联接类型，按照从最佳类型到最坏类型进行排序:

1. system：表仅有一行 (= 系统表)。这是 const 联接类型的一个特例。
2. const：表最多有一个匹配行, 它将在查询开始时被读取。因为仅有一行, 在这行的列值可被优化器剩余部分认为是常数。const 表很快, 因为它们只读取一次!
3. eq_ref：类似 ref，区别就在使用的索引是唯一索引，对于每个索引键值，表中只有一条记录匹配，简单来说，就是多表连接中使用 primary key 或者 unique key 作为关联条件。
4. ref：对于每个来自于前面的表的行组合, 所有有匹配索引值的行将从这张表中读取。
5. range：只检索给定范围的行，使用一个索引来选择行
6. index：Full Index Scan，index 与 ALL 区别为 index 类型只遍历索引树
7. ALL：Full Table Scan， MySQL 将遍历全表以找到匹配的行


**possible_keys**，指出 MySQL 能使用哪个索引在表中找到记录，查询涉及到的字段上若存在索引，则该索引将被列出，但不一定被查询使用


**Key**，key 列显示 MySQL 实际决定使用的键（索引），如果没有选择索引，键是 NULL。要想强制 MySQL 使用或忽视 possible_keys 列中的索引，在查询中使用 FORCE INDEX、USE INDEX 或者 IGNORE INDEX。

**key_len**，表示索引中使用的字节数，不损失精确性的情况下，长度越短越好 

**ref**，表示上述表的连接匹配条件，即哪些列或常量被用于查找索引列上的值

**rows**，表示 MySQL 根据表统计信息及索引选用情况，估算的找到所需的记录所需要读取的行数

**Extra**，该列包含 MySQL 解决查询的详细信息, 有以下几种情况：

- Using where: 列数据是从仅仅使用了索引中的信息而没有读取实际的行动的表返回的，这发生在对表的全部的请求列都是同一个索引的部分的时候，表示 mysql 服务器将在存储引擎检索行后再进行过滤
- Using temporary：表示 MySQL 需要使用临时表来存储结果集，常见于排序和分组查询
- Using filesort：MySQL 中无法利用索引完成的排序操作称为 “文件排序”
- Using join buffer：改值强调了在获取连接条件时没有使用索引，并且需要连接缓冲区来存储中间结果。如果出现了这个值，那应该注意，根据查询的具体情况可能需要添加索引来改进能。
- Impossible where：这个值强调了 where 语句会导致没有符合条件的行。


## 参考

- [MySQL 5.7 - EXPLAIN Output Format](https://dev.mysql.com/doc/refman/5.7/en/explain-output.html)


