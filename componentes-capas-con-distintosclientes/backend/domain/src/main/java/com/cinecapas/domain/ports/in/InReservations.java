package com.cinecapas.domain.ports.in;

import com.cinecapas.domain.enums.ReservationStatus;
import com.cinecapas.domain.models.ReservationModel;

import java.util.List;
import java.util.Optional;

public interface InReservations {
    /**
     *
     * @param nueva
     * @param status
     * @return
     */
    ReservationModel guardar(ReservationModel nueva);

    /**
     *
     * @param reservaId
     * @return
     */
    Optional<ReservationModel> porId(long reservaId);

    /**
     *
     * @param funcionId
     * @return
     */
    List<String> asientosOcupados(long funcionId);

    /**
     *
     * @param reservaId
     */
    void marcarCancelada(long reservaId);
}
