package com.ridehailing.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private String code;
    private String message;
    private T data;
    private String correlationId;

    public static <T> ApiResponse<T> success(T data, String correlationId) {
        return ApiResponse.<T>builder()
                .code("SUCCESS")
                .message("Operation successful")
                .data(data)
                .correlationId(correlationId)
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message, String correlationId) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .correlationId(correlationId)
                .build();
    }
}
