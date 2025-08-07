package com.flynaut.workforcemgmt.service;

import com.flynaut.workforcemgmt.dto.*;
import com.flynaut.workforcemgmt.model.enums.Priority;

import java.util.List;

public interface TaskManagementService {
    List<TaskManagementDto> createTasks(TaskCreateRequest request);
    List<TaskManagementDto> updateTasks(UpdateTaskRequest request);
    String assignByReference(AssignByReferenceRequest request);
    List<TaskManagementDto> fetchTasksByDate(TaskFetchByDateRequest request);
    TaskManagementDto findTaskById(Long id);
    TaskManagementDto updatePriority(Long taskId, Priority priority);
    List<TaskManagementDto> fetchTasksByPriority(Priority priority);
    void addComment(Long taskId, CommentDto commentDto);
    List<Object> getTaskHistory(Long taskId);
}