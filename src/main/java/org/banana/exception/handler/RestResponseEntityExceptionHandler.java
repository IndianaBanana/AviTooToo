package org.banana.exception.handler;

import org.banana.exception.AdvertisementNotFoundException;
import org.banana.exception.AdvertisementTypeNotFoundException;
import org.banana.exception.AdvertisementUpdateException;
import org.banana.exception.CityNotFoundException;
import org.banana.exception.CommentNotFoundException;
import org.banana.exception.UserNotFoundException;
import org.banana.exception.UserRatesTheSameUserException;
import org.banana.security.exception.UserPhoneAlreadyExistsException;
import org.banana.security.exception.UserUpdateOldEqualsNewDataException;
import org.banana.security.exception.UserUsernameAlreadyExistsException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({UserNotFoundException.class, UsernameNotFoundException.class, AdvertisementNotFoundException.class,
            CommentNotFoundException.class, CityNotFoundException.class, AdvertisementTypeNotFoundException.class})
    protected ResponseEntity<Object> handleNotFoundException(RuntimeException ex, WebRequest request) {
        return buildErrorResponse(ex, request, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({
            UserUsernameAlreadyExistsException.class,
            UserPhoneAlreadyExistsException.class,
            UserUpdateOldEqualsNewDataException.class,
            UserRatesTheSameUserException.class,
            AdvertisementUpdateException.class,
    })
    protected ResponseEntity<Object> handleConflictDataException(RuntimeException ex, WebRequest request) {
        return buildErrorResponse(ex, request, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({
            BadCredentialsException.class
    })
    protected ResponseEntity<Object> handleBadCredentialsException(RuntimeException ex, WebRequest request) {
        return buildErrorResponse(ex, request, HttpStatus.UNAUTHORIZED);
    }


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Validation failed");
        problemDetail.setDetail("One or more fields are invalid");

        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> Optional.ofNullable(error.getDefaultMessage()).orElse("Invalid"),
                        (existing, replacement) -> existing
                ));

        problemDetail.setProperty("errors", fieldErrors);
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", ((ServletWebRequest) request).getRequest().getRequestURI());

        return handleExceptionInternal(ex, problemDetail, headers, HttpStatus.BAD_REQUEST, request);
    }

    private ResponseEntity<Object> buildErrorResponse(RuntimeException ex, WebRequest request, HttpStatus status) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problemDetail.setTitle("Application error");
        return handleExceptionInternal(ex, problemDetail, new HttpHeaders(), status, request);
    }
}
