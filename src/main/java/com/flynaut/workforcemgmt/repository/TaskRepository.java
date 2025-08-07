package com.flynaut.workforcemgmt.repository;

import com.flynaut.workforcemgmt.model.TaskManagement;
import com.flynaut.workforcemgmt.model.enums.TaskStatus;
import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    Optional<TaskManagement> findById(Long id);
    TaskManagement save(TaskManagement task);
    List<TaskManagement> findAll();
    List<TaskManagement> findByReferenceIdAndReferenceType(Long referenceId, com.flynaut.workforcemgmt.model.enums.ReferenceType referenceType);
    List<TaskManagement> findByAssigneeIdIn(List<Long> assigneeIds);
}
