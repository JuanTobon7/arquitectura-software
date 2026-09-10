package com.cinecapas.application.mappers;


import com.cinecapas.application.dto.MovieDto;
import com.cinecapas.application.dto.StoreMovieDto;
import com.cinecapas.domain.enums.Formats;
import com.cinecapas.domain.models.MoviesModel;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class MoviesMappers {

    public static MoviesModel toModel(MovieDto dto){
        Set<Formats> formats = dto.getFormatos().stream()
                .map(Formats::valueOf).collect(Collectors.toSet());

        return MoviesModel.builder()
                .id(dto.getId())
                .titulo(dto.getTitulo())
                .sinopsis(dto.getSinopsis())
                .clasificacion(dto.getClasificacion())
                .duracionMinutos(dto.getDuracionMinutos())
                .rating(dto.getRating())
                .posterUrl(dto.getPosterUrl())
                .formatos(formats)
                .genero(dto.getGenero())
                .build();
    }

    public static MoviesModel toModel(StoreMovieDto dto){
        Set<Formats> formats = dto.getFormatos().stream()
                .map(Formats::valueOf).collect(Collectors.toSet());

        return MoviesModel.builder()
                .titulo(dto.getTitulo())
                .sinopsis(dto.getSinopsis())
                .clasificacion(dto.getClasificacion())
                .duracionMinutos(dto.getDuracionMinutos())
                .rating(dto.getRating())
                .posterUrl(dto.getPosterUrl())
                .formatos(formats)
                .genero(dto.getGenero())
                .build();
    }

    public static MovieDto toDto(MoviesModel model){
        List<String> formats = model.getFormatos().stream()
                .map(Enum::name).collect(Collectors.toList());

        return MovieDto.builder()
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
}
