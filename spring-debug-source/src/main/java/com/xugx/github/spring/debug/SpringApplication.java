package com.xugx.github.spring.debug;

import com.xugx.github.spring.debug.bean.SimpleBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author ：ex-xugaoxiang001
 * @description ：
 * @copyright ：	Copyright 2019 yowits Corporation. All rights reserved.
 * @create ：2019/3/14 16:51
 */
public class SpringApplication {

    public  static void main(String[] args){
        ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext("spring-config.xml");

        SimpleBean simpleBean = (SimpleBean) classPathXmlApplicationContext.getBean("simpleBean");

        System.out.println(simpleBean);

    }

}
