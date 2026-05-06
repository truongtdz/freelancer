package com.freelancer.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Convenience: throw with plain HttpStatus + custom message.
     * Maps BAD_REQUEST → VALIDATION_ERROR, FORBIDDEN → FORBIDDEN, 409 → DUPLICATE_APPLICATION, etc.
     */
    public AppException(HttpStatus httpStatus, String message) {
        super(message);
        this.errorCode = mapHttpStatus(httpStatus);
    }

    private static ErrorCode mapHttpStatus(HttpStatus status) {
        return switch (status) {
            case FORBIDDEN       -> ErrorCode.FORBIDDEN;
            case UNAUTHORIZED    -> ErrorCode.UNAUTHORIZED;
            case NOT_FOUND       -> ErrorCode.JOB_NOT_FOUND;
            case CONFLICT        -> ErrorCode.DUPLICATE_APPLICATION;
            default              -> ErrorCode.VALIDATION_ERROR;
        };
    }
}
