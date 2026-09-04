package com.weeklyreport.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.weeklyreport.backend.entity.ReviewHistory;

public interface ReviewHistoryRepository extends JpaRepository<ReviewHistory, Long> {
}
