package com.xugx.github.mybatis.debug.source;

import com.xugx.github.mybatis.debug.source.entity.User;
import com.xugx.github.mybatis.debug.source.mapper.UserMapper;
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
public class MybatisDebugSourceTest {

    public  static void main(String[] args) throws IOException {
        query();
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


}
