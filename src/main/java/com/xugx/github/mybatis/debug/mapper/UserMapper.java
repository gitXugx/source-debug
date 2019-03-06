package com.xugx.github.mybatis.debug.mapper;

import com.xugx.github.mybatis.debug.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author ：apple
 * @description ：
 * @copyright ：	Copyright 2019 yowits Corporation. All rights reserved.
 * @create ：2019/3/6 下午10:14
 */
@Mapper
public interface UserMapper {

    int insert(User user);

    User query(Integer id);
}
