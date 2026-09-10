package com.cinecapas.domain.ports.in;

import com.cinecapas.domain.models.UsersModel;

public interface InUserAccounts {
    /**
     *
     * @param nombre
     * @param email
     * @param hashedPassword
     * @return
     */
    UsersModel registrar(String nombre, String email, String hashedPassword);

    UsersModel porEmail(String email);

}
