package com.weeklyreport.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.weeklyreport.backend.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
