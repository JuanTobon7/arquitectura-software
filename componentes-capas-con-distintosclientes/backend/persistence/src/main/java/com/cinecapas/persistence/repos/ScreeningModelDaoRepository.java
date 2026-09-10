package com.cinecapas.persistence.repos;

import com.cinecapas.persistence.daos.ScreeningModelDao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScreeningModelDaoRepository extends JpaRepository<ScreeningModelDao, Long> {
}
