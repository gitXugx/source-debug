


## 

```java
String resource = "org/mybatis/example/mybatis-config.xml";
InputStream inputStream = Resources.getResourceAsStream(resource);
SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
```
把配置文件读成输入流，然后使用SqlSessionFactoryBuilder来构建SqlSessionFactory对象。

```java
//该类是用来获取SqlSession的
public interface SqlSessionFactory {

  SqlSession openSession();

  //自动提交
  SqlSession openSession(boolean autoCommit);
  //连接
  SqlSession openSession(Connection connection);
  //带事务隔离级别
  SqlSession openSession(TransactionIsolationLevel level);
  //执行器的类型
  SqlSession openSession(ExecutorType execType);
  SqlSession openSession(ExecutorType execType, boolean autoCommit);
  SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level);
  SqlSession openSession(ExecutorType execType, Connection connection);

  Configuration getConfiguration();

}

```
该类主要是创建 `SqlSession` 来进行连接数据库执行sql。

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
  PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="org.mybatis.example.BlogMapper">
  <select id="selectBlog" resultType="Blog">
    select * from Blog where id = #{id}
  </select>
</mapper>
```
该xml是对应的sql语句和配置。

```java
SqlSession session = sqlSessionFactory.openSession();
try {
  Blog blog = (Blog) session.selectOne("org.mybatis.example.BlogMapper.selectBlog", 101);
} finally {
  session.close();
}
```

执行是要先获取 `SqlSession` 然后进行调用 `selectOne` 传入对应的Mapper和参数，进行执行获取对应的结果，这种方式可能不是太常见。

```java
BlogMapper mapper = session.getMapper(BlogMapper.class);
Blog blog = mapper.selectBlog(101);
```
也可以使用 `session.getMapper(BlogMapper.class)` 来获取对应的mapper对象，然后调用其方法。

到底 `sqlSessionFactory.openSession();` 是怎样获取的 `SqlSession`的呢
```java
public class DefaultSqlSessionFactory implements SqlSessionFactory {

