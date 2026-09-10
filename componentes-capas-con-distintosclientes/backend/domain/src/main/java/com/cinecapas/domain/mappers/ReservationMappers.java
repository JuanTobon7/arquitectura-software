package com.cinecapas.domain.mappers;

import com.cinecapas.domain.enums.ReservationStatus;
import com.cinecapas.domain.models.ReservationModel;
import com.cinecapas.persistence.daos.ReservationModelDao;

public final class ReservationMappers {
    public static ReservationModelDao toDao(ReservationModel model){
        return ReservationModelDao.builder()
                .id(model.getId())
                .email(model.getEmail())
                .codigo(model.getCodigo())
                .asientos(model.getAsientos())
                .creadaEn(model.getCreadaE())
                .funcionId(model.getFuncionId())
                .nombreCliente(model.getNombreCliente())
                .estado(model.getEstado().toString())
                .build();
    }

    public static ReservationModel toModel(ReservationModelDao dao){
        return ReservationModel.builder()
                .id(dao.getId())
                .email(dao.getEmail())
                .codigo(dao.getCodigo())
                .asientos(dao.getAsientos())
                .creadaE(dao.getCreadaEn())
                .funcionId(dao.getFuncionId())
                .nombreCliente(dao.getNombreCliente())
                .estado(ReservationStatus.valueOf(dao.getEstado()))
                .build();
    }
}
