package com.cinecapas.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty(value = "clave")
    String password;
}
