package com.weeklyreport.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.weeklyreport.backend.entity.ReportVersionTask;

public interface ReportVersionTaskRepository extends JpaRepository<ReportVersionTask, Long> {
}
