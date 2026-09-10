package com.cinecapas.application.components.impl;

import com.cinecapas.application.components.contract.EncryptPasswords;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class EncryptPasswordsImpl implements EncryptPasswords {
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    @Override
    public String codificar(String claveTextoPlano) {
        return bcrypt.encode(claveTextoPlano);
    }

    @Override
    public boolean coincide(String claveTextoPlano, String claveHash) {
        return bcrypt.matches(claveTextoPlano, claveHash);
    }
}
