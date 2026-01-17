package org.example.kaoyanplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.kaoyanplatform.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}