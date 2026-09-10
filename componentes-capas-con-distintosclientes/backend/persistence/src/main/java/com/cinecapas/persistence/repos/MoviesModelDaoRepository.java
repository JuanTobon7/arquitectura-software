package com.cinecapas.persistence.repos;

import com.cinecapas.persistence.daos.MoviesModelDao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoviesModelDaoRepository extends JpaRepository<MoviesModelDao, Long> {
}
