package com.xugx.github.mybatis.debug.source.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author ：ex-xugaoxiang001
 * @description ：
 * @copyright ：	Copyright 2019 yowits Corporation. All rights reserved.
 * @create ：2019/3/8 10:40
 */
public class MapperProxy<T> implements InvocationHandler {


    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(Object.class.equals(method.getDeclaringClass())){
           return method.invoke(this, args);
        }
        System.out.println("代理成功");
        return null;
    }

    public T getMapper(Class<?> clazz){
       return  (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, this);
    }

}
