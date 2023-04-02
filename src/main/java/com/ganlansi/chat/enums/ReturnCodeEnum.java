package com.ganlansi.chat.enums;

import com.ganlansi.chat.bean.ApiResultCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * @Author: kongyanfang
 * @Date: 2019/10/9 19:59
 */
@Slf4j
@Getter
public enum ReturnCodeEnum implements ApiResultCode {

    /**
     * 编码说明：
     * 1:网关(拦截器)
     * 2:USER登录及注册
     * 3:chatGPT接口调用
     */
    SUCCESS("0", "操作成功"),
    API_NOT_EXITS("1000", "接口不存在"),
    SYSTEM_EXCEPTION("1001", "操作异常"),
    SYSTEM_REQ_PARAM_ILLEGAL("1002", "请求参数不合法"),
    ILLEGAL_OPERATION("1003", "非法操作"),
    NO_AUTH_TO_PASS("1004", "未授权任何页面，拒绝访问；请联系管理员"),
    NO_OPEN_ID("1005", "缺失openid"),
    NO_APPID("1006", "缺失appid"),



    TOKEN_IS_VALID("2000", "token失效"),
    NO_LOGIN("2001", "账号登录超时或者未登录"),
    NON_SUPPORT_LOGIN("2002", "不支持此登录格式"),
    CHECK_SIGN_ERROR("2003", "签名验证不匹配"),

    LOGIN_RESPONSE_ERROR("2004", "返回数据不合法"),
    LOGIN_BUILD_USER_ERROR("2005", "构建用户信息失败"),
    LOGIN_NO_FIND_APP_ID("2006", "appid不能为空"),
    LOGIN_NO_MATCH_APP_ID("2007", "appid不匹配"),
    WECHAT_GAME_CODE_ERROR("2008", "code无效"),
    WECHAT_GAME_ENCRYPT_ERROR("2009", "加密错误"),
    WECHAT_GAME_DECRYPT_ERROR("2010", "解密错误"),
    WECHAT_GAME_DECRYPT_DATA_ERROR("2011", "解密数据不合法"),

    MISS_PARM("2100", "请求参数缺失!"),

    CHAT_GPT_API_REQUEST_FAIL("3001", "chatGPT接口调用失败"),


    ;

    private String code;
    private String message;

    ReturnCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ReturnCodeEnum getEnumByType(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        for (ReturnCodeEnum enums : ReturnCodeEnum.values()) {
            if (enums.getCode().equals(code)) {
                return enums;
            }
        }
        return null;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @param resultCode
     * @return
     */
    public boolean equals(String resultCode) {
        //log.info("----> [是否相等] {}=={}", resultCode, this.getCode());
        return this.getCode().equals(resultCode);
    }

}
