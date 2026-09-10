package com.cinecapas.domain.mappers;

import com.cinecapas.domain.models.UsersModel;
import com.cinecapas.persistence.daos.UsersModelDao;

public final class UsersMappers {
    public static UsersModelDao toDao(UsersModel model) {
        return UsersModelDao.builder()
                .id(model.getId())
                .nombre(model.getNombre())
                .email(model.getEmail())
                .claveHash(model.getClaveHash())
                .build();
    }

    public static UsersModel toModel(UsersModelDao dao){
        return UsersModel.builder()
                .id(dao.getId())
                .nombre(dao.getNombre())
                .email(dao.getEmail())
                .claveHash(dao.getClaveHash())
                .build();
    }
}
