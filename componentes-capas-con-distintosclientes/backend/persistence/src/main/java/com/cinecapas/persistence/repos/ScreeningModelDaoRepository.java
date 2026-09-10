package com.cinecapas.persistence.repos;

import com.cinecapas.persistence.daos.ScreeningModelDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScreeningModelDaoRepository extends JpaRepository<ScreeningModelDao, Long> {
}
