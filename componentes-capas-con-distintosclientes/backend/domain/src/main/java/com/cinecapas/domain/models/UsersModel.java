package com.cinecapas.domain.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter @Setter
@Builder
public class UsersModel {
    private static final Pattern EMAIL_VALIDO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final Long id;
    private final String nombre;
    private final String email;
    private final String claveHash;
}
