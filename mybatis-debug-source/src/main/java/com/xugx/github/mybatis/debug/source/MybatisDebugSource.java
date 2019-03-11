package com.xugx.github.mybatis.debug.source;

import com.xugx.github.mybatis.debug.source.entity.User;
import com.xugx.github.mybatis.debug.source.mapper.UserMapper;
import com.xugx.github.mybatis.debug.source.proxy.MapperProxy;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author ：ex-xugaoxiang001
 * @description ：
 * @copyright ：	Copyright 2019 yowits Corporation. All rights reserved.
 * @create ：2019/3/7 15:27
 */
public class MybatisDebugSource {

    public  static void main(String[] args) throws IOException {
//        query();
//        testMapperProxy();
        testCache();
    }

    public static void query() throws IOException {
        String resource = "mybatis-config.xml";
        //读取配置文件成二进制流
        InputStream inputStream = Resources.getResourceAsStream(resource);
        //解析创建SqlSessionFactory
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper userMapper=sqlSession.getMapper(UserMapper.class);
        User user=userMapper.query(1);

        User user2=userMapper.query(1);
//        sqlSession.commit();
    }

    /**
     * 测试代理类
     */
    public static void testMapperProxy(){
        MapperProxy<UserMapper> userMapperMapperProxy = new MapperProxy<UserMapper>();
        UserMapper mapper = userMapperMapperProxy.getMapper(UserMapper.class);
        mapper.query(1);
        System.out.println(mapper.toString());
    }

    /**
     * 测试缓存
     */
    public static void testCache() throws IOException {
        String resource = "mybatis-config.xml";
        //读取配置文件成二进制流
        InputStream resourceAsStream = Resources.getResourceAsStream(resource);
        InputStream inputStream = resourceAsStream;
        //解析创建SqlSessionFactory
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper userMapper=sqlSession.getMapper(UserMapper.class);
        User user=userMapper.query(1);
//        UserMapper mapper1 = sqlSession.getMapper(UserMapper.class);
//        mapper1.query(1);

        SqlSession sqlSession2 = sqlSessionFactory.openSession();
        UserMapper mapper = sqlSession2.getMapper(UserMapper.class);
        User query = mapper.query(1);
//        User user2=userMapper.query(1);
//        sqlSession.commit();
    }

}
