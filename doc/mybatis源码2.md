
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

1. 获取事务 `Transaction` 主要获取数据库连接，操作事务的动作
2. 创建执行器 `executor` 执行sql，解析sql的操作
3. 创建 `SqlSession` sql会话，可以有缓存等























































