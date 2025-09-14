package lk.gov.mohe.adminsystem.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response<?> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<ErrorInfo> errors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String defaultMessage = error.getDefaultMessage();
            if (defaultMessage == null || defaultMessage.isEmpty()) {
                defaultMessage = "Invalid value";
            }
            ErrorInfo errorInfo = new ErrorInfo(error.getField(), defaultMessage);
            errors.add(errorInfo);
        });
        return new Response<>(errors);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Response<?>> handleResponseStatusException(ResponseStatusException ex) {
        List<ErrorInfo> errors = new ArrayList<>();
        String message = ex.getReason();
        if (message == null || message.isEmpty()) {
            message = "Something went wrong";
        }
        errors.add(new ErrorInfo(null, message));
        Response<?> response = new Response<>(errors);
        return new ResponseEntity<>(response, ex.getStatusCode());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Response<?> handleAllExceptions(Exception ex) {
        log.error("An unexpected error occurred", ex);
        List<ErrorInfo> errors = new ArrayList<>();
        errors.add(new ErrorInfo(null, "Something went wrong"));
        return new Response<>(errors);
    }
}
