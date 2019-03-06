package com.xugx.github.mybatis.debug;

import com.xugx.github.mybatis.debug.entity.User;
import com.xugx.github.mybatis.debug.mapper.UserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MybatisDebugApplicationTests {

    @Autowired
    UserMapper userMapper;

    @Test
    public void contextLoads() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
         List<User> objects = new CopyOnWriteArrayList<>();

        Runnable runnable = ()-> {
            User query = userMapper.query(1);
            objects.add(query);
            System.out.println(objects);
        };
        for (int i =0 ; i< 2; i++){
            executorService.execute( runnable);
        }
        Thread.sleep(1000);
        System.out.println(objects);
    }
}
