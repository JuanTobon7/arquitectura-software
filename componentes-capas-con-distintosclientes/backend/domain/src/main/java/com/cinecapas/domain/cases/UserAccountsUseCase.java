package com.cinecapas.domain.cases;

import com.cinecapas.domain.exceptions.ExistingEmail;
import com.cinecapas.domain.mappers.UsersMappers;
import com.cinecapas.domain.models.UsersModel;
import com.cinecapas.domain.ports.in.InUserAccounts;
import com.cinecapas.persistence.daos.UsersModelDao;
import com.cinecapas.persistence.repos.UsersModelDaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAccountsUseCase implements InUserAccounts {

    private final UsersModelDaoRepository usersRepository;

    @Override
    public UsersModel registrar(String nombre, String email, String hashedPassword) {
        if (usersRepository.findByEmail(email) != null) {
                    throw new ExistingEmail(
                "Ya existe un usuario con el email: " + email);
        }

        UsersModel user = UsersModel.builder()
                .nombre(nombre)
                .email(email)
                .claveHash(hashedPassword)
                .build();

        UsersModelDao dao = UsersMappers.toDao(user);
        dao = usersRepository.save(dao);
        return UsersMappers.toModel(dao);
    }

    @Override
    public UsersModel porEmail(String email) {
        UsersModelDao user = usersRepository.findByEmail(email);
        return UsersMappers.toModel(user);
    }
}
