package com.weeklyreport.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.weeklyreport.backend.entity.ReportHour;

public interface ReportHourRepository extends JpaRepository<ReportHour, Long> {
}
