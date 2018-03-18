package com.ironman.forum.util;

import org.springframework.http.HttpStatus;

public enum ResponseStatus {
    SUCCESS(1000, HttpStatus.OK, "成功"),
    PARAM_ERROR(2001, HttpStatus.BAD_REQUEST, "参数错误"),
    MOMENT_NOT_EXIST(3001, HttpStatus.BAD_REQUEST, "动态不存在"),
    BLOG_NOT_EXIST(4001, HttpStatus.BAD_REQUEST, "动态不存在"),
    DUPLICATE_LIKE_LOG(7001, HttpStatus.BAD_REQUEST, "不能重复赞或者踩"),
    LIKE_LOG_NOT_EXIST(7002, HttpStatus.BAD_REQUEST, "赞或踩记录不存在"),
    ARTICLE_TYPE_ILLEGAL(7003, HttpStatus.INTERNAL_SERVER_ERROR, "ArticleType类型不合法"),
    SYSTEM_ERROR(9999, HttpStatus.INTERNAL_SERVER_ERROR, "系统错误");
    private int code;
    private HttpStatus httpStatus;
    private String msg;

    ResponseStatus(int code, HttpStatus httpStatus, String msg) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMsg() {
        return msg;
    }
}
