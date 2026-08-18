package com.reconstruyecol.ayudaterremoto.security;

public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException(String message) {
        super(message);
    }
}
