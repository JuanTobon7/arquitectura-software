package com.cinecapas.application.mappers;

import com.cinecapas.application.dto.UserDto;
import com.cinecapas.domain.models.UsersModel;

public final class UsersMappers {

    public UsersModel toModel(UserDto dto) {
        return UsersModel.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .email(dto.getEmail())
                .build();
    }

    public UserDto toDto(UsersModel model) {
        return UserDto.builder()
                .id(model.getId())
                .email(model.getEmail())
                .nombre(model.getNombre())
                .build();
    }
}