package com.weeklyreport.backend.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.weeklyreport.backend.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report> {

    Optional<Report> findByUserIdAndProjectIdAndWeekStart(Long userId, Long projectId, LocalDate weekStart);
}
