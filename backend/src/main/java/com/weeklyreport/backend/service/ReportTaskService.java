package com.weeklyreport.backend.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weeklyreport.backend.dto.ReportTaskRequestDto;
import com.weeklyreport.backend.dto.ReportTaskResponseDto;
import com.weeklyreport.backend.entity.Report;
import com.weeklyreport.backend.entity.ReportTask;
import com.weeklyreport.backend.entity.enums.ReportStatus;
import com.weeklyreport.backend.exception.ResourceNotFoundException;
import com.weeklyreport.backend.mapper.ReportTaskMapper;
import com.weeklyreport.backend.repository.ReportTaskRepository;
import com.weeklyreport.backend.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportTaskService {

    private static final Set<ReportStatus> EDITABLE_STATUSES = Set.of(ReportStatus.DRAFT,
            ReportStatus.NEEDS_CORRECTION);

    private final ReportTaskRepository reportTaskRepository;
    private final ReportAccessGuard accessGuard;

    @Transactional
    public ReportTaskResponseDto addTask(Long reportId, ReportTaskRequestDto request, CustomUserDetails currentUser) {
        Report report = accessGuard.loadEditable(reportId, currentUser, EDITABLE_STATUSES);
        ReportTask task = ReportTaskMapper.toEntity(request);
        task.setReport(report);
        return ReportTaskMapper.toResponseDto(reportTaskRepository.save(task));
    }

    @Transactional
    public ReportTaskResponseDto updateTask(
            Long reportId, Long taskId, ReportTaskRequestDto request, CustomUserDetails currentUser) {
        accessGuard.loadEditable(reportId, currentUser, EDITABLE_STATUSES);
        ReportTask task = getTaskInReport(reportId, taskId);

        task.setTaskName(request.taskName());
        task.setPriority(request.priority());
        task.setPlannedPercentage(request.plannedPercentage());
        task.setActualPercentage(request.actualPercentage());
        task.setStatus(request.status());
        task.setPlannedHours(request.plannedHours());
        task.setSpentHours(request.spentHours());
        task.setDeliverable(request.deliverable());

        return ReportTaskMapper.toResponseDto(reportTaskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long reportId, Long taskId, CustomUserDetails currentUser) {
        accessGuard.loadEditable(reportId, currentUser, EDITABLE_STATUSES);
        reportTaskRepository.delete(getTaskInReport(reportId, taskId));
    }

    @Transactional(readOnly = true)
    public List<ReportTaskResponseDto> listTasks(Long reportId, CustomUserDetails currentUser) {
        accessGuard.loadViewable(reportId, currentUser);
        return reportTaskRepository.findByReportId(reportId).stream().map(ReportTaskMapper::toResponseDto).toList();
    }

    // Never leak whether a task ID exists under a different report — treat
    // mismatched parent as 404.
    private ReportTask getTaskInReport(Long reportId, Long taskId) {
        ReportTask task = reportTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
        if (!task.getReport().getId().equals(reportId)) {
            throw new ResourceNotFoundException("Task not found: " + taskId);
        }
        return task;
    }
}
