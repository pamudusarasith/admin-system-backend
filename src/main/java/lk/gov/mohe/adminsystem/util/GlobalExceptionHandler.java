package lk.gov.mohe.adminsystem.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<ErrorInfo> errors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String defaultMessage = error.getDefaultMessage();
            if (defaultMessage == null || defaultMessage.isEmpty()) {
                defaultMessage = "Invalid value";
            }
            ErrorInfo errorInfo = new ErrorInfo(error.getField(), defaultMessage);
            errors.add(errorInfo);
        });
        return ApiResponse.error("Validation failed", errors);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ApiResponse<Void> handleMissingServletRequestPart(MissingServletRequestPartException ex) {
        String partName = ex.getRequestPartName();
        String message = "Missing required part: " + partName;
        List<ErrorInfo> errors = List.of(new ErrorInfo(partName, message));
        return ApiResponse.error(message, errors);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<?>> handleResponseStatusException(ResponseStatusException ex) {
        String message = ex.getReason();
        if (message == null || message.isEmpty()) {
            message = "Something went wrong";
        }
        ApiResponse<Void> apiResponse = ApiResponse.error(message, null);
        return new ResponseEntity<>(apiResponse, ex.getStatusCode());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ApiResponse<Void> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
        return ApiResponse.error("You do not have permission to perform this action",
            null);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleAllExceptions(Exception ex) {
        log.error("An unexpected error occurred", ex);
        return ApiResponse.error("Something went wrong", null);
    }
}
