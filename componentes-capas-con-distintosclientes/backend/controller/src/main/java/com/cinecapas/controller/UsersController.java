package com.cinecapas.controller;

import com.cinecapas.application.dto.LoginUserDto;
import com.cinecapas.application.dto.StoreUserDto;
import com.cinecapas.application.dto.UserDto;
import com.cinecapas.application.services.contracts.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsersController {
    private final UserService servicio;

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto registrar(@Valid @RequestBody StoreUserDto dto) {
        return servicio.registrar(dto);
    }

    @PostMapping("/login")
    public UserDto login(@Valid @RequestBody LoginUserDto dto) {
        return servicio.autenticar(dto);
    }
}
