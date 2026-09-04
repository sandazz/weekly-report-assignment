package com.weeklyreport.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.weeklyreport.backend.entity.ReportBlocker;

public interface ReportBlockerRepository extends JpaRepository<ReportBlocker, Long> {

    List<ReportBlocker> findByReportId(Long reportId);
}
