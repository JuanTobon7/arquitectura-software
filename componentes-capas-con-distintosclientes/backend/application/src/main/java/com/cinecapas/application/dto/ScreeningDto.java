package com.cinecapas.application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@Builder
public class ScreeningDto {
    private long id;
    private long peliculaId;
    private LocalDateTime fechaHora;
    private String formato;
    private String sala;
    private double preci;
    private int filas;
    private int columnas;
}
