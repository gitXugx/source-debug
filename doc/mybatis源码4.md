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








































