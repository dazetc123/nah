package com.example.betong.Exception;

import org.springframework.http.HttpStatus;

/**
 * Exception nghiệp vụ dùng chung cho các module ngoài Auth (Quản lý người
 * dùng, Quản lý xe...). Tách khỏi AuthException để tên lớp không gây
 * hiểu nhầm là chỉ dùng cho đăng nhập/đăng ký.
 */
public class AppException extends RuntimeException {

    private final HttpStatus status;

    public AppException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
