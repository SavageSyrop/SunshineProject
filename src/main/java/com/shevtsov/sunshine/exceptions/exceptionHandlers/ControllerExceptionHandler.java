package com.shevtsov.sunshine.exceptions.exceptionHandlers;

import com.shevtsov.sunshine.exceptions.ActionAlreadyCompletedException;
import com.shevtsov.sunshine.exceptions.AlreadyExistsException;
import com.shevtsov.sunshine.exceptions.AuthenticationCredentialsReadingException;
import com.shevtsov.sunshine.exceptions.AuthorizationErrorException;
import com.shevtsov.sunshine.exceptions.BannedException;
import com.shevtsov.sunshine.exceptions.InvalidActionException;
import com.shevtsov.sunshine.exceptions.LikeRevokedException;
import com.shevtsov.sunshine.exceptions.SecurityConfigurationException;
import com.shevtsov.sunshine.exceptions.SelfInteractionException;
import com.shevtsov.sunshine.exceptions.WeakDataException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.naming.AuthenticationException;
import javax.persistence.EntityNotFoundException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFoundException(Exception exception, WebRequest request) {
        return constructResponseEntity(exception, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({AlreadyExistsException.class})
    public ResponseEntity<Object> alreadyUser(Exception exception, WebRequest request) {
        return constructResponseEntity(exception, HttpStatus.IM_USED);
    }

    @ExceptionHandler(InvalidActionException.class)
    public ResponseEntity<Object> invalidAction(Exception exception, WebRequest request) {
        return constructResponseEntity(exception, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({SelfInteractionException.class, WeakDataException.class})
    public ResponseEntity<Object> badDataExceptions(Exception exception, WebRequest request) {
        return constructResponseEntity(exception, HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler({LikeRevokedException.class, ActionAlreadyCompletedException.class})
    public ResponseEntity<Object> likeRevoke(Exception exception, WebRequest request) {
        return constructResponseEntity(exception, HttpStatus.OK);
    }

    @ExceptionHandler({AuthenticationCredentialsReadingException.class, BannedException.class, AuthorizationErrorException.class, AuthenticationException.class, AccessDeniedException.class})
    public ResponseEntity<Object> failAuth(Exception exception, WebRequest request) {
        return constructResponseEntity(exception, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(SecurityConfigurationException.class)
    public ResponseEntity<Object> securityFail(Exception exception, WebRequest request) {
        return constructResponseEntity(exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Object> nullPointer(Exception exception, WebRequest request) {
        return constructResponseEntity(exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Object> constructResponseEntity(Exception exception, HttpStatus httpStatus) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("message", exception.getMessage());
        log.error(exception.getMessage(), exception);
        return new ResponseEntity<>(body, httpStatus);
    }
}
