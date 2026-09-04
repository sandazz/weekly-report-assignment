package com.weeklyreport.backend.mapper;

import com.weeklyreport.backend.dto.ProjectRequestDto;
import com.weeklyreport.backend.dto.ProjectResponseDto;
import com.weeklyreport.backend.entity.Project;

public final class ProjectMapper {

    private ProjectMapper() {
    }

    public static Project toEntity(ProjectRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return Project.builder()
                .name(dto.name())
                .description(dto.description())
                .active(dto.active() == null || dto.active())
                .build();
    }

    public static ProjectResponseDto toResponseDto(Project project) {
        if (project == null) {
            return null;
        }
        return new ProjectResponseDto(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.isActive(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
