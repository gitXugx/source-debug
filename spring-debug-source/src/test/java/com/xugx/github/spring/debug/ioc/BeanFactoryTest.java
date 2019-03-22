package com.xugx.github.spring.debug.ioc;

import org.junit.Test;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

/**
 * @author ：ex-xugaoxiang001
 * @description ：
 * @copyright ：	Copyright 2019 yowits Corporation. All rights reserved.
 * @create ：2019/3/15 11:38
 */
public class BeanFactoryTest {

    @Test
    public void testSimpleBean(){
        XmlBeanFactory xmlBeanFactory = new XmlBeanFactory(new ClassPathResource("spring-config.xml"));

        Object simpleBean = xmlBeanFactory.getBean("simpleBean2");

        System.out.println(simpleBean);
    }


    @Test
    public void testCyclicReference(){

       new  ClassPathXmlApplicationContext("spring-config.xml");


    }


}
