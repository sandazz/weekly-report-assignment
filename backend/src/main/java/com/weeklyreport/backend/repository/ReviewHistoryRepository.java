package com.weeklyreport.backend.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.weeklyreport.backend.entity.ReviewHistory;

public interface ReviewHistoryRepository extends JpaRepository<ReviewHistory, Long> {

    List<ReviewHistory> findByReportIdOrderByCreatedAtAsc(Long reportId);

    List<ReviewHistory> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
