package com.nat.music.api.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        log.warn("404 Not Found on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("400 Bad Request on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("500 Unexpected error on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.internalServerError()
                .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request.getRequestURI()));
    }

    /*
     to handle @Valid issues
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        for (var error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        log.warn("400 Validation failed on {}: {}", getPath(request), errors);

        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, "Validation failed", getPath(request), errors));
    }

    /*
     to handle all others
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {

        String message = ex.getMessage();
        if (body instanceof ProblemDetail) {
            message = ((ProblemDetail) body).getDetail();
        }
        log.warn("{} on {}: {}", statusCode.value(), getPath(request), message);

        return ResponseEntity.status(statusCode)
                .headers(headers)
                .body(ErrorResponse.of(statusCode, message, getPath(request)));
    }

    private String getPath(WebRequest request) {
        // to retreive the path of the url.
        if (request instanceof ServletWebRequest) {
            ServletWebRequest servletRequest = (ServletWebRequest) request;
            return servletRequest.getRequest().getRequestURI();
        }
        return null;
    }
}
