package com.gdsd.TutorService.exception;

import org.springframework.http.HttpStatus;

public class GenericTutorException extends RuntimeException {
    private String message;
    private HttpStatus httpStatus;

    public GenericTutorException() {
    }

    public GenericTutorException(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public GenericTutorException(String message, String message1, HttpStatus httpStatus) {
        super(message);
        this.message = message1;
        this.httpStatus = httpStatus;
    }

    public GenericTutorException(String message, Throwable cause, String message1, HttpStatus httpStatus) {
        super(message, cause);
        this.message = message1;
        this.httpStatus = httpStatus;
    }

    public GenericTutorException(Throwable cause, String message, HttpStatus httpStatus) {
        super(cause);
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public GenericTutorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String message1, HttpStatus httpStatus) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.message = message1;
        this.httpStatus = httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}
