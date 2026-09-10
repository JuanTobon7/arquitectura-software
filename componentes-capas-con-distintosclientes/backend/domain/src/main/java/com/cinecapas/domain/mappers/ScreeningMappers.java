package com.cinecapas.domain.mappers;

import com.cinecapas.domain.models.ScreeningModel;
import com.cinecapas.persistence.daos.ScreeningModelDao;

public final class ScreeningMappers {

    public ScreeningModelDao toDao(ScreeningModel model) {
        ScreeningModelDao dao = new ScreeningModelDao();
        dao.setId(model.getId());
        dao.setPeliculaId(model.getPeliculaId());
        dao.setFechaHora(model.getFechaHora());
        dao.setFormato(model.getFormato());
        dao.setSala(model.getSala());
        dao.setFilas(model.getFilas());
        dao.setColumnas(model.getColumnas());
        dao.setPrecio(model.getPreci());
        return dao;
    }

    public ScreeningModel toModel(ScreeningModelDao dao) {
        return ScreeningModel.builder()
                .id(dao.getId())
                .peliculaId(dao.getPeliculaId())
                .fechaHora(dao.getFechaHora())
                .formato(dao.getFormato())
                .sala(dao.getSala())
                .filas(dao.getFilas())
                .columnas(dao.getColumnas())
                .preci(dao.getPrecio())
                .build();
    }
}