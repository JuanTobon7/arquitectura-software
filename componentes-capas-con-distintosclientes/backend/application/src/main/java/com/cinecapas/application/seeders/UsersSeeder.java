package com.cinecapas.application.seeders;

import com.cinecapas.application.dto.StoreUserDto;
import com.cinecapas.application.exceptions.DuplicatedEmailException;
import com.cinecapas.application.services.contracts.UserService;
import com.cinecapas.domain.ports.in.InUserAccounts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsersSeeder implements CommandLineRunner {

    private final UserService userService;
    private final InUserAccounts usersRepository;

    @Override
    public void run(String... args) {
        registrar("Administrador", "admin@micos.com", "admin123");
        registrar("Juan Pérez", "juan@micos.com", "juan123");
        registrar("María García", "maria@micos.com", "maria123");
    }

    private void registrar(String nombre, String email, String password) {
        if (usersRepository.porEmail(email) != null) {
            log.info("[UsersSeeder] Ya existe: {}", email);
            return;
        }
        try {
            StoreUserDto dto = new StoreUserDto();
            dto.setNombre(nombre);
            dto.setEmail(email);
            dto.setPassword(password);
            userService.registrar(dto);
            log.info("[UsersSeeder] Usuario creado: {}", email);
        } catch (DuplicatedEmailException e) {
            log.info("[UsersSeeder] Ya existe: {}", email);
        }
    }
}
