package com.weeklyreport.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.weeklyreport.backend.entity.Report;
import com.weeklyreport.backend.entity.enums.ReportStatus;

public interface ReportRepository extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report> {

    Optional<Report> findByUserIdAndProjectIdAndWeekStart(Long userId, Long projectId, LocalDate weekStart);

    long countByWeekStartAndStatusIn(LocalDate weekStart, List<ReportStatus> statuses);

    List<Report> findByWeekStart(LocalDate weekStart);

    long countByStatus(ReportStatus status);
}
