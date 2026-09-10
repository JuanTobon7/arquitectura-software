package com.cinecapas.application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter @Builder
public class MovieDto {
    private Long id;
    private String titulo;
    private String sinopsis;
    private String genero;
    private String clasificacion;
    private int duracionMinutos;
    private double rating;
    private List<String> formatos;
    private String posterUrl;
}
