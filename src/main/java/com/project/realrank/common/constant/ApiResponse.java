package com.project.realrank.common.constant;

import org.springframework.http.HttpStatus;

public record ApiResponse<T>(
        T data,
        int statusCode,
        String resultCode,
        String resultMessage,
        String detailMessage

) {

    private static final String SUCCESS_MESSAGE = "정상 처리 되었습니다";
    private static final String ERROR_MESSAGE = "시스템 에러가 발생했습니다";

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(data, HttpStatus.OK.value(), String.valueOf(HttpStatus.OK.value()), SUCCESS_MESSAGE, SUCCESS_MESSAGE);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(data, HttpStatus.CREATED.value(), String.valueOf(HttpStatus.CREATED.value()), SUCCESS_MESSAGE, SUCCESS_MESSAGE);
    }

    public static <T> ApiResponse<T> error(HttpErrorCode errorCode, String message) {
        return new ApiResponse<>(null, errorCode.getStatusCode(), String.valueOf(errorCode.getReason()), message, message);
    }

}