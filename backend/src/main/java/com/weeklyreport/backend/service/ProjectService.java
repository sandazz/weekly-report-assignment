package com.weeklyreport.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weeklyreport.backend.dto.ProjectRequestDto;
import com.weeklyreport.backend.dto.ProjectResponseDto;
import com.weeklyreport.backend.entity.Project;
import com.weeklyreport.backend.exception.ResourceNotFoundException;
import com.weeklyreport.backend.mapper.ProjectMapper;
import com.weeklyreport.backend.repository.ProjectRepository;
import com.weeklyreport.backend.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private static final String TEAM_MEMBER_ROLE = "TEAM_MEMBER";

    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public List<ProjectResponseDto> listProjects(boolean includeInactive, CustomUserDetails currentUser) {
        // TEAM_MEMBER always gets active-only, regardless of the requested flag.
        boolean effectiveIncludeInactive = includeInactive && !TEAM_MEMBER_ROLE.equals(currentUser.getRoleName());
        List<Project> projects = effectiveIncludeInactive
                ? projectRepository.findAll()
                : projectRepository.findByActiveTrue();
        return projects.stream().map(ProjectMapper::toResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponseDto getProject(Long id) {
        return ProjectMapper.toResponseDto(getOrThrow(id));
    }

    @Transactional
    public ProjectResponseDto createProject(ProjectRequestDto request) {
        Project project = ProjectMapper.toEntity(request);
        return ProjectMapper.toResponseDto(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponseDto updateProject(Long id, ProjectRequestDto request) {
        Project project = getOrThrow(id);
        project.setName(request.name());
        project.setDescription(request.description());
        if (request.active() != null) {
            project.setActive(request.active());
        }
        return ProjectMapper.toResponseDto(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(Long id) {
        // Soft delete only: reports hold a FK to this project, so hard-deleting would
        // break existing reports' history/integrity.
        Project project = getOrThrow(id);
        project.setActive(false);
        projectRepository.save(project);
    }

    private Project getOrThrow(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
    }
}
