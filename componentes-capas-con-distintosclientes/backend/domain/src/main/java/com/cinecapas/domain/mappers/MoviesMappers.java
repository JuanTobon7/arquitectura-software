package com.cinecapas.domain.mappers;

import com.cinecapas.domain.enums.Formats;
import com.cinecapas.domain.models.MoviesModel;
import com.cinecapas.persistence.daos.MoviesModelDao;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class MoviesMappers {
    public static MoviesModelDao toDao(MoviesModel model){
        String formats = model.getFormatos().stream()
                .map(formato -> formato.name())
                .collect(Collectors.joining(", "));

        return MoviesModelDao.builder()
                .id(model.getId())
                .titulo(model.getTitulo())
                .sinopsis(model.getSinopsis())
                .clasificacion(model.getClasificacion())
                .duracionMinutos(model.getDuracionMinutos())
                .rating(model.getRating())
                .posterUrl(model.getPosterUrl())
                .formatos(formats)
                .genero(model.getGenero())
                .build();
    }

    public static MoviesModel toModel(MoviesModelDao dao){
        Set<Formats> formats = Arrays.stream(dao.getFormatos().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .flatMap(s -> Formats.desdeEtiqueta(s).stream())
                .collect(Collectors.toSet());

        return MoviesModel.builder()
                .id(dao.getId())
                .titulo(dao.getTitulo())
                .sinopsis(dao.getSinopsis())
                .clasificacion(dao.getClasificacion())
                .duracionMinutos(dao.getDuracionMinutos())
                .rating(dao.getRating())
                .posterUrl(dao.getPosterUrl())
                .formatos(formats)
                .genero(dao.getGenero())
                .build();
    }
}
