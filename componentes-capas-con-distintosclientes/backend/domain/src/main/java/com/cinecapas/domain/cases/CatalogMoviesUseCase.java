package com.cinecapas.domain.cases;

import com.cinecapas.domain.enums.Formats;
import com.cinecapas.domain.mappers.MoviesMappers;
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
        var dao = moviesRepository.save(MoviesMappers.toDao(pelicula));
        return MoviesMappers.toModel(dao);
    }

    @Override
    public List<MoviesModel> buscar(String genero, Formats formato) {
        return moviesRepository.findAll().stream()
                .map(MoviesMappers::toModel)
                .toList();
    }

    @Override
    public Optional<MoviesModel> porId(long id) {
        return moviesRepository.findById(id)
                .map(MoviesMappers::toModel);
    }
}
