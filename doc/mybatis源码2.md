
## 基础学习

深入 `mybatis` 的前提就是从基础开始，先学会怎么用。 现在 `Spring` 和 `SpringBoot` 对 `mybatis` 的支持很友好，几乎不需要我们的配置就可以用。屏蔽了对 `mybatis` 的操作。下面就先从最基础的原生 `mybatis` 开始。

**环境:**

主要: JDK1.8 , mybatis 3.5.0 

使用 `idea` 创建一个 `maven` 项目。

`maven` 主要引入的jar 
POM.xml:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>mybatis-debug</artifactId>
        <groupId>com.xugx.github</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>mybatis-debug-source</artifactId>

    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>3.8.1</version>
            <scope>test</scope>
        </dependency>
        <!-- https://mvnrepository.com/artifact/org.mybatis/mybatis -->
        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis</artifactId>
            <version>3.5.0</version>
        </dependency>
        <!-- https://mvnrepository.com/artifact/mysql/mysql-connector-java -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.7-dmr</version>
        </dependency>
        <!-- https://mvnrepository.com/artifact/log4j/log4j -->
        <dependency>
            <groupId>log4j</groupId>
            <artifactId>log4j</artifactId>
            <version>1.2.17</version>
        </dependency>
    </dependencies>
</project>
```
使用`mybatis` 需要创建log4j的日志配置。用来打印`mybatis`的日志

```properties
log4j.rootLogger=DEBUG,stdout
log4j.logger.org.mybatis=DEBUG
log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern=%d %5p %c: %m%n
```

数据库的信息用`properties` 存放,以给 `mybatis-config.xml` 读取:

```properties
driver=com.mysql.cj.jdbc.Driver
url= jdbc:mysql://localhost:3306/mybatis-debug
username=root
password=123456
```

配置`mybatis-config.xml` 文件，让`mybatis`来进行解析
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE configuration PUBLIC
        "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
    <properties resource="jdbc.properties"></properties>
    <environments default="development">
        <environment id="development">
            <transactionManager type="JDBC" />
            <dataSource type="POOLED">
                <property name="driver" value="${driver}" />
                <property name="url" value="${url}" />
                <property name="username" value="${username}" />
                <property name="password" value="${password}" />
            </dataSource>
        </environment>
    </environments>
    <!-- 这个是下面创建mapper.xml 使在解析的时候就addMapper-->
    <mappers>
        <mapper resource="mapper\UserMapper.xml" />
    </mappers>
</configuration>
```

`UserMapper.xml` 只进行一个查询语句:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<!-- 命名空间指向UserMapper -->
<mapper namespace="com.xugx.github.mybatis.debug.source.mapper.UserMapper">
    <select id="query" resultType="com.xugx.github.mybatis.debug.source.entity.User">
        select * from t_user where id = #{id}
    </select>
</mapper>
```

下面是实体类和`mapper`类:

```java
public interface UserMapper {
    User query(Integer id);
}

public class User {
    //get set 方法省略
    private Integer id;
    private String name;
}
```

上面都做好后，我们就只差查询数据库了:

```java
public class MybatisDebugSourceTest {

    public static void query() throws IOException {

        String resource = "mybatis-config.xml";
        //读取配置文件成二进制流
        InputStream inputStream = Resources.getResourceAsStream(resource);
        //解析xml,并创建SqlSessionFactory
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        //获取sqlsession
        SqlSession sqlSession = sqlSessionFactory.openSession();
        //从sqlsession中获取到注册的mapper
        UserMapper userMapper=sqlSession.getMapper(UserMapper.class);
        //执行sql
        User user=userMapper.query(1);
        User user2=userMapper.query(1);
        
    }
}
```

1. 根据读入的xml流，创建`SqlSessionFactory`
2. 通过`SqlSessionFactory` 获取 `SqlSession` 
3. 使用 `SqlSession` 得到注册的 `mapper`

知道上面大致的步骤后，深入了解下:

**SqlSessionFactory的创建**

通过IDE 进入到 `SqlSessionFactoryBuilder().build(inputStream)` 的`build` 方法

```java
public class SqlSessionFactoryBuilder {

