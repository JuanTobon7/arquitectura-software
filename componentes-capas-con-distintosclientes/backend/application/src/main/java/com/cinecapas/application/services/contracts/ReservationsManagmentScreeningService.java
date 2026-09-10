package com.cinecapas.application.services.contracts;

import com.cinecapas.application.dto.*;

import java.util.List;
import java.util.Optional;

public interface ReservationsManagmentScreeningService {
    List<ScreeningDto> funcionesDePelicula(long peliculaId);

    Optional<ScreeningDto> funcionPorId(long funcionId);

    Optional<ReservationsDto> reservaPorId(long reservaId);

    SeatsAvailabilityDto disponibilidad(long funcionId);

    ReservationsDto crear(ReservationRequestDto solicitud);

    void cancelar(long reservaId);
}
