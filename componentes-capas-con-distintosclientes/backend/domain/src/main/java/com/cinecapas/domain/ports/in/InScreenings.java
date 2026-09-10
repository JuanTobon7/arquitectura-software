package com.cinecapas.domain.ports.in;

import com.cinecapas.domain.models.ScreeningModel;

import java.util.List;
import java.util.Optional;

public interface InScreenings {
    Optional<ScreeningModel> porId(long funcionId);

    List<ScreeningModel> porPelicula(long peliculaId);
}
