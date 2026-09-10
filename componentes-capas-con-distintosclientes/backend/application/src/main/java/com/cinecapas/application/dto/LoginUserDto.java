package com.cinecapas.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginUserDto {
    @NotBlank(message = "Email Necesario")
    @Email(message = "Email Invalido")
    String email;
    @NotBlank(message = "Password Necesario")
    String password;
}
