# mybatis (insert)
> 前面大致把mybatis如何进行查询语句的执行完整的分析了一遍(最后结果集解析未分析)，下面把mybatis的insert源码分析下


## 环境
与查询的环境一致.


`UserMapper` 在之前的基础上新增了一个 `insert` 方法

```java
public interface UserMapper {
    User query(Integer id);

    int insert(User user);

}
```

 `mapper.xml` 文件也要相应写出对应的sql通过加载 `configuration` 时解析 `mapper.xml` 注册到 `MapperRegistry` 

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.xugx.github.mybatis.debug.source.mapper.UserMapper">
    <!--<cache eviction="LRU"-->
           <!--type="org.apache.ibatis.cache.impl.PerpetualCache"-->
           <!--flushInterval="120000"-->
           <!--size="1024"-->
           <!--readOnly="true"/>-->
    <select id="query" resultType="com.xugx.github.mybatis.debug.source.entity.User" >
        select * from t_user where id = #{id}
    </select>
    <insert id="insert" parameterType="com.xugx.github.mybatis.debug.source.entity.User" >
        insert into t_user(name) value (#{name})
    </insert>
</mapper>
```

下面是对应的测试方法:

```java
public class MybatisDebugSourceTest {
  @Test
    public  void insert() throws IOException {
        //sqlsession和userMapper的获取在query源码解析的时候都已经讲过
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper userMapper=sqlSession.getMapper(UserMapper.class);
        User user1 = new User();
        user1.setName("testName");
        //主要分析这句
        userMapper.insert(user1);
        //上面未设置自动提交，在这里手动提交
        sqlSession.commit();
    }
}
```

1. `query` 源码分析过 `userMapper.insert(user1);` 是注册 `mapper` 的时候就进行创建一个对应的 `MapperProxyFactory` 代理工厂 ，
2. 当`getMapper` 的时候就创建一个该 `mapper` 的代理类，代理类缓存有 `mapperMethod` ，`mapper` 方法的声明存放在 `MapperMethod` 中，当同一个代理类进行执行相同的 `mapperMethod` 时无需再次解析
3. 执行 `mapperMethod.execute` 方法去找到对应的命令，去执行对应的sqlsession方法。

这里执行的是 `INSERT` 

```java
public class MapperMethod {

 public Object execute(SqlSession sqlSession, Object[] args) {
    Object result;
    switch (command.getType()) {
      case INSERT: {
        //参数转换成key和value的映射， param1 args0 
    	Object param = method.convertArgsToSqlCommandParam(args);
        //通过sqlsession来执行对应的sql， command.getName()是statement，param是映射
        result = rowCountResult(sqlSession.insert(command.getName(), param));
        break;
      }
      default:
        throw new BindingException("Unknown execution method for: " + command.getName());
    }
    if (result == null && method.getReturnType().isPrimitive() && !method.returnsVoid()) {
      throw new BindingException("Mapper method '" + command.getName()
          + " attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
    }
    return result;
  }
}

```

主要的逻辑还是 `sqlSession.insert(command.getName(), param)` 方法。

`sqlSession.insert` 最终调用的是 `update` 方法

```java
public class DefaultSqlSession implements SqlSession {
 @Override
  public int update(String statement, Object parameter) {
    try {
      dirty = true;
      //从configuration中通过方法的statement获取到对应mapper.xml的statement
      MappedStatement ms = configuration.getMappedStatement(statement);
      //交给执行器执行其update方法
      return executor.update(ms, wrapCollection(parameter));
    } catch (Exception e) {
      throw ExceptionFactory.wrapException("Error updating database.  Cause: " + e, e);
    } finally {
      ErrorContext.instance().reset();
    }
  }
}
```

1. 通过 `MapperMethod` 中的`sqlCommand.name` 方法的全限定名，来获取到 `configuration` 中的 `mapper.xml` 方法statement
2. 通过 `wrapCollection` 来对参数为数组或者集合未使用 `@param` 的进行一个映射
3. 执行器执行 `update` 方法

执行器的还以 `cachingExecutor` 来说

```java
public class CachingExecutor implements Executor {

    public int update(MappedStatement ms, Object parameterObject) throws SQLException {
        //清除二级缓存，一方给查询的语句造成脏数据
        flushCacheIfRequired(ms);
        //执行更新语句
        return delegate.update(ms, parameterObject);
    }
    private void flushCacheIfRequired(MappedStatement ms) {
        //如果有缓存且刷新缓存
        Cache cache = ms.getCache();
        if (cache != null && ms.isFlushCacheRequired()) {
        tcm.clear(cache);
        }
    }
}
```
1. 如果是更新语句，首先判断是否刷新缓存, `baseExecutor.update`清空一级缓存,防止查询出脏数据
2. 委托给 `simpleExecutor` 来进行执行 `doUpdate`



```java
public class SimpleExecutor extends BaseExecutor {

  public int doUpdate(MappedStatement ms, Object parameter) throws SQLException {
    Statement stmt = null;
    try {
      //获取配置信息
      Configuration configuration = ms.getConfiguration();
      //创建 StatementHandler，解析了sql创建了parameterHandler resultSetHandler 等两个处理器，并执行插件
      StatementHandler handler = configuration.newStatementHandler(this, ms, parameter, RowBounds.DEFAULT, null, null);
      stmt = prepareStatement(handler, ms.getStatementLog());
      return handler.update(stmt);
    } finally {
      closeStatement(stmt);
    }
  }
}

```















































