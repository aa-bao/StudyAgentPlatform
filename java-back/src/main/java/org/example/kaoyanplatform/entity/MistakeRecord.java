package org.example.kaoyanplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tb_mistake_record")
public class MistakeRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer questionId;
    private Integer errorCount; // 错误次数
    private LocalDateTime createTime;
    private LocalDateTime updateTime; // 更新时间
}
