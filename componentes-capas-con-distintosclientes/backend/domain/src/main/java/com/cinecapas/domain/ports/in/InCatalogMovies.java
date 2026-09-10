package com.cinecapas.domain.ports.in;

import com.cinecapas.domain.enums.Formats;
import com.cinecapas.domain.models.MoviesModel;

import java.util.List;
import java.util.Optional;

public interface InCatalogMovies {
    MoviesModel registrar(MoviesModel pelicula);

    List<MoviesModel> buscar(String genero, Formats formato);

    Optional<MoviesModel> porId(long id);
}
