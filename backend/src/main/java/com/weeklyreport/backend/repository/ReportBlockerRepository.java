package com.weeklyreport.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.weeklyreport.backend.entity.ReportBlocker;

public interface ReportBlockerRepository extends JpaRepository<ReportBlocker, Long> {
}
