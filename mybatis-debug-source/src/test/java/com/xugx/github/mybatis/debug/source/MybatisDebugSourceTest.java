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

    @Test
    public  void query() throws IOException {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper userMapper=sqlSession.getMapper(UserMapper.class);
        User user=userMapper.query(1);
        User user2=userMapper.query(1);
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
     * 测试缓存
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

}
