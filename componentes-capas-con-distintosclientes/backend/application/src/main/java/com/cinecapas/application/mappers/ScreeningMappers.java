package com.cinecapas.application.mappers;

import com.cinecapas.application.dto.ScreeningDto;
import com.cinecapas.domain.models.ScreeningModel;

public final class ScreeningMappers {

    public static ScreeningModel toModel(ScreeningDto dto) {
        return ScreeningModel.builder()
                .id(dto.getId())
                .peliculaId(dto.getPeliculaId())
                .fechaHora(dto.getFechaHora())
                .formato(dto.getFormato())
                .sala(dto.getSala())
                .preci(dto.getPreci())
                .build();
    }

    public static ScreeningDto toDto(ScreeningModel model) {
        return ScreeningDto.builder()
                .id(model.getId())
                .peliculaId(model.getPeliculaId())
                .fechaHora(model.getFechaHora())
                .formato(model.getFormato())
                .sala(model.getSala())
                .preci(model.getPreci())
                .build();
    }
}