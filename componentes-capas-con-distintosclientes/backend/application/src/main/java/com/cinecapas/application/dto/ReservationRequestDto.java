package com.cinecapas.application.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class ReservationRequestDto {
    private long screeningId;
    private String nombreCliente;
    private String email;
    private List<String> asiento;
}
