package org.example.kaoyanplatform.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code; // 200 是成功，500 是失败
    private String msg;   // 提示信息
    private T data;       // 具体数据

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        return result;
    }

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("成功");
        result.setData(null);
        return result;
    }
}