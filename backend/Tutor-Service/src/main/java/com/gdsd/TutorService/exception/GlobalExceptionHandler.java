package com.gdsd.TutorService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map> resourceNotFoundException(ResourceNotFoundException ex) {
        String message = ex.getMessage();
        Map<String, String> apiResponse = new HashMap<>();
        apiResponse.put("message", message);

        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(GenericTutorException.class)
    public ResponseEntity<Map> genericTutorException(GenericTutorException ex) {
        String message = ex.getMessage();
        Map<String, String> apiResponse = new HashMap<>();
        apiResponse.put("message", message);

        return new ResponseEntity<>(apiResponse, ex.getHttpStatus());
    }

    @ExceptionHandler(GenericException.class)
    public ResponseEntity<Map> genericException(GenericException ex) {
        String message = ex.getMessage();
        Map<String, String> apiResponse = new HashMap<>();
        apiResponse.put("message", message);

        return new ResponseEntity<>(apiResponse, ex.getHttpStatus());
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<Map> sqlIntegrityConstraintViolation(SQLIntegrityConstraintViolationException ex) {
        String message = ex.getMessage();
        Map<String, String> apiResponse = new HashMap<>();
        apiResponse.put("message", message);

        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    // To handel banned users
    @ExceptionHandler(StudentBannedException.class)
    public ResponseEntity<ErrorResponse> handleStudentBannedException(StudentBannedException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(StudentLockedException.class)
    public ResponseEntity<ErrorResponse> handleStudentLockedException(StudentLockedException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
