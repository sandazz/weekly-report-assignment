package com.weeklyreport.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.weeklyreport.backend.entity.ReportAchievement;

public interface ReportAchievementRepository extends JpaRepository<ReportAchievement, Long> {
}
