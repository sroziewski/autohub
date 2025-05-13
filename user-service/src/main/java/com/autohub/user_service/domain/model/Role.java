package com.autohub.user_service.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class Role {
    private UUID id;
    private String name;
}

