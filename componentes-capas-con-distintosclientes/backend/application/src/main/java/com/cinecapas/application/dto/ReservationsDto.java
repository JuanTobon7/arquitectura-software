package com.cinecapas.application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @Builder
public class ReservationsDto {
    private long id;
    private String codigo;
    private long funcionId;
    private String nombreCliente;
    private String email;
    private List<String> asientos;
    private String estado;
    private LocalDateTime creadaE;
}
