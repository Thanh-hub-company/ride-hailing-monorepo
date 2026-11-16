package com.example.tripservice.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.persistence.OptimisticLockException;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class BusinessExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handle(BusinessException exception) {
        String correlationId = UUID.randomUUID().toString();
        return ResponseEntity.status(exception.getStatus())
                .body(new ErrorResponse(exception.getCode(), exception.getMessage(), correlationId,
                        exception.getDetails(), Instant.now()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleStatus(ResponseStatusException exception) {
        String correlationId = UUID.randomUUID().toString();
        String message = exception.getReason() == null ? "Yêu cầu không hợp lệ." : exception.getReason();
        String code = exception.getStatusCode().value() == 409 ? "INVALID_TRIP_STATE" : "REQUEST_FAILED";
        return ResponseEntity.status(exception.getStatusCode())
                .body(new ErrorResponse(code, message, correlationId, Map.of(), Instant.now()));
    }

    @ExceptionHandler({OptimisticLockException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ErrorResponse> handleConcurrency(Exception exception) {
        return ResponseEntity.status(409)
                .body(new ErrorResponse("INVALID_TRIP_STATE",
                        "Chuyến đi vừa được xử lý bởi một request khác.",
                        UUID.randomUUID().toString(), Map.of(), Instant.now()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleMalformedRequest(Exception exception) {
        String message = exception instanceof HttpMessageNotReadableException
                && exception.getMessage() != null
                && exception.getMessage().contains("UUID")
                ? "quoteId phải là UUID hợp lệ. Hãy chạy Create quote trước và dùng đúng biến quoteId."
                : "Request body hoặc tham số có định dạng không hợp lệ.";
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("VALIDATION_ERROR", message,
                        UUID.randomUUID().toString(), Map.of(), Instant.now()));
    }
}
