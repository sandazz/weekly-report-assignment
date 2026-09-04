package com.weeklyreport.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.weeklyreport.backend.entity.ReportTask;

public interface ReportTaskRepository extends JpaRepository<ReportTask, Long> {

    List<ReportTask> findByReportId(Long reportId);
}
