package com.ssasinsa.wearagain.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException exception, HttpServletRequest request) {
        ErrorCode errorCode = exception.getErrorCode();
        ErrorResponse response = ErrorResponse.of(errorCode, exception.getMessage(), request.getRequestURI());
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        BindingResult bindingResult = exception.getBindingResult();
        String message = bindingResult.hasFieldErrors() ? resolveFieldError(bindingResult) : bindingResult.getObjectName();
        ErrorResponse response = ErrorResponse.of(CommonErrorCode.INVALID_REQUEST, message, request.getRequestURI());
        return ResponseEntity.status(CommonErrorCode.INVALID_REQUEST.getStatus()).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        ErrorResponse response = ErrorResponse.of(CommonErrorCode.INVALID_REQUEST, exception.getMessage(), request.getRequestURI());
        return ResponseEntity.status(CommonErrorCode.INVALID_REQUEST.getStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandledException(Exception exception, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(CommonErrorCode.INTERNAL_SERVER_ERROR, exception.getMessage(), request.getRequestURI());
        return ResponseEntity.status(CommonErrorCode.INTERNAL_SERVER_ERROR.getStatus()).body(response);
    }

    private String resolveFieldError(BindingResult bindingResult) {
        FieldError fieldError = bindingResult.getFieldError();
        if (fieldError == null) {
            return CommonErrorCode.INVALID_REQUEST.getMessage();
        }
        return fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : fieldError.getField();
    }
}
