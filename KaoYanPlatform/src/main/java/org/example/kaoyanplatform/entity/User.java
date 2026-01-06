package org.example.kaoyanplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tb_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;            // 账号/用户名
    private String password;            // 密码
    private String phone;               // 手机号
    private String email;               // 邮箱
    private String nickname;            // 昵称
    private String avatar;              // 头像url
    private String role;                // 身份
    private String targetSchool;        // 目标院校
    private Integer targetTotalScore;   // 目标总分
    private String examYear;            // 考研年份
    private String examSubjects;        // 公共课

    @TableField(fill = FieldFill.INSERT)
    @JsonProperty("createTime")
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonProperty("updateTime")
    private LocalDateTime updateTime;
}