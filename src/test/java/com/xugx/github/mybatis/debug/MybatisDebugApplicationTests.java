package com.xugx.github.mybatis.debug;

import com.xugx.github.mybatis.debug.entity.User;
import com.xugx.github.mybatis.debug.mapper.UserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MybatisDebugApplicationTests {

    @Autowired
    UserMapper userMapper;

    @Test
    public void contextLoads() {
        User query = userMapper.query(1);
        System.out.println(query);
    }

}
