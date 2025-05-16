package com.autohub.user_service.presentation.mapper;

import com.autohub.user_service.domain.entity.User;
import com.autohub.user_service.presentation.dto.user.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "secondName", source = "secondName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "birthDate", source = "birthDate")
    @Mapping(target = "lastLoginAt", source = "lastLoginAt")
    @Mapping(target = "verified", source = "verified")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "roles", expression = "java(user.getRoleNames())")
    UserResponse toResponse(User user);
}
