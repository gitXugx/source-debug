package com.xugx.github.mybatis.debug.spring.mapper;

import com.xugx.github.mybatis.debug.spring.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author ：ex-xugaoxiang001
 * @description ：
 * @copyright ：	Copyright 2019 yowits Corporation. All rights reserved.
 * @create ：2019/3/7 14:21
 */
@Mapper
public interface UserMapper {


    int insert(User user);

    User query(Integer id);

}
