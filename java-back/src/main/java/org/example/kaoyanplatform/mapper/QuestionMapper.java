package org.example.kaoyanplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.kaoyanplatform.entity.Question;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}