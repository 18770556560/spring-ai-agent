package com.cc.springaiagent.config;

import com.cc.springaiagent.exception.SecurityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SecurityViolationException.class)
    public ResponseEntity<String> handleSecurityViolation(SecurityViolationException ex) {
        // 返回 403 Forbidden 或 400 Bad Request
        return ResponseEntity.status(403).body(ex.getMessage());
    }
}