  public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
    try {
      //1. 解析xmlconfig配置，里面比较复杂，最后创建Configuration对象。
      XMLConfigBuilder parser = new XMLConfigBuilder(inputStream, environment, properties);
      //2. 做一些解析处理和初始化Configuration信息，最后根据Configuration创建SqlSessionFactory
      return build(parser.parse());
    } catch (Exception e) {
      throw ExceptionFactory.wrapException("Error building SqlSession.", e);
    } finally {
      ErrorContext.instance().reset();
      try {
        inputStream.close();
      } catch (IOException e) {
        // Intentionally ignore. Prefer previous error.
      }
    }
  }
}
```

上面的 `1` 和 `2` 说的比较简单，下面用另一种方式来详细说明：

```java
/** 开始调用是**/
SqlSessionFactoryBuilder.build(Reader reader, String environment, Properties properties)
- XMLConfigBuilder.new
-- XPathParser.new
--- XPathParser.createDocument(new InputSource(reader)) /**根据输入流创建Document对象，使用的是dom解析方式**/
-- Configuration.new /** 创建Configuration对象，注册类型别名 **/
- XMLConfigBuilder.parse /**使用xpath解析document对象，解析的是mybatis-config.xml文件**/
-- XMLConfigBuilder.parseConfiguration /**解析mybatis-config.xml，把对应的数据存放到Configuration里面**/
//下面有很多解析的节点:properties ,类型别名,插件,对象工厂,设置,环境,类型处理器,mapper映射器等，下面只看其中几个
--- XMLConfigBuilder.pluginElement /**拦截器: 循环解析plugins有多少个子节点，使用反射创建Interceptor对象，然后设置properties，添加到configuration的拦截器中**/
--- XMLConfigBuilder.mapperElement /**mapper映射器: 因为mappers标签的子标签有3种方式：1.resource 2.绝对路径 3.包名。加载class方法不同，都是获取class对象 **/
---- configuration.addMapper /** 实际上调用的是mapperRegistery的addmapper **/
----- MapperRegistery.addMapper /** 看该类是不是接口，是不是已经被注册过，然后class作为key，创建的mapper代理对象作为value，添加到映射中。后续是解析mapper.xml和mapper中的注解解析 ，最后移除掉不必要的方法**/
DefaultSqlSessionFactory.build.DefaultSqlSessionFactory(Configuration).new
```

大致逻辑:
1. 解析 `mybatis-config.xml` 到 `Configuration`中。
2. 在解析的过程中要初始化 `Configuration` ，properties ,类型别名,插件,对象工厂,设置,环境,类型处理器,mapper映射器等
3. 在初始化 `Configuration` 的 `MapperRegistery` 时候还要进行解析 `mapper.xml` 和 `mapper`类，最后确保解析成功的 `mapper` class添加到映射表中
4. 初始化 `Configuration` 后，创建默认的 `DefaultSqlSessionFactory` 



**SqlSession的获取**

通过IDE 进入到 `sqlSessionFactory.openSession();` 的`openSession` 方法
因为上面获取的 `sqlSessionFactory`静态类型是`DefaultSqlSessionFactory` 所以调用

```java
public class DefaultSqlSessionFactory implements SqlSessionFactory {

  private SqlSession openSessionFromDataSource(ExecutorType execType, TransactionIsolationLevel level, boolean autoCommit) {
    Transaction tx = null;
    try {
      //获取环境信息
      final Environment environment = configuration.getEnvironment();
      //创建jdbc事务工厂，因为在解析mybatis-config.xml的时候创建的是JDBC的
      final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
      //初始化jdbctx
      tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
      //创建执行器，默认获取的是简单执行器，同时可以通过装饰者模式可以增强执行器，也可以通过拦截器对执行器进行处理
      final Executor executor = configuration.newExecutor(tx, execType);
      //创建默认的sqlsession
      return new DefaultSqlSession(configuration, executor, autoCommit);
    } catch (Exception e) {
      closeTransaction(tx); // may have fetched a connection so lets call close()
      throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
    } finally {
      ErrorContext.instance().reset();
    }
  }
}
```

1. 获取事务 `Transaction` 主要用于数据库连接，操作事务。
2. 创建执行器 `executor` 执行sql，解析sql的操作
3. 创建 `SqlSession` sql会话，可以有缓存等

**JDK动态代理**

通过 `sqlSession.getMapper(UserMapper.class);` 就能获取 `mapper` 还能执行其方法，这让人很不可思议。想要知道怎么回事，先学习下基于接口的jdk的动态代理。

```java

