package com.project.realrank.common.exception;

import com.project.realrank.common.constant.ApiResponse;
import com.project.realrank.common.constant.HttpErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Optional;

@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("handleIllegalArgumentException = " , e);
        ApiResponse<?> apiResponse = ApiResponse.error(HttpErrorCode.BAD_REQUEST, e.getMessage());
        return ResponseEntity.status(HttpErrorCode.BAD_REQUEST.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentException(MethodArgumentNotValidException e) {
        log.error("handleMethodArgumentException = " , e);
        String message = Optional.ofNullable(e.getBindingResult().getFieldError())
                .map(FieldError::getDefaultMessage)
                .orElse("요청 값이 올바르지 않습니다.");
        ApiResponse<?> apiResponse = ApiResponse.error(HttpErrorCode.BAD_REQUEST, e.getMessage());
        return ResponseEntity.status(HttpErrorCode.BAD_REQUEST.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        log.error("Exception = " , e);
        ApiResponse<?> apiResponse = ApiResponse.error(HttpErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        return ResponseEntity.status(HttpErrorCode.INTERNAL_SERVER_ERROR.getStatusCode()).body(apiResponse);
    }
}
