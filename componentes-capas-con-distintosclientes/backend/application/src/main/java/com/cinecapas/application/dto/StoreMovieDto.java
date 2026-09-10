package com.cinecapas.application.dto;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
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
