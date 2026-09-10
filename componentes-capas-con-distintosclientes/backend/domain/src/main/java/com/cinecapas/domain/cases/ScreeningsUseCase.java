package com.cinecapas.domain.cases;

import com.cinecapas.domain.models.ScreeningModel;
import com.cinecapas.domain.ports.in.InScreenings;
import com.cinecapas.persistence.repos.ScreeningModelDaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ScreeningsUseCase implements InScreenings {
    private final ScreeningModelDaoRepository screeningsRepository;

    @Override
    public Optional<ScreeningModel> porId(long funcionId) {
        return Optional.empty();
    }

    @Override
    public List<ScreeningModel> porPelicula(long peliculaId) {
        return List.of();
    }
}
