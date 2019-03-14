package com.xugx.github.spring.debug.ioc;

import org.junit.Before;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * @author ：ex-xugaoxiang001
 * @description ：
 * @copyright ：	Copyright 2019 yowits Corporation. All rights reserved.
 * @create ：2019/3/14 17:04
 */
public class BaseTest {
    public static ApplicationContext context;
    @Before
    public void init() throws IOException {
        context = new ClassPathXmlApplicationContext("spring-config.xml");
    }

}
