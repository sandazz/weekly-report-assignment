package com.weeklyreport.backend.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.weeklyreport.backend.dto.dashboard.ProjectWorkloadDto;
import com.weeklyreport.backend.entity.ReportTask;

public interface ReportTaskRepository extends JpaRepository<ReportTask, Long> {

    List<ReportTask> findByReportId(Long reportId);

    @Query("""
            SELECT new com.weeklyreport.backend.dto.dashboard.ProjectWorkloadDto(
                t.report.project.id, t.report.project.name, COUNT(t), COALESCE(SUM(t.plannedHours), 0.0))
            FROM ReportTask t
            WHERE t.report.weekStart BETWEEN :weekStart AND :weekEnd
            GROUP BY t.report.project.id, t.report.project.name
            """)
    List<ProjectWorkloadDto> findWorkloadByProject(
            @Param("weekStart") LocalDate weekStart, @Param("weekEnd") LocalDate weekEnd);

    // Raw projection (not TaskTrendPointDto directly) so the service can format the
    // week label
    // and fill in zero-count weeks that have no rows at all.
    interface WeeklyCompletedCount {
        LocalDate getWeekStart();

        Long getCompletedCount();
    }

    @Query("""
            SELECT t.report.weekStart AS weekStart, COUNT(t) AS completedCount
            FROM ReportTask t
            WHERE t.status = com.weeklyreport.backend.entity.enums.TaskStatus.COMPLETED
              AND t.report.weekStart >= :fromWeek
              AND (:userId IS NULL OR t.report.user.id = :userId)
            GROUP BY t.report.weekStart
            """)
    List<WeeklyCompletedCount> findWeeklyCompletedCounts(
            @Param("fromWeek") LocalDate fromWeek, @Param("userId") Long userId);
}
