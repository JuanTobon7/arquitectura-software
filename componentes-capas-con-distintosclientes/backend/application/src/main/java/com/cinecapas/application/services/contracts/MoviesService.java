package com.cinecapas.application.services.contracts;

import com.cinecapas.application.dto.MovieDto;
import com.cinecapas.application.dto.StoreMovieDto;

import java.util.List;

public interface MoviesService {
    List<MovieDto> buscar(String genero, String formatoTexto);
    MovieDto registrar(StoreMovieDto datos);
}
