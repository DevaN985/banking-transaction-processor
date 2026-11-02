package com.example.banking.controller;
import com.example.banking.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
@RestControllerAdvice
public class ApiExceptionHandler  {
    @ExceptionHandler(TransactionService.AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse notFound(RuntimeException ex)  {
        return new ErrorResponse(Instant.now(), 404, ex.getMessage());
    }
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse badRequest(RuntimeException ex)  {
        return new ErrorResponse(Instant.now(), 400, ex.getMessage());
    }
    public record ErrorResponse(Instant timestamp, int status, String message)  {
    }
}
