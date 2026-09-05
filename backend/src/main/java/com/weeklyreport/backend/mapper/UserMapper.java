package com.weeklyreport.backend.mapper;

import com.weeklyreport.backend.dto.UserRequestDto;
import com.weeklyreport.backend.dto.UserResponseDto;
import com.weeklyreport.backend.dto.UserSummaryDto;
import com.weeklyreport.backend.entity.User;

// Note: role association is resolved and set by the service layer, not here.
public final class UserMapper {

    private UserMapper() {
    }

    public static User toEntity(UserRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return User.builder()
                .name(dto.name())
                .email(dto.email())
                .password(dto.password())
                .active(dto.active() == null || dto.active())
                .build();
    }

    public static UserResponseDto toResponseDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                RoleMapper.toResponseDto(user.getRole()),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    public static UserSummaryDto toSummaryDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserSummaryDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().getName(),
                user.isActive(),
                user.getCreatedAt());
    }
}
