package com.franz.reggie.common;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class Result<T> implements Serializable {
    private Integer code;

    private String msg;

    private T data;

    private Map map = new HashMap();

    public static <T>Result<T> success(T object){
        Result<T> result = new Result<T>();
        result.code = Code.SUCCESS;
//        result.msg = "success";
        result.data = object;
        return result;
    }

    public static <T>Result<T> error(String msg){
        Result<T> result = new Result<T>();
        result.code = Code.ERROR;
        result.msg = msg;
//        result.data = object;
        return result;
    }

    public Result<T> add(String key, Object value){
        this.map.put(key, value);
        return this;
    }
}
