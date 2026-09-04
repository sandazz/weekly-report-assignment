package com.weeklyreport.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.weeklyreport.backend.entity.ReportAchievement;

public interface ReportAchievementRepository extends JpaRepository<ReportAchievement, Long> {

    List<ReportAchievement> findByReportId(Long reportId);
}
