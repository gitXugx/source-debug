package com.xugx.github.mybatis.debug.source.mapper;

import com.xugx.github.mybatis.debug.source.entity.User;

/**
 * @author ：ex-xugaoxiang001
 * @description ：
 * @copyright ：	Copyright 2019 yowits Corporation. All rights reserved.
 * @create ：2019/3/7 15:12
 */

public interface UserMapper {
    User query(Integer id);
}