  private SqlSession openSessionFromDataSource(ExecutorType execType, TransactionIsolationLevel level, boolean autoCommit) {
    Transaction tx = null;
    try {
        //获取其环境信息
      final Environment environment = configuration.getEnvironment();
      //根据环境配置获取一个事务工厂，如果环境中没有配置事务工厂，这时使用默认的
      final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
      //通过对应的事务工厂来产生一个事务
      tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
      //生成一个执行器(事务包含在执行器里)，里面有拦截器，用户可以写对应的拦截器来进行对Executor改造,如果开启缓存,里面就是有缓存的Executor，使用包装者模式
      final Executor executor = configuration.newExecutor(tx, execType);
      //然后产生一个DefaultSqlSession
      return new DefaultSqlSession(configuration, executor, autoCommit);
    } catch (Exception e) {
      //如果打开session出错，则关闭它的事务连接
      closeTransaction(tx); 
      throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
    } finally {
      //最后清空错误上下文
      ErrorContext.instance().reset();
    }
  }
}
```
获取到默认的 `DefaultSqlSession` 然后执行其 `selectOne` 方法，或者获取其 `mapper`对象。

那就先看 `selectOne`的实现:

```java
public class DefaultSqlSession implements SqlSession {
  @Override
  public <T> T selectOne(String statement, Object parameter) {
    //根据查询出来的结果进行处理。主要调用的 selectList
    List<T> list = this.<T>selectList(statement, parameter);
    //如果查询出来一个则返回结果
    if (list.size() == 1) {
      return list.get(0);
    //多个则抛错  
    } else if (list.size() > 1) {
      throw new TooManyResultsException("Expected one result (or null) to be returned by selectOne(), but found: " + list.size());
    } else {
      //没有则返回null
      return null;
    }
  }
}
```

可以看出来 `selectOne` 在 `DefaultSqlSession` 中的实现主要是 `selectList` 根据结果再来判断是不是一个。如果查询多个的话就直接 `selectList`，根据返回值的不同来给出不同的结果。


在去看一下 `selectList` 到底是怎么调用的。

```java
public class DefaultSqlSession implements SqlSession {
  @Override
  public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
    try {
      //根据statement id找到对应的MappedStatement
      MappedStatement ms = configuration.getMappedStatement(statement);
      //wrapCollection(parameter)这里面做了个集合名字的映射，这也就是为什么我们参数是collection的时候，xml中是List时候是list，数组时是 array参数
      //使用执行器执行查询
      return executor.query(ms, wrapCollection(parameter), rowBounds, Executor.NO_RESULT_HANDLER);
    } catch (Exception e) {
      throw ExceptionFactory.wrapException("Error querying database.  Cause: " + e, e);
    } finally {
      ErrorContext.instance().reset();
    }
  }
}
```
`selectList` 方法通过配置对象 `configuration`拿到 `MappedStatement` 找到对应的sql，然后使用执行器调用对应的sql。用到的有mapper的声明(包括sql)，sql的参数，分页对象，resultHander。executor当时在获取的时候，有多种，如果缓存开启，都会被包装成带缓存的执行器。那下面看看 `CachingExecutor` 二级缓存的实现。

```java
public class CachingExecutor implements Executor {
  @Override
  public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
    //获取到处理过的sql对象(变量都已经替换成?了)
    boundSql = ms.getBoundSql(parameterObject);
	//创建缓存的key其实还是调用的原本的BaseExecutor的createCacheKey
    CacheKey key = createCacheKey(ms, parameterObject, rowBounds, boundSql);
    return query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
  }

    @Override
  public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql)
      throws SQLException {
    Cache cache = ms.getCache();
    //先查询缓存，查不到在给实际的执行器去查
    if (cache != null) {
      flushCacheIfRequired(ms);
      //是否开启缓存
      if (ms.isUseCache() && resultHandler == null) {
        ensureNoOutParams(ms, parameterObject, boundSql);
        //从缓存中拿，如果拿不到
        List<E> list = (List<E>) tcm.getObject(cache, key);
        if (list == null) {
          //则交给执行器来进行执行  
          list = delegate.<E> query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
          //查出的结果方法到缓存中
          tcm.putObject(cache, key, list); // issue #578 and #116
        }
        return list;
      }
    }
    //如果没有缓存则，交给执行器查找
    return delegate.<E> query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
  }
}
```

1. 把带变量的sql进行静态处理，替换成? ，存放到 `boundSql` 对象中
2. 组装cacheKey，看是否需要从缓存中获取结果
3. 如果缓存中有就直接从缓存中拿，如果没有先查，然后存放缓存。
4. 如果没有开启cache，则直接查询。


```java
public abstract class BaseExecutor implements Executor {
 @Override
  public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
    ErrorContext.instance().resource(ms.getResource()).activity("executing a query").object(ms.getId());
    //如果执行器已经关闭，报错
    if (closed) {
      throw new ExecutorException("Executor was closed.");
    }
    //先清局部缓存
    if (queryStack == 0 && ms.isFlushCacheRequired()) {
      clearLocalCache();
    }
    List<E> list;
    try {
      //加一,这样递归调用到上面的时候就不会再清局部缓存了
      queryStack++;
      //先根据cachekey从localCache去查
      list = resultHandler == null ? (List<E>) localCache.getObject(key) : null;
      if (list != null) {
        //若查到localCache缓存，处理localOutputParameterCache
        handleLocallyCachedOutputParameters(ms, key, parameter, boundSql);
      } else {
        //从数据库查
        list = queryFromDatabase(ms, parameter, rowBounds, resultHandler, key, boundSql);
      }
    } finally {
      //清空堆栈
      queryStack--;
    }
    if (queryStack == 0) {
      //延迟加载队列中所有元素
      for (DeferredLoad deferredLoad : deferredLoads) {
        deferredLoad.load();
      }
      // issue #601
      //清空延迟加载队列
      deferredLoads.clear();
      if (configuration.getLocalCacheScope() == LocalCacheScope.STATEMENT) {
        // issue #482
    	//如果是STATEMENT，清本地缓存
        clearLocalCache();
      }
    }
    return list;
  }
}

```


