public interface InvocationHandler {

    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable;
}

public class MapperProxy<T> implements InvocationHandler {
    //代理方法的处理
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(Object.class.equals(method.getDeclaringClass())){
           return method.invoke(this, args);
        }
        System.out.println("代理成功");
        return null;
    }
    //创建代理对象
    public T getMapper(Class<?> clazz){
       return  (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, this);
    }
}
public class MybatisDebugSourceTest {
    public static void testMapper(){
        //创建代理类
        MapperProxy<UserMapper> userMapperMapperProxy = new MapperProxy<UserMapper>();
        //生成代理对象
        UserMapper mapper = userMapperMapperProxy.getMapper(UserMapper.class);
        mapper.query(1);
        System.out.println(mapper.toString());
    }
}

```
jdk代理通过字节码加载到jvm生成代理对象。达到接口就能调用的形式。`userMapperMapperProxy.getMapper(UserMapper.class);` 可以看成:
1. 创建 `UserMapper` 的子类并实现其方法，传入`MapperProxy`对象 
2. `UserMapper` 的每个方法都是调用 `MapperProxy` 的 `invoke` 方法。
3. 如果想调用原始类的方法可以 `method.invoke(${目标类的对象}, args)` 即可。

由于UserMapper是没有实现类的，这里就直接使用代理类来 `method.invoke`。是不是和 `mybatis` 获取mapper差不多。


**getMapper实现**

通过IDE一致跟踪，实际上调用的是 `MapperRegistery` 的 `getMapper` 方法。之前在获取`DefaultSqlSessionFactory` 时解析mapper映射器，添加的Mapper，下面就是对之前的一个添加进行获取。

```java
public class MapperRegistry {
  public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    //从map中获取对应的代理工厂，每个mapper.class都会有个代理工厂。因为在添加的时候已经创建好了
    final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
    if (mapperProxyFactory == null) {
      throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
    }
    try {
      //创建代理对象
      return mapperProxyFactory.newInstance(sqlSession);
    } catch (Exception e) {
      throw new BindingException("Error getting mapper instance. Cause: " + e, e);
    }
  }
}
```

上面返回对象的关键是 `mapperProxyFactory.newInstance(sqlSession)`。


**Mapper代理对象的获取**

这里就是上面说的基于jdk的动态代理。

```java
public class MapperProxyFactory<T> {
  //mapper接口的class对象
  private final Class<T> mapperInterface;
  //对方法的缓存
  private final Map<Method, MapperMethod> methodCache = new ConcurrentHashMap<>();
  //基于mapper接口生成代理对象
  protected T newInstance(MapperProxy<T> mapperProxy) {
    return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, mapperProxy);
  }
  //上面getMapper主要调用的方法
  public T newInstance(SqlSession sqlSession) {
    //创建一个代理处理者
    final MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface, methodCache);
    return newInstance(mapperProxy);
  }
}
//mapper代理处理者
public class MapperProxy<T> implements InvocationHandler, Serializable {
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      //如果调用的是object方法则直接使用当前处理者。
      if (Object.class.equals(method.getDeclaringClass())) {
        return method.invoke(this, args);
      //这个是调用默认方法，java8接口支持默认方法，则调用代理对象的默认方法，其实也是接口的默认方法。
      } else if (isDefaultMethod(method)) {
        return invokeDefaultMethod(proxy, method, args);
      }
    } catch (Throwable t) {
      throw ExceptionUtil.unwrapThrowable(t);
    }
    //缓存该方法解析的结果，下次再次执行时可以直接获取mapper方法
    //例如: userMapper.query  ..... userMapper.query 第二次就不需要再次解析，只是同一个mapper
    final MapperMethod mapperMethod = cachedMapperMethod(method);
    //执行其sql，所有的执行操作都在这里
    return mapperMethod.execute(sqlSession, args);
  }
}
```

上面在获取 `Mapper` 的时候巧妙的使用了动态代理, 使在使用 `mybatis` 的时候不用去实现只需要写接口即可。调用时使用代理对象来执行其对应的sql。



**准备执行sql的参数**

执行`userMapper.query` 实际上执行的是代理对象的 `MapperProxy.invoke`方法。最终执行sql在 `MapperMethod.execute`方法中。

```java
public class MapperMethod {
  //SqlCommand sql命令，其中包含了，sql是 SELECT 还是UPDATE 等 
  //还有就是方法全限定名 com.xugx.github.mybatis.debug.source.mapper.UserMapper.query 
  private final SqlCommand command;
  //方法的返回参数和入参的解析等。
  private final MethodSignature method;
  public Object execute(SqlSession sqlSession, Object[] args) {
    Object result;
    //UserMapper.query的command.Type是SELECT
    switch (command.getType()) {
      case SELECT:
        //有结果处理器
        if (method.returnsVoid() && method.hasResultHandler()) {
          executeWithResultHandler(sqlSession, args);
          result = null;
         //结果返回多个 
        } else if (method.returnsMany()) {
          result = executeForMany(sqlSession, args);
        //返回结果为map  
        } else if (method.returnsMap()) {
          result = executeForMap(sqlSession, args);
          //返回的是游标，一般时存储过程
        } else if (method.returnsCursor()) {
          result = executeForCursor(sqlSession, args);
        } else {
          //设置参数映射: param1 param2还有被@param标记的
          Object param = method.convertArgsToSqlCommandParam(args);
          //进行查询sql
          result = sqlSession.selectOne(command.getName(), param);
          if (method.returnsOptional() &&
              (result == null || !method.getReturnType().equals(result.getClass()))) {
            result = Optional.ofNullable(result);
          }
        }
        break;
      case FLUSH:
        result = sqlSession.flushStatements();
        break;
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

上面可以总结为：
1. 根据sql类型执行合适的case分支，这里时SELECT
2. 设置映射参数
3. `sqlession.selectone` 执行sql

**执行sql**

```java
public class DefaultSqlSession implements SqlSession {
  @Override
  public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
    try {
      //根据com.xugx.github.mybatis.debug.source.mapper.UserMapper.query ，获取Mapped的声明的信息
      MappedStatement ms = configuration.getMappedStatement(statement);
      // wrapCollection是对只有一个集合类的处理映射为list vlaue 
      return executor.query(ms, wrapCollection(parameter), rowBounds, Executor.NO_RESULT_HANDLER);
    } catch (Exception e) {
      throw ExceptionFactory.wrapException("Error querying database.  Cause: " + e, e);
    } finally {
      ErrorContext.instance().reset();
    }
  }
}
```

上面是获取到之前解析的 `MappedStatement` 和设置 `wrapCollection(parameter)` 。作为参数传给执行器的query方法




```java
public class CachingExecutor implements Executor {

  public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
    //替换掉对应的?
    BoundSql boundSql = ms.getBoundSql(parameterObject);
    //根据ms和参数和分页参数和sql的hash拼装为key
    CacheKey key = createCacheKey(ms, parameterObject, rowBounds, boundSql);
    //进行先查询缓存，查询不到在查询数据库
    return query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
  }
  public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql)
      throws SQLException {
    Cache cache = ms.getCache();
    if (cache != null) {
      flushCacheIfRequired(ms);
      if (ms.isUseCache() && resultHandler == null) {
        ensureNoOutParams(ms, boundSql);
        @SuppressWarnings("unchecked")
        List<E> list = (List<E>) tcm.getObject(cache, key);
        if (list == null) {
          list = delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
          tcm.putObject(cache, key, list); // issue #578 and #116
        }
        return list;
      }
    }
    return delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
  }

}

```












































































