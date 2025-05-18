package org.banana.exception.handler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.security.access.AccessDeniedException;
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

@Slf4j
@RestControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    public static final String VALIDATION_FAILED = "Validation failed";
    public static final String ONE_OR_MORE_PARAMETERS_ARE_INVALID = "One or more parameters are invalid";
    public static final String ONE_OR_MORE_FIELDS_ARE_INVALID = "One or more fields are invalid";
    public static final String INVALID = "Invalid";
    public static final String ERRORS = "errors";
    public static final String TIMESTAMP = "timestamp";
    public static final String PATH = "path";

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
            AccessDeniedException.class,
            UserDeleteCommentException.class,
    })
    protected ResponseEntity<Object> handleAccessException(RuntimeException ex, WebRequest request) {
        return buildErrorResponse(ex, request, HttpStatus.FORBIDDEN);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.error("Error: {}", ex.getMessage(), ex);
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle(VALIDATION_FAILED);
        problemDetail.setDetail(ONE_OR_MORE_FIELDS_ARE_INVALID);

        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> Optional.ofNullable(error.getDefaultMessage()).orElse(INVALID),
                        (existing, replacement) -> existing
                ));

        ex.getBindingResult()
                .getGlobalErrors()
                .forEach(error -> fieldErrors.put("global", Optional.ofNullable(error.getDefaultMessage())
                        .orElse(INVALID)));

        problemDetail.setProperty(ERRORS, fieldErrors);
        problemDetail.setProperty(TIMESTAMP, Instant.now());
        problemDetail.setProperty(PATH, ((ServletWebRequest) request).getRequest().getRequestURI());

        return handleExceptionInternal(ex, problemDetail, headers, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        log.error("Error: {}", ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle(VALIDATION_FAILED);
        problemDetail.setDetail(ONE_OR_MORE_PARAMETERS_ARE_INVALID);

        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        cv -> {
                            Path path = cv.getPropertyPath();
                            String last = null;
                            for (Path.Node node : path) {
                                last = node.getName();
                            }
                            return last;
                        },
                        ConstraintViolation::getMessage,
                        (m1, m2) -> m1
                ));

        problemDetail.setProperty(ERRORS, errors);
        problemDetail.setProperty(TIMESTAMP, Instant.now());
        problemDetail.setProperty(PATH, ((ServletWebRequest) request).getRequest().getRequestURI());

        return handleExceptionInternal(
                ex, problemDetail, new HttpHeaders(),
                HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    protected ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        return buildErrorResponse(ex, request, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<Object> handleAllUnhandledExceptions(Exception ex, WebRequest request) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle("Unexpected server error");
        problemDetail.setDetail("An unexpected error occurred. Please contact support.");
        problemDetail.setProperty(TIMESTAMP, Instant.now());
        problemDetail.setProperty(PATH, ((ServletWebRequest) request).getRequest().getRequestURI());

        return handleExceptionInternal(ex, problemDetail, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<Object> buildErrorResponse(RuntimeException ex, WebRequest request, HttpStatus status) {
        log.error("Error: {}", ex.getMessage(), ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problemDetail.setTitle("Application error");
        return handleExceptionInternal(ex, problemDetail, new HttpHeaders(), status, request);
    }
}
