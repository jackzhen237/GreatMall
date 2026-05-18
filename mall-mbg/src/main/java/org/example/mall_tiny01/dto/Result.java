package org.example.mall_tiny01.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class Result<T> implements Serializable {

    private Integer code; //编码：200成功，0和其它数字为失败
    private String message; //错误信息（注意：这里改成了 message）
    private T data; //数据

    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.code = 200;
        return result;
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.code = 200;
        result.message = "操作成功！"; // 这里也改成 message
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result result = new Result();
        result.message = msg; // 这里也改成 message
        result.code = 0;
        return result;
    }
}