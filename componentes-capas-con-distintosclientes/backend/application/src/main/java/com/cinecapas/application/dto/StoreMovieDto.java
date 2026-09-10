package com.cinecapas.application.dto;

import jakarta.validation.constraints.*;

import java.util.List;

public class StoreMovieDto {
    @NotBlank(message = "El título es obligatorio")
    String titulo;

    String sinopsis;
    @NotBlank(message = "El género es obligatorio")
    String genero;

    String clasificacion;
    @Positive(message = "La duración debe ser positiva")
    @NotNull(message = "Debes ingresar la duracion")
    int duracionMinutos;

    @Min(0) @Max(10)
    double rating;

    @NotEmpty(message = "Debe indicar al menos un formato")
    List<String> formatos;

    String posterUrl;
}
