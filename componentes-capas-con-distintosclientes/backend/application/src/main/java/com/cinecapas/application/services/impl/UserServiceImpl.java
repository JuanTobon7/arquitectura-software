package com.cinecapas.application.services.impl;

import com.cinecapas.application.components.contract.EncryptPasswords;
import com.cinecapas.application.components.contract.MailNotificator;
import com.cinecapas.application.dto.StoreUserDto;
import com.cinecapas.application.dto.UserDto;
import com.cinecapas.application.exceptions.AccessNotGranted;
import com.cinecapas.application.exceptions.DuplicatedEmailException;
import com.cinecapas.application.services.contracts.UserService;
import com.cinecapas.domain.exceptions.ExistingEmail;
import com.cinecapas.domain.models.UsersModel;
import com.cinecapas.domain.ports.in.InUserAccounts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final InUserAccounts inUserAccounts;
    private final MailNotificator mailNotificator;
    private final EncryptPasswords encryptPasswords;

    @Override
    public UserDto registrar(StoreUserDto storeUserDto){
        try {
        String hashPassword = encryptPasswords.codificar(storeUserDto.getPassword());
        UsersModel user = inUserAccounts.registrar(
                storeUserDto.getNombre(),
                storeUserDto.getEmail(),
                hashPassword
        );
        return UserDto.builder()
                .id(user.getId())
                .nombre(user.getNombre())
                .email(user.getEmail())
                .build();
        }catch (ExistingEmail e){
            throw new DuplicatedEmailException("Email "+storeUserDto.getEmail() + "existente");
        }
    }

    @Override
    public UserDto autenticar(String email, String password) {
        UsersModel usersModel = inUserAccounts.porEmail(email);
        if (usersModel == null || !encryptPasswords.coincide(password, usersModel.getClaveHash())) {
            throw new AccessNotGranted("contraseñas invalidas");
        }
        return UserDto.builder()
                .id(usersModel.getId())
                .nombre(usersModel.getNombre())
                .email(usersModel.getEmail())
                .build();
    }
}
