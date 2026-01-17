package org.example.kaoyanplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.kaoyanplatform.entity.Book;

@Mapper
public interface BookMapper extends BaseMapper<Book> {
}
