package com.devpriyanshu.documentqa.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorDto(Instant timestamp, int status, String error, String message, Map<String, String> details) {

    public static ApiErrorDto of(int status, String error, String message) {
        return new ApiErrorDto(Instant.now(), status, error, message, Map.of());
    }

    public static ApiErrorDto withDetails(int status, String error, String message, Map<String, String> details) {
        return new ApiErrorDto(Instant.now(), status, error, message, details);
    }
}
