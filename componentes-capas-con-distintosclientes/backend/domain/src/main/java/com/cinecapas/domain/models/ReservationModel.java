package com.cinecapas.domain.models;

import com.cinecapas.domain.enums.ReservationStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @Builder
public class ReservationModel {
    private long id;
    private String codigo;
    private long funcionId;
    private String nombreCliente;
    private String email;
    private List<String> asientos;
    private ReservationStatus estado;
    private LocalDateTime creadaE;
}
