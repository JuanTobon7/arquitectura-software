package com.cinecapas.persistence.repos;

import com.cinecapas.persistence.daos.ReservationModelDao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationModelDaoRepository extends JpaRepository<ReservationModelDao, Long> {
    Optional<ReservationModelDao> findByFuncionIdAndEstado(Long funcionId, String estado);
}
