package com.tinaco.monitoragua.exception;

public class RefreshTokenNoEncontradoException extends RuntimeException {
    public RefreshTokenNoEncontradoException(String message) {
        super(message);
    }
}
