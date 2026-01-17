package org.example.kaoyanplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_user_progress")
public class UserProgress {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer subjectId;
    private Integer finishedCount;
    private Integer correctCount;
    private LocalDateTime updateTime;
}
