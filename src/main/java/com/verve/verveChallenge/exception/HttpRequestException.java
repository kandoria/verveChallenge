package com.verve.verveChallenge.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class HttpRequestException extends Exception {

    private final HttpStatusCode statusCode;

    public HttpRequestException(String message, HttpStatusCode statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

}
