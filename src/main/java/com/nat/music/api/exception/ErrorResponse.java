package com.nat.music.api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> errors
) {

    public static ErrorResponse of(HttpStatusCode status, String message, String path) {
        return of(status, message, path, null);
    }

    public static ErrorResponse of(HttpStatusCode status, String message, String path, Map<String, String> errors) {
        HttpStatus resolved = HttpStatus.resolve(status.value());
        String error = resolved != null ? resolved.getReasonPhrase() : "Error"; // reason phrase example "Not found"
        return new ErrorResponse(Instant.now(), status.value(), error, message, path, errors);
    }
}
