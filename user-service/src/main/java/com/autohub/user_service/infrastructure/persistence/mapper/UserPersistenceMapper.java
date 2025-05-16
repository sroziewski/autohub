package com.autohub.user_service.infrastructure.persistence.mapper;

import com.autohub.user_service.domain.entity.Role;
import com.autohub.user_service.domain.entity.RoleType;
import com.autohub.user_service.domain.entity.User;
import com.autohub.user_service.domain.entity.UserStatus;
import com.autohub.user_service.infrastructure.persistence.entity.RoleEntity;
import com.autohub.user_service.infrastructure.persistence.entity.RoleTypeEntity;
import com.autohub.user_service.infrastructure.persistence.entity.UserEntity;
import com.autohub.user_service.infrastructure.persistence.entity.UserStatusEntity;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.Set;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserPersistenceMapper {

    UserPersistenceMapper INSTANCE = Mappers.getMapper(UserPersistenceMapper.class);

    /**
     * Maps a persistence entity to a domain model
     *
     * @param entity The UserEntity to convert
     * @return The domain User
     */
    User toDomain(UserEntity entity);

    /**
     * Maps a domain model to a persistence entity
     *
     * @param domain The domain User to convert
     * @return The persistence UserEntity
     */
    @Mapping(target = "createdAt", ignore = true) // Handled by @PrePersist
    @Mapping(target = "updatedAt", ignore = true) // Handled by @PreUpdate
    UserEntity toEntity(User domain);

    /**
     * Maps a Role domain to a RoleEntity
     */
    @Mapping(target = "user", ignore = true)
    RoleEntity roleToRoleEntity(Role role);

    /**
     * Maps a RoleEntity to a Role domain
     */
    Role roleEntityToRole(RoleEntity roleEntity);

    /**
     * Maps a set of RoleEntity objects to a set of Role domains
     */
    Set<Role> roleEntitiesToRoles(Set<RoleEntity> roleEntities);

    /**
     * Maps a set of Role domains to a set of RoleEntity objects
     */
    Set<RoleEntity> rolesToRoleEntities(Set<Role> roles);

    /**
     * Maps domain RoleType to entity RoleType
     */
    default RoleTypeEntity mapRoleType(RoleType roleType) {
        if (roleType == null) {
            return null;
        }
        return RoleTypeEntity.valueOf(roleType.name());
    }

    /**
     * Maps entity RoleType to domain RoleType
     */
    default RoleType mapEntityRoleType(RoleTypeEntity roleTypeEntity) {
        if (roleTypeEntity == null) {
            return null;
        }
        return RoleType.valueOf(roleTypeEntity.name());
    }

    /**
     * Maps domain UserStatus to entity UserStatus
     */
    default UserStatusEntity mapUserStatus(UserStatus status) {
        if (status == null) {
            return null;
        }
        return UserStatusEntity.valueOf(status.name());
    }

    /**
     * Maps entity UserStatus to domain UserStatus
     */
    default UserStatus mapEntityUserStatus(UserStatusEntity status) {
        if (status == null) {
            return null;
        }
        return UserStatus.valueOf(status.name());
    }

    /**
     * After mapping from entity to domain, handle the roles
     */
    @AfterMapping
    default void afterToDomain(UserEntity entity, @MappingTarget User.UserBuilder builder) {
        if (entity.getRoles() != null && !entity.getRoles().isEmpty()) {
            Set<Role> roles = roleEntitiesToRoles(entity.getRoles());
            builder.roles(roles);
        }
    }

    /**
     * After mapping from domain to entity, handle the roles
     */
    @AfterMapping
    default void afterToEntity(User domain, @MappingTarget UserEntity entity) {
        if (domain.getRoles() != null) {
            entity.getRoles().clear(); // Clear existing roles

            for (Role role : domain.getRoles()) {
                RoleEntity roleEntity = roleToRoleEntity(role);
                roleEntity.setUser(entity);
                roleEntity.setUserId(domain.getId());
                entity.getRoles().add(roleEntity);
            }
        }
    }
}
