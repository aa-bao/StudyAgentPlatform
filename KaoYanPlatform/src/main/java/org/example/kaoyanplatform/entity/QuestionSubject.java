package org.example.kaoyanplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tb_question_subject")
public class QuestionSubject {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long questionId;
    private Integer subjectId;
}
