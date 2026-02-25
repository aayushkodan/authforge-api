package com.aayush.authforge.authfordgeapi.common.exceptions;

public class InvalidTokenException extends RuntimeException{
    public InvalidTokenException(String invalidOrExpiredToken) {
        super(invalidOrExpiredToken);
    }
}
