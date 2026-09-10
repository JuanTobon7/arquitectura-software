package com.cinecapas.domain.models;

import com.cinecapas.domain.enums.Formats;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter @Setter @Builder
public class MoviesModel {
    private final Long id;
    private final String titulo;
    private final String sinopsis;
    private final String genero;
    private final String clasificacion;
    private final int duracionMinutos;
    private final double rating;
    private final Set<Formats> formatos;
    private final String posterUrl;
}
