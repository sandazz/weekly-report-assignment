package com.weeklyreport.backend.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.weeklyreport.backend.dto.dashboard.HoursByTypeDto;
import com.weeklyreport.backend.entity.ReportHour;

public interface ReportHourRepository extends JpaRepository<ReportHour, Long> {

    List<ReportHour> findByReportId(Long reportId);

    @Query("""
            SELECT new com.weeklyreport.backend.dto.dashboard.HoursByTypeDto(h.taskType, COALESCE(SUM(h.hours), 0.0))
            FROM ReportHour h
            WHERE h.taskType IS NOT NULL
              AND h.report.weekStart BETWEEN :weekStart AND :weekEnd
            GROUP BY h.taskType
            """)
    List<HoursByTypeDto> findHoursByType(
            @Param("weekStart") LocalDate weekStart, @Param("weekEnd") LocalDate weekEnd);
}
