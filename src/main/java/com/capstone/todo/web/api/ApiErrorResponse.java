package com.capstone.todo.web.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/**
 * Standard API error envelope:
 *
 * <pre>
 * {
 *   "error": {
 *     "code": "...",
 *     "message": "...",
 *     "details": [ ... ],
 *     "correlationId": "..."
 *   }
 * }
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(ApiError error) {

    public record ApiError(String code, String message, List<?> details, String correlationId) {
        public ApiError {
            if (details == null) {
                details = new ArrayList<>();
            }
        }
    }

    public static ApiErrorResponse of(String code, String message, List<?> details, String correlationId) {
        return new ApiErrorResponse(new ApiError(code, message, details, correlationId));
    }

    public static ApiErrorResponse of(String code, String message, String correlationId) {
        return of(code, message, List.of(), correlationId);
    }
}
