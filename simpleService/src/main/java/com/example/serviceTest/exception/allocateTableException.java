package com.example.serviceTest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason ="allocate error")
public class allocateTableException extends RuntimeException {
    public allocateTableException() {
        
    }
}

