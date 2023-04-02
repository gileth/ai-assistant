package com.ganlansi.chat.exception;


import com.ganlansi.chat.bean.ApiResultCode;
import com.ganlansi.chat.enums.ReturnCodeEnum;

import java.util.ArrayList;
import java.util.List;

public class BaseException extends RuntimeException {
    private static final long serialVersionUID = 779097140579256830L;
    private String code;
    private List<String> params = new ArrayList(0);
    private String message;

    public BaseException() {
    }

    public BaseException(String message) {
        this.code = ReturnCodeEnum.SYSTEM_EXCEPTION.getCode();
        if (message == null) {
            this.message = message;
        }
    }

    public BaseException(ApiResultCode status) {
        this.code = status.getCode();
        this.message = status.getMessage();
    }

    public BaseException(String code, List<String> params) {
        super(String.valueOf(code));
        this.code = code;
        this.params = params;
        if (this.message == null) {
            this.message = String.valueOf(code);
        }

    }

    public BaseException(String code, String[] params) {
        super(String.valueOf(code));
        this.code = code;
        String[] var3 = params;
        int var4 = params.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            String s = var3[var5];
            this.params.add(s);
        }

        if (this.message == null) {
            this.message = String.valueOf(code);
        }

    }

    public BaseException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BaseException(String code, String message, String linkUrl) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public List<String> getParams() {
        return this.params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}

