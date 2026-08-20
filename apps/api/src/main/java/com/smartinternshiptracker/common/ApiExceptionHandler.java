package com.smartinternshiptracker.common;

import com.smartinternshiptracker.application.ApplicationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApplicationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleApplicationNotFound(ApplicationNotFoundException exception) {
        return new ErrorResponse(exception.getMessage());
    }
}