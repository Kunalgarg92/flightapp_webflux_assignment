package com.flightapp.webflux.exception;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.http.server.reactive.ServerHttpRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalErrorHandler {
	
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationErrors(MethodArgumentNotValidException ex,ServerHttpRequest request
) {
        Map<String, String> fieldErrors = new HashMap<>();
        List<ObjectError> errorList = ex.getBindingResult().getAllErrors();
        errorList.forEach(error -> {
            if (error instanceof FieldError) {
                String fieldName = ((FieldError) error).getField();
                String msg = error.getDefaultMessage();
                fieldErrors.put(fieldName, msg);
            }
        });
        String joined = fieldErrors.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("; "));

        ErrrorResponse body = new ErrrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                joined,
                request.getURI().getPath()

        );
        Map<String, Object> response = new HashMap<>();
        response.put("errorResponse", body);
        response.put("errors", fieldErrors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrrorResponse> handleConstraintViolations(ConstraintViolationException ex,
    		ServerHttpRequest request) {
        String msg = ex.getConstraintViolations().stream()
                .map(cv -> cv.getMessage())
                .collect(Collectors.joining("; "));
        ErrrorResponse body = new ErrrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                msg,
                request.getURI().getPath()

        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrrorResponse> handleNotReadable(HttpMessageNotReadableException ex,ServerHttpRequest request) {
    	String msg = "Malformed JSON or invalid field type";
        if (ex.getMostSpecificCause() != null) {
            msg = ex.getMostSpecificCause().getMessage();
        }
        ErrrorResponse body = new ErrrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                msg,
                request.getURI().getPath()
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrrorResponse> handleIllegalArg(IllegalArgumentException ex,ServerHttpRequest request) {
        ErrrorResponse body = new ErrrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getURI().getPath()
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrrorResponse> handleNPE(NullPointerException ex, ServerHttpRequest request
) {
        ex.printStackTrace(); 
        ErrrorResponse body = new ErrrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "A required value was missing or an unexpected null occurred",
                request.getURI().getPath()

        );
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrrorResponse> handleAll(Exception ex, ServerHttpRequest request) {
        ex.printStackTrace(); 
        ErrrorResponse body = new ErrrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage() != null ? ex.getMessage() : "Unexpected error",
                		 request.getURI().getPath()
        );
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrrorResponse> handleNotFound(ResourceNotFoundException ex, ServerHttpRequest request
) {
        ErrrorResponse body = new ErrrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getURI().getPath()

        );
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }
   
}
