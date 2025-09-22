package com.tinaco.monitoragua.exception;

public class RefreshTokenExpiradoException extends RuntimeException {
    public RefreshTokenExpiradoException(String message) {
        super(message);
    }
}
