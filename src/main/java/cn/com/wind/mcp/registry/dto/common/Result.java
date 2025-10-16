package cn.com.wind.mcp.registry.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用API响应类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private boolean success;
    private String message;
    private T data;
    private String error;

    /**
     * 成功响应
     *
     * @param data 响应数据
     * @return Result对象
     */
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
            .success(true)
            .data(data)
            .build();
    }

    /**
     * 成功响应（带消息）
     *
     * @param message 消息
     * @param data    响应数据
     * @return Result对象
     */
    public static <T> Result<T> success(String message, T data) {
        return Result.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .build();
    }

    /**
     * 失败响应
     *
     * @param error 错误信息
     * @return Result对象
     */
    public static <T> Result<T> fail(String error) {
        return Result.<T>builder()
            .success(false)
            .error(error)
            .build();
    }

    /**
     * 失败响应（带消息）
     *
     * @param message 消息
     * @param error   错误信息
     * @return Result对象
     */
    public static <T> Result<T> fail(String message, String error) {
        return Result.<T>builder()
            .success(false)
            .message(message)
            .error(error)
            .build();
    }
}