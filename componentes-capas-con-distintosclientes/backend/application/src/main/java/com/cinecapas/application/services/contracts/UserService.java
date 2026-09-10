package com.cinecapas.application.services.contracts;

import com.cinecapas.application.dto.StoreUserDto;
import com.cinecapas.application.dto.UserDto;
import com.cinecapas.application.exceptions.DuplicatedEmailException;
import com.cinecapas.domain.exceptions.ExistingEmail;

public interface UserService {
    /**
     *
     * @param user
     * @return
     * @throws DuplicatedEmailException
     */
    UserDto registrar(StoreUserDto user) throws DuplicatedEmailException;

    UserDto autenticar(String email, String password);

}
