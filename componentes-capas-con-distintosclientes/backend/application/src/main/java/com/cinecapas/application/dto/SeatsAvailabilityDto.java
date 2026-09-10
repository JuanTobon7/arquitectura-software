package com.cinecapas.application.dto;

import lombok.*;

import java.util.List;

@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class SeatsAvailabilityDto {
    private long funcionId;
    private int filas;
    private int columnas;
    private List<String> ocupado;
}
