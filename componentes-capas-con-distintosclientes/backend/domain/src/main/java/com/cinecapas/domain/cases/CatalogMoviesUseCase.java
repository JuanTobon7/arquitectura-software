package com.cinecapas.domain.cases;

import com.cinecapas.domain.enums.Formats;
import com.cinecapas.domain.models.MoviesModel;
import com.cinecapas.domain.ports.in.InCatalogMovies;
import com.cinecapas.persistence.repos.MoviesModelDaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CatalogMoviesUseCase implements InCatalogMovies {

    private final MoviesModelDaoRepository moviesRepository;

    @Override
    public MoviesModel registrar(MoviesModel pelicula) {

        return moviesRepository.saveAll(pelicula);
    }

    @Override
    public List<MoviesModel> buscar(String genero, Formats formato) {
        return moviesRepository.buscarTodas();
    }

    @Override
    public Optional<MoviesModel> porId(long id) {
        return moviesRepository.buscarPorId(id);
    }
}
