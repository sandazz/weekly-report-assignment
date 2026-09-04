package com.weeklyreport.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.weeklyreport.backend.entity.ReportHour;

public interface ReportHourRepository extends JpaRepository<ReportHour, Long> {

    List<ReportHour> findByReportId(Long reportId);
}
