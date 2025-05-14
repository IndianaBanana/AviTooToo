package org.banana.exception.handler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.banana.exception.AddingCommentWhenParentCommenterIsNullException;
import org.banana.exception.AdvertisementNotFoundException;
import org.banana.exception.AdvertisementTypeAlreadyExistsException;
import org.banana.exception.AdvertisementTypeNotFoundException;
import org.banana.exception.AdvertisementUpdateException;
import org.banana.exception.CityAlreadyExistsException;
import org.banana.exception.CityNotFoundException;
import org.banana.exception.CommentNotFoundException;
import org.banana.exception.ConversationNotFoundException;
import org.banana.exception.MessageSendException;
import org.banana.exception.SaleHistoryAccessDeniedException;
import org.banana.exception.SaleHistoryAdvertisementQuantityIsLowerThanExpectedException;
import org.banana.exception.SaleHistoryNotFoundException;
import org.banana.exception.UserDeleteCommentException;
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

    @ExceptionHandler({
            UserNotFoundException.class,
            UsernameNotFoundException.class,
            AdvertisementNotFoundException.class,
            CommentNotFoundException.class,
            CityNotFoundException.class,
            AdvertisementTypeNotFoundException.class,
            SaleHistoryNotFoundException.class,
            ConversationNotFoundException.class
    })
    protected ResponseEntity<Object> handleNotFoundException(RuntimeException ex, WebRequest request) {
        return buildErrorResponse(ex, request, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({
            UserUsernameAlreadyExistsException.class,
            UserPhoneAlreadyExistsException.class,
            UserUpdateOldEqualsNewDataException.class,
            UserRatesTheSameUserException.class,
            AdvertisementUpdateException.class,
            MessageSendException.class,
            AddingCommentWhenParentCommenterIsNullException.class,
            SaleHistoryAdvertisementQuantityIsLowerThanExpectedException.class,
            CityAlreadyExistsException.class,
            AdvertisementTypeAlreadyExistsException.class,
    })
    protected ResponseEntity<Object> handleConflictDataException(RuntimeException ex, WebRequest request) {
        return buildErrorResponse(ex, request, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({
            SaleHistoryAccessDeniedException.class,
            UserDeleteCommentException.class,
    })
    protected ResponseEntity<Object> handleAccessException(RuntimeException ex, WebRequest request) {
        return buildErrorResponse(ex, request, HttpStatus.FORBIDDEN);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Validation failed");
        problemDetail.setDetail("One or more fields are invalid");

        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> Optional.ofNullable(error.getDefaultMessage()).orElse("Invalid"),
                        (existing, replacement) -> existing
                ));

        ex.getBindingResult()
                .getGlobalErrors()
                .forEach(error -> fieldErrors.put("global", Optional.ofNullable(error.getDefaultMessage())
                        .orElse("Invalid")));

        problemDetail.setProperty("errors", fieldErrors);
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", ((ServletWebRequest) request).getRequest().getRequestURI());

        return handleExceptionInternal(ex, problemDetail, headers, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Validation failed");
        problemDetail.setDetail("One or more parameters are invalid");

        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        cv -> {
                            // Вычленяем имя параметра из пути, например "filter.page"
                            String[] path = cv.getPropertyPath().toString().split("\\.");
                            return path[path.length - 1];
                        },
                        ConstraintViolation::getMessage,
                        (m1, m2) -> m1
                ));

        problemDetail.setProperty("errors", errors);
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path",
                ((ServletWebRequest) request).getRequest().getRequestURI());

        return handleExceptionInternal(
                ex, problemDetail, new HttpHeaders(),
                HttpStatus.BAD_REQUEST, request);
    }

    private ResponseEntity<Object> buildErrorResponse(RuntimeException ex, WebRequest request, HttpStatus status) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problemDetail.setTitle("Application error");
        return handleExceptionInternal(ex, problemDetail, new HttpHeaders(), status, request);
    }
}
