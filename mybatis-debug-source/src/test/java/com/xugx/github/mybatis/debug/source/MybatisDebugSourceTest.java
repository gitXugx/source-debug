package com.xugx.github.mybatis.debug.source;

import com.xugx.github.mybatis.debug.source.entity.User;
import com.xugx.github.mybatis.debug.source.mapper.UserMapper;
import com.xugx.github.mybatis.debug.source.proxy.MapperProxy;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author ：ex-xugaoxiang001
 * @description ：
 * @copyright ：	Copyright 2019 yowits Corporation. All rights reserved.
 * @create ：2019/3/7 15:27
 */

public class MybatisDebugSourceTest {

    private static SqlSessionFactory sqlSessionFactory;

    @Before
    public void init() throws IOException {

        String resource = "mybatis-config.xml";
        //读取配置文件成二进制流
        InputStream inputStream = Resources.getResourceAsStream(resource);
        //解析创建SqlSessionFactory
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }

    /**
     * 一级缓存测试，当sqlsession提交后会清除一级缓存
     * @throws IOException
     */
    @Test
    public  void query() throws IOException {
        SqlSession sqlSession = sqlSessionFactory.openSession(true);
        UserMapper userMapper=sqlSession.getMapper(UserMapper.class);
        User user=userMapper.query(1);
        sqlSession.commit();
        User user2=userMapper.query(1);
    }

    /**
     * 多线程测试
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public  void TestLevelOneCache() throws IOException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper userMapper=sqlSession.getMapper(UserMapper.class);
        Runnable runnable = ()-> {
            User query = userMapper.query(1);
        };

        for (int i =0 ; i< 2; i++){
            executorService.execute( runnable);
        }
        Thread.sleep(3000);
    }

    /**
     * 测试代理类
     */
    @Test
    public  void testMapperProxy(){
        MapperProxy<UserMapper> userMapperMapperProxy = new MapperProxy<UserMapper>();
        UserMapper mapper = userMapperMapperProxy.getMapper(UserMapper.class);
        mapper.query(1);
        System.out.println(mapper.toString());
    }

    /**
     * 测试二级缓存，二级缓存只有sqlsession commit后才会进入到缓存，否则不会进入到缓存
     */
    @Test
    public  void testCache() throws IOException {

        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper userMapper=sqlSession.getMapper(UserMapper.class);
        User user=userMapper.query(1);
        sqlSession.close();
        SqlSession sqlSession2 = sqlSessionFactory.openSession();
        UserMapper mapper = sqlSession2.getMapper(UserMapper.class);
        User query = mapper.query(1);
    }

    @Test
    public  void insert() throws IOException {
        SqlSession sqlSession = sqlSessionFactory.openSession(true);
        UserMapper userMapper=sqlSession.getMapper(UserMapper.class);
        User user1 = new User();
        user1.setName("testName");
        userMapper.insert(user1);
    }



}
