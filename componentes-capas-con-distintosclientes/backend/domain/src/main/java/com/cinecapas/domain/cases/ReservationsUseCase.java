package com.cinecapas.domain.cases;

import com.cinecapas.domain.enums.ReservationStatus;
import com.cinecapas.domain.mappers.ReservationMappers;
import com.cinecapas.domain.models.ReservationModel;
import com.cinecapas.domain.ports.in.InReservations;
import com.cinecapas.persistence.daos.ReservationModelDao;
import com.cinecapas.persistence.repos.ReservationModelDaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReservationsUseCase implements InReservations {
    private final ReservationModelDaoRepository reservasRepository;

    @Override
    public ReservationModel guardar(ReservationModel nueva) {
        ReservationModelDao dao = ReservationMappers.toDao(nueva);
        dao = reservasRepository.save(dao);
        return ReservationMappers.toModel(dao);
    }

    @Override
    public Optional<ReservationModel> porId(long reservaId) {
        ReservationModelDao dao = reservasRepository.findById(reservaId).orElse(null);
        return Optional.ofNullable(dao).map(ReservationMappers::toModel);
    }

    @Override
    public List<String> asientosOcupados(long funcionId) {
        String estado = ReservationStatus.ACTIVA.name();
        return reservasRepository.findByFuncionIdAndEstado(funcionId,estado ).stream()
                .flatMap(r -> r.getAsientos().stream())
                .distinct()
                .toList();
    }

    @Override
    public void marcarCancelada(long reservaId) {

    }
}
