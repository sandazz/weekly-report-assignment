package com.weeklyreport.backend.repository;

import org.springframework.data.jpa.domain.Specification;

import com.weeklyreport.backend.entity.Report;
import com.weeklyreport.backend.entity.enums.ReportStatus;

public final class ReportSpecifications {

    private ReportSpecifications() {
    }

    public static Specification<Report> belongsToUser(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Report> hasStatus(ReportStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Report> hasProject(Long projectId) {
        return (root, query, cb) -> cb.equal(root.get("project").get("id"), projectId);
    }
}
