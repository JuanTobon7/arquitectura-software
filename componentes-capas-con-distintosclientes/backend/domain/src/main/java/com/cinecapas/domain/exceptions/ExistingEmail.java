package com.cinecapas.domain.exceptions;

public class ExistingEmail extends RuntimeException{
    public ExistingEmail(String email) {
        super(" El email " + email + " ya existe");
    }
}
