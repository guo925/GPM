package com.gjx.gpms.common.result;

import lombok.Data;

/**
 * 统一返回结果
 */
@Data
public class Result<T> {

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 提示信息
     */
    private String msg;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 成功返回
     */
    public static <T> Result<T> success(T data) {

        Result<T> result = new Result<>();

        result.setCode(200);

        result.setMsg("success");

        result.setData(data);

        return result;
    }

    /**
     * 成功返回
     */
    public static <T> Result<T> success() {

        Result<T> result = new Result<>();

        result.setCode(200);

        result.setMsg("success");

        return result;
    }

    /**
     * 失败返回
     */
    public static <T> Result<T> error(String msg) {

        Result<T> result = new Result<>();

        result.setCode(500);

        result.setMsg(msg);

        return result;
    }
}