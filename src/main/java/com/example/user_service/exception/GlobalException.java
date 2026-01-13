package com.example.user_service.exception;

import com.example.user_service.dto.ErrorReponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalException {
    @ExceptionHandler(ResourceNotFound.class)
    public ResponseEntity<ErrorReponse> handleResourceNotFound(ResourceNotFound ex){
        log.error("Resource exception: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(
                new ErrorReponse(
                        400,
                        ex.getMessage(),
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorReponse> handleIllegalArgument(IllegalArgumentException ex){
        log.error("Illegal argument exception: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(
                new ErrorReponse(
                        401,
                        ex.getMessage(),
                        LocalDateTime.now()
                )
        );
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorReponse> handleValidationException(MethodArgumentNotValidException ex){
        log.error("Validation exception: {}", ex);
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error-> error.getField()+" "+error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(
                new ErrorReponse(
                        400,
                        message,
                        LocalDateTime.now()
                )
        );
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorReponse> handleGeneralException(Exception ex){
        log.error("General exception: {}", ex.getMessage());
        return ResponseEntity.status(500).body(
                new ErrorReponse(
                        500,
                        "An unexpected error occurred.",
                        LocalDateTime.now()
                )
        );
    }
}
