package com.weeklyreport.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.weeklyreport.backend.entity.ReportBlocker;
import com.weeklyreport.backend.entity.enums.ReportStatus;

public interface ReportBlockerRepository extends JpaRepository<ReportBlocker, Long> {

    List<ReportBlocker> findByReportId(Long reportId);

    long countByReport_StatusNot(ReportStatus status);
}
