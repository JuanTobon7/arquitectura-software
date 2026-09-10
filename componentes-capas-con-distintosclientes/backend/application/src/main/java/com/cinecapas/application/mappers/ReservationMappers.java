package com.cinecapas.application.mappers;

import com.cinecapas.application.dto.ReservationsDto;
import com.cinecapas.domain.models.ReservationModel;

public final class ReservationMappers {
    public static ReservationModel toModel(ReservationsDto reservationsDto) {
        return ReservationModel.builder()
                .id(reservationsDto.getId())
                .codigo(reservationsDto.getCodigo())
                .email(reservationsDto.getEmail())
                .asientos(reservationsDto.getAsientos())
                .creadaE(reservationsDto.getCreadaE())
                .funcionId(reservationsDto.getFuncionId())
                .nombreCliente(reservationsDto.getNombreCliente())
                .build();
    }

    public static ReservationsDto toDto(ReservationModel reservationModel) {
        return ReservationsDto.builder()
                .id(reservationModel.getId())
                .codigo(reservationModel.getCodigo())
                .email(reservationModel.getEmail())
                .asientos(reservationModel.getAsientos())
                .creadaE(reservationModel.getCreadaE())
                .funcionId(reservationModel.getFuncionId())
                .nombreCliente(reservationModel.getNombreCliente())
                .estado(reservationModel.getEstado().toString())
                .build();
    }
}
