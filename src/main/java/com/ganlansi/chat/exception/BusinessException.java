package com.ganlansi.chat.exception;

import com.ganlansi.chat.bean.ApiResultCode;

public class BusinessException  extends BaseException {
    private static final long serialVersionUID = -4987001305340759909L;

    private BusinessException() {
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String code, String... params) {
        super(code, params);
    }

    public BusinessException(String code, String message) {
        super(code, message);
    }

    public BusinessException(ApiResultCode status) {
        super(status);
    }

    public BusinessException(String code, String message, String linkUrl) {
        super(code, message, linkUrl);
    }

    public static void throwMessage(String code, String msg) {
        throw new BusinessException(code, msg);
    }

    public static void throwMessage(ApiResultCode status) {
        throw new BusinessException(status);
    }

    public static void throwMessage(String msg) {
        BusinessException be = new BusinessException();
        be.setMessage(msg);
        throw be;
    }
}
