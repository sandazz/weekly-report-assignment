package com.weeklyreport.backend.repository;

import java.time.LocalDate;

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

    public static Specification<Report> weekStartBetween(LocalDate from, LocalDate to) {
        if (from != null && to != null) {
            return (root, query, cb) -> cb.between(root.get("weekStart"), from, to);
        }
        if (from != null) {
            return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("weekStart"), from);
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("weekStart"), to);
    }

    // Manual null-checked chaining (not Specification.where(null)) — that overload
    // is ambiguous
    // in this Spring Data JPA version between the Specification<T> and
    // PredicateSpecification<T>
    // variants of where().
    public static Specification<Report> combine(
            Long userId, ReportStatus status, Long projectId, LocalDate fromDate, LocalDate toDate) {
        Specification<Report> spec = null;
        if (userId != null) {
            spec = belongsToUser(userId);
        }
        if (status != null) {
            Specification<Report> hasStatus = hasStatus(status);
            spec = spec == null ? hasStatus : spec.and(hasStatus);
        }
        if (projectId != null) {
            Specification<Report> hasProject = hasProject(projectId);
            spec = spec == null ? hasProject : spec.and(hasProject);
        }
        if (fromDate != null || toDate != null) {
            Specification<Report> weekRange = weekStartBetween(fromDate, toDate);
            spec = spec == null ? weekRange : spec.and(weekRange);
        }
        return spec;
    }
}
