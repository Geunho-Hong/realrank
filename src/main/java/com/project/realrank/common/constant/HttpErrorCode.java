package com.project.realrank.common.constant;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum HttpErrorCode {

    UNAUTHORIZED_ERROR(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase(), "Authentication Exception Error"),
    BUSINESS_ERROR(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), "Business Exception Error"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), "잘못된 요청입니다"),
    NOT_FOUND_ERROR(HttpStatus.NOT_FOUND.value(),HttpStatus.NOT_FOUND.getReasonPhrase(), "데이터를 찾을 수 없습니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN.value(),HttpStatus.FORBIDDEN.getReasonPhrase(), "유효하지 않은 접근입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(),HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "시스템 에러가 발생했습니다");

    private int statusCode;
    private String reason;
    private String description;

    HttpErrorCode(int statusCode, String reason, String description) {
        this.statusCode = statusCode;
        this.reason = reason;
        this.description = description;
    }

}
