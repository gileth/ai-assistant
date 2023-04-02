package com.ganlansi.chat.bean;

import com.ganlansi.chat.enums.ReturnCodeEnum;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ApiResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String code;
    private String msg;
    private T data;

    public ApiResult() {
    }

    public ApiResult(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> ApiResult<T> success(T data) {
        return creatResult(ReturnCodeEnum.SUCCESS, data);
    }

    public static <T> ApiResult error(ApiResultCode resultCode){
        return creatResult(resultCode, null);
    }

    public static <T> ApiResult creatResult(ApiResultCode resultCode, T data){
        return new ApiResult(resultCode.getCode(), resultCode.getMessage(), data);
    }

    @Override
    public String toString() {
        return "ApiResult{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }


}
