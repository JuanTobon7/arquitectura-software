package com.cinecapas.domain.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @Builder
public class ScreeningModel {
    private long id;
    private long peliculaId;
    private LocalDateTime fechaHora;
    private String formato;
    private String sala;
    private int filas;
    private int columnas;
    private double preci;
}
