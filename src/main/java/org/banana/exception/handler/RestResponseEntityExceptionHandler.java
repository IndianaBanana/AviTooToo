package org.banana.exception.handler;

import org.banana.exception.UserNotFoundException;
import org.banana.security.exception.UserPhoneAlreadyExistsException;
import org.banana.security.exception.UserUsernameAlreadyExistsException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({UserNotFoundException.class, UsernameNotFoundException.class})
    protected ResponseEntity<Object> handleNotFoundException(RuntimeException ex, WebRequest request) {
        return buildErrorResponse(ex, request, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({UserUsernameAlreadyExistsException.class, UserPhoneAlreadyExistsException.class})
    protected ResponseEntity<Object> handleConflictDataException(RuntimeException ex, WebRequest request) {
        return buildErrorResponse(ex, request, HttpStatus.CONFLICT);
    }

    private ResponseEntity<Object> buildErrorResponse(RuntimeException ex, WebRequest request, HttpStatus status) {
        Map<String, String> body = Map.of("error", ex.getMessage());
        return handleExceptionInternal(ex, body, new HttpHeaders(), status, request);
    }
}
