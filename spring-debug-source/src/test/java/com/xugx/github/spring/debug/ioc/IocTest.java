package com.xugx.github.spring.debug.ioc;

import com.xugx.github.spring.debug.bean.SimpleBean;
import org.junit.Test;

/**
 * @author ：ex-xugaoxiang001
 * @description ：
 * @copyright ：	Copyright 2019 yowits Corporation. All rights reserved.
 * @create ：2019/3/14 16:58
 */

public class IocTest extends BaseTest {

    @Test
    public void getBean(){
        SimpleBean simpleBean = (SimpleBean)context.getBean("simpleBean");
        System.out.println(simpleBean);
    }

}
