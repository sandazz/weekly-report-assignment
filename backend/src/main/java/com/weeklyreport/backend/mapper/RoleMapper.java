package com.weeklyreport.backend.mapper;

import com.weeklyreport.backend.dto.RoleRequestDto;
import com.weeklyreport.backend.dto.RoleResponseDto;
import com.weeklyreport.backend.entity.Role;

public final class RoleMapper {

    private RoleMapper() {
    }

    public static Role toEntity(RoleRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return Role.builder()
                .name(dto.name())
                .build();
    }

    public static RoleResponseDto toResponseDto(Role role) {
        if (role == null) {
            return null;
        }
        return new RoleResponseDto(
                role.getId(),
                role.getName(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }
}
