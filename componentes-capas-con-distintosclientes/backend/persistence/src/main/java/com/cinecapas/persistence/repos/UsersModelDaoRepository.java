package com.cinecapas.persistence.repos;

import com.cinecapas.persistence.daos.UsersModelDao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersModelDaoRepository extends JpaRepository<UsersModelDao, Long> {
    UsersModelDao findByEmail(String email);
}
