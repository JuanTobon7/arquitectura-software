package com.cinecapas.application.services.impl;

import com.cinecapas.application.dto.MovieDto;
import com.cinecapas.application.dto.StoreMovieDto;
import com.cinecapas.application.mappers.MoviesMappers;
import com.cinecapas.application.services.contracts.MoviesService;
import com.cinecapas.domain.enums.Formats;
import com.cinecapas.domain.models.MoviesModel;
import com.cinecapas.domain.ports.in.InCatalogMovies;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MoviesServiceImpl implements MoviesService {
    private final InCatalogMovies inCatalogMovies;

    public List<MovieDto> buscar(String genero, String formatoTexto) {
        Formats formato = null;
        if (formatoTexto != null && !formatoTexto.isBlank()
                && !"todas".equalsIgnoreCase(formatoTexto.trim())) {
            formato = Formats.desdeEtiqueta(formatoTexto)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Formato desconocido: " + formatoTexto));
        }
        return inCatalogMovies.buscar(genero, formato).stream()
                .map(MoviesMappers::toDto)
                .toList();
    }

    @Override
    public MovieDto registrar(StoreMovieDto datos) {
        MoviesModel response = inCatalogMovies.registrar(MoviesMappers.toModel(datos));
        return MoviesMappers.toDto(response);
    }
}