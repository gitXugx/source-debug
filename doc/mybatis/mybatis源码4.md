# mybatis核心问题
> `mybatis` 主要设计由 `configuration` 、`SqlSession` 、`Executor` 、`Handler` (`PreparedStatementHandler` 、 `DefaultParameterHandler`、`DefaultResultSetHandler`) 组成


## 作用

**Configuration**

该对象主要存放 `mybatis-config.xml` 配置信息和 `mapepr.xml` 存放在 `MapperRegister` 中 , 负责提供创建 `Handler`。

**SqlSession**

由 `defaultSqlSessionFactory` 来实例化化 `Transaction` 和 `Executor` 来创建 `defaultSqlsession` 。

`SqlSession` 主要提供执行Sql、获取mapper、和事务的接口，隐含着对缓存的操作。



**Executor**

1. `Executor` 接口约定了了执行sql，事务获取管理，缓存和懒加载等操作
2. `BaseExecutor` 实现了 `Executor` 实现了通用逻辑和模板方法。
3. `CachingExecutor` 通过委托模式具体实现委托给 `BaseExecutor` 的实现类，该类主要是二级缓存的实现。中途会有一个拦截器，可以使用外置缓存
4. `SimpleExecutor` 当前sqlsession每次执行都创建一个新的 `Statement`，创建对应的 `StatementHandler` 并执行插件 ， 获取连接代理类，获取 `prepardStatement` 代理类。
5. `ReuseExecutor` 执行的时候创建一个`Statement` 重复使用
6. `BatchExector` 略

**Handler**

**`StatementHandler`** 
1. `StatementHandler` 对 `Statement` 的基本处理操作包括获取 `Statement` 参数处理，执行sql，获取解析后的sql，获取参数处理器的约定
2. `BaseStatementHandler` 实现通用`statement`操作的逻辑，创建 `DefaultParameterHandler` 和 `DefaultResultSetHandler` 并执行插件
3. `PrepardStatementHandler` 类型是预执行的操作， 安全性好，有效防止Sql注入等问题; 
4. `SimpleStatementHandler` 类型是 `Statement` 一般用于无参数查询，它是由sql注入的风险
5. `callableStatementHandler` 支持调用存储过程,提供了对输出和输入/输出参数(INOUT)的支持; 
6. `RoutingStatementHandler` 实现 `StatementHandler` 创建时初始化上面3中类型的操作，委托给上面的实现



## 流程

下面是看源码后画的图:

<div align="center"> <img src="https://github.com/gitXugx/doc-images/blob/master/images/mybatisSource/mybatis%E6%BA%90%E7%A0%81%E5%9B%BE.jpg" /> </div><br>


1. `mybatis-config.xml` 和 `Mapper.xml` 文件主要依靠两个 `XmlConfigurationBuilder` 和 `XmlMapperBuilder` 然后生成 `Configuration`
`Configuration` 包括 :
**evn** ： 环境信息，主要包括 `TranscationFactory`和`DataSourceFactry` 
**plugin**: 提供Mybatis的插件功能，主要使用代理模式实现
**mapperRegister**: 创建 `mapperProxyFactory` 和 mapper.class的映射
**properties**: 导入的properties文件变量
**typeHandlers**: 类型处理器
等多个配置

2. 通过 `Configuration` 创建 `DefaultSqlSessionFactory` 
3. 使用 `DefaultSqlSessionFactory` 生成 `SqlSessionFactoy` 主要由 `Configuration` 里的 `TranscationFactoy`和 `DataSourceFactory` 创建 `Transaction` 和 `DataSource` 然后通过 `Transcation` 和 `ExecutorType` 创建对应的执行器, 然后创建 `SqlSession` 当执行的时候实际是委托给 `Executor` 
4. 获取 `Mapper` 方法，实际上获取的是 `Mapper`的代理对象，主要由 `MapperRegister` 来提供。
5. 当调用 `Mapper` 代理对象的方法时，实际上最后执行命令的是 `Executor` ， `Executor` 提供一级缓存和二级缓存。二级缓存支持插件功能
6. 通过 `Configuration` 创建 `PreparedStatementHandler` 然后获取 `Connection`代理对象，初始化 `statement` 和获取其代理对象，可使用插件
7. 使用 `ParameterHandler` 来设置sql占位符，可使用插件
8. 查询结束后使用 `ResultSetHandler` 对结果集代理进行处理，可使用插件



## 设计模式

1. 装饰者模式 : CachingExecutor
2. 动态代理模式: MapperProxy、connection、PreparedStatement、ResultSet、Plugin...
3. 委托模式: RoutingStatementHandler
4. 模板方法: BaseExecutor
5. 责任链模式: Plugin
6. 建造者模式: CacheBuilder、MappedStatement ...
7. 工厂模式: DefaultSqlSessionFactory、MapperProxyFactory
























































