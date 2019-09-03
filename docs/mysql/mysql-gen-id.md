# 基于 MySQL 的分布式 ID 生成服务
---

分布式 ID 生成服务在业务系统开发中经常会用到，不过一般都会作为基础服务存在，大多数情况下不需要自己去造一个轮子出来。由于全局 ID 一般业务系统是用来作为数据库的主键进行存储的，因此不能有重复；我们知道数据库（InnoDB）会为主键 建立聚簇索引，也就是说数据库的物理存储会和 ID 的顺序保持一致，为了更方便的支持一些分页或者排序的业务需求，最好 ID 能够是趋势递增的。因此我们对于分布式 ID 生成服务主要会有以下两个要求：

- 全局唯一
- 趋势有序


[分布式 ID 生成器](https://mp.weixin.qq.com/s?__biz=MjM5ODYxMDA5OQ%3D%3D&mid=2651960245&idx=1&sn=5cef3d8ca6a3e6e94f61e0edaf985d11&chksm=bd2d06698a5a8f7fc89056af619b9b7e79b158bceb91bdeb776475bc686721e36fb925904a67&mpshare=1&scene=1&srcid=0626i5Oy0egfPAKrFc5VFrAK) 这篇文章对常见的实现方案做了一下总结。无外乎以下几种：

1. ** 使用数据库的 auto_increment 来生成全局唯一递增 ID**。实现简单，但扩展性差，写入单点，性能有上限，并且可用性无法保证。
2. ** 单点批量 ID 生成服务 **。数据库使用双主保证可用性，数据库中只存储当前 ID 的最大值，每次批量获取 ID 放在缓存中，用完了再取不用每次都访问 DB，这样既可以保证 ID 绝对有序，也大大降低了数据库的压力。但也要意识到此方案依然强依赖 DB、生成的 ID 虽然绝对递增但是可能不连续。
3. **uuid/guid**。不依赖远程服务完全本地化，基本没有性能上限。但缺点明显，无法做到趋势递增、字符串做主键效率低。
4. ** 取当前毫秒数 **。这种方法既能保证递增，又是本地服务，看上去好像简单而实用。确实一些简单的场景可以用，但它致命缺点是无法保证唯一性，因为它依赖机器时钟，并且理论并发量不能超过 1000.
5. **类 snowflake 算法**。一种 Twitter 开源的分布式 ID 生成算法，其核心思想其实就是结合毫秒数、机器编号、随机序列号等方式尽可能的避免 ID 重复，又能保证趋势递增。目前很多开源的方案都是基于这一思想的实现。

附一些开源实现，在以后的业务使用中建议参考一下这些开源代码，毕竟公司有的组件和代码已经很老了，慢慢的肯定有些不合理和可以优化的地方，不能闭着眼睛直接就拿来用：

- [Leaf：美团分布式 ID 生成服务开源](https://tech.meituan.com/2019/03/07/open-source-project-leaf.html)
  - 提供两种实现方案可供选择：号段模式 & snowflake 模式，[github 地址](https://github.com/Meituan-Dianping/Leaf)
- [百度 UidGenerator](https://github.com/baidu/uid-generator/blob/master/README.zh_cn.md)
  - 基于 Snowflake 算法的唯一 ID 生成器


上面介绍了这么多方法，其实抛开业务场景没有绝对好坏，因此使用的时候需要结合实际场景进行选择。我们在做评价系统组件化的时候 MySQL 做了分库分表，线上使用的便是其中第 2 种方案，美团开源的 Leaf 也支持这种方案（号段模式），有空可以学习一下其中的实现做一下对比。下面简单介绍一下我们使用的一些实现细节。



首先创建表结构如下：

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/9c415b81-45d8-49f7-9c91-99e5729d51bc)


通过 DataSource（可使用连接池如 druid）配置一个 SequenceUtil

```java
@Bean(name ="sequenceUtil")
	public SequenceUtil sequenceUtil(@Qualifier("sequenceUtilDataSource") DataSource sequenceUtilDataSource){SequenceUtil  sequenceUtil1 = new SequenceUtil();
		Sequence defaultSequenct = new Sequence();
		defaultSequenct.setDataSource(sequenceUtilDataSource);
		defaultSequenct.setBlockSize(30);
		defaultSequenct.setStartValue(20000000);
		sequenceUtil1.setDefaultSequence(defaultSequenct);
		return sequenceUtil1;
	}

// 使用时
long id= sequenceUtil.get(SequenceKeyEnum.COMMENT.getKey());
```

来看看 SequenceUtil 的具体实现


```java
public class SequenceUtil {
    private Sequence defaultSequence;
    public void setDefaultSequence(Sequence defaultSequence) {this.defaultSequence = defaultSequence;}
    public long get(String name) {if (defaultSequence != null) {return defaultSequence.get(name);
        } else {throw new RuntimeException("sequence "+ name +" undefined!");}
    }
}
```

再来看看 Sequence 类里的 get 方法具体实现（代码比较老，有很多可以优化的地方）。大概原理就是每次从库里取 blockSize 个 id 出来，缓存一个 stepMap 中，每次业务调用 get 取 id 时先看 stepMap 缓存中还有没有，有则 incrementAndGet，没有再去库里取一批出来。

注意 key 可能不连续，如果取了 5 个出来缓存到 map，只用了其中 2 个，此时应用重启 map 缓存丢失，会去重新从库里去，但是可以保证递增

另外注意 get 方法一定要 synchronized ，避免并发导致 id 重复，明显这种方案吞吐量不高，在单个业务中使用没问题，但是要作为公共服务提供出供多个业务方使用可能就有性能瓶颈了

```java
public class Sequence {private final static Log log = LogFactory.getLog(Sequence.class);
    private int blockSize = 5;
    private long startValue = 0;
    private final static String GET_SQL = "select id from sequence_value where name = ?";
    private final static String NEW_SQL = "insert into sequence_value (id,name) values (?,?)";
    private final static String UPDATE_SQL = "update sequence_value set id = ?  where name = ? and id = ?";

    private Map<String,Step> stepMap = new HashMap<String, Step>();

    private boolean getNextBlock(String sequenceName, Step step) {Long value = getPersistenceValue(sequenceName);
        if (value == null) {try {value = newPersistenceValue(sequenceName); 
            } catch (Exception e) {log.error("newPersistenceValue error!");
                value = getPersistenceValue(sequenceName); 
            }
        }
        boolean b = saveValue(value,sequenceName) == 1;
        if (b) {step.setCurrentValue(value);
            step.setEndValue(value+blockSize);
        }
        return b;
    }

    public synchronized long get(String sequenceName) {Step step = stepMap.get(sequenceName);
        if(step ==null) {step = new Step(startValue,startValue+blockSize);
            stepMap.put(sequenceName, step);
        } else {if (step.currentValue < step.endValue) {return step.incrementAndGet();
            }
        }
        for (int i = 0; i < blockSize; i++) {if (getNextBlock(sequenceName,step)) {return step.incrementAndGet();
            }
        }
        throw new RuntimeException("No more value.");
    }

    private int saveValue(long value, String sequenceName) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {connection = dataSource.getConnection();
            statement = connection.prepareStatement(UPDATE_SQL);
            statement.setLong(1, value + blockSize);
            statement.setString(2, sequenceName);
            statement.setLong(3, value);
            return statement.executeUpdate();} catch (Exception e) {log.error("newPersistenceValue error!", e);
            throw new RuntimeException("newPersistenceValue error!", e);
        } finally {if (statement != null) {try {statement.close();
                } catch (SQLException e) {log.error("close statement error!", e);
                }
            }
            if (connection != null) {try {connection.close();
                } catch (SQLException e) {log.error("close connection error!", e);
                }
            }
        }
    }

    private Long getPersistenceValue(String sequenceName) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {connection = dataSource.getConnection();
            statement = connection.prepareStatement(GET_SQL);
            statement.setString(1, sequenceName);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {return resultSet.getLong("id");}
        } catch (Exception e) {log.error("getPersistenceValue error!", e);
            throw new RuntimeException("getPersistenceValue error!", e);
        } finally {if (resultSet != null) {try {resultSet.close();
                } catch (SQLException e) {log.error("close resultset error!", e);
                }
            }
            if (statement != null) {try {statement.close();
                } catch (SQLException e) {log.error("close statement error!", e);
                }
            }
            if (connection != null) {try {connection.close();
                } catch (SQLException e) {log.error("close connection error!", e);
                }
            }
        }
        return null;
    }

    private Long newPersistenceValue(String sequenceName) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {connection = dataSource.getConnection();
            statement = connection.prepareStatement(NEW_SQL);
            statement.setLong(1, startValue);
            statement.setString(2, sequenceName);
            statement.executeUpdate();} catch (Exception e) {log.error("newPersistenceValue error!", e);
            throw new RuntimeException("newPersistenceValue error!", e);
        } finally {if (statement != null) {try {statement.close();
                } catch (SQLException e) {log.error("close statement error!", e);
                }
            }
            if (connection != null) {try {connection.close();
                } catch (SQLException e) {log.error("close connection error!", e);
                }
            }
        }
        return startValue;
    }



    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {this.dataSource = dataSource;}

    public void setBlockSize(int blockSize) {this.blockSize = blockSize;}

    public void setStartValue(long startValue) {this.startValue = startValue;}

    static class Step {
        private long currentValue;
        private long endValue;

        Step(long currentValue, long endValue) {
            this.currentValue = currentValue;
            this.endValue = endValue;
        }

        public void setCurrentValue(long currentValue) {this.currentValue = currentValue;}

        public void setEndValue(long endValue) {this.endValue = endValue;}

        public  long incrementAndGet() {return ++currentValue;}
    }
}
```

## 参考

- [UidGenerator](https://github.com/baidu/uid-generator/blob/master/README.zh_cn.md)
- [Leaf](https://github.com/Meituan-Dianping/Leaf)
- [Leaf：美团分布式ID生成服务开源](https://tech.meituan.com/2019/03/07/open-source-project-leaf.html)
- [分布式ID生成器](https://mp.weixin.qq.com/s?__biz=MjM5ODYxMDA5OQ%3D%3D&mid=2651960245&idx=1&sn=5cef3d8ca6a3e6e94f61e0edaf985d11&chksm=bd2d06698a5a8f7fc89056af619b9b7e79b158bceb91bdeb776475bc686721e36fb925904a67&mpshare=1&scene=1&srcid=0626i5Oy0egfPAKrFc5VFrAK)
