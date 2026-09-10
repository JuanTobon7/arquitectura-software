package com.cinecapas.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class StoreUserDto {
    @NotBlank(message = "Email Necesario")
    @Email(message = "Email Invalido")
    private String email;

    @NotBlank(message = "Nombre Necesario")
    private String nombre;

    @NotBlank(message = "Password Necesario")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
}
