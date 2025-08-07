package com.flynaut.workforcemgmt.service.impl;

import com.flynaut.workforcemgmt.common.exception.ResourceNotFoundException;
import com.flynaut.workforcemgmt.dto.*;
import com.flynaut.workforcemgmt.mapper.ITaskManagementMapper;
import com.flynaut.workforcemgmt.model.TaskManagement;
import com.flynaut.workforcemgmt.model.enums.Priority;
import com.flynaut.workforcemgmt.model.enums.Task;
import com.flynaut.workforcemgmt.model.enums.TaskStatus;
import com.flynaut.workforcemgmt.repository.TaskRepository;
import com.flynaut.workforcemgmt.service.TaskManagementService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskManagementServiceImpl implements TaskManagementService {

    private final TaskRepository taskRepository;
    private final ITaskManagementMapper taskMapper;
    private final Map<Long, List<CommentDto>> commentStore = new HashMap<>();
    private final Map<Long, List<ActivityLogDto>> activityStore = new HashMap<>();


    public TaskManagementServiceImpl(TaskRepository taskRepository, ITaskManagementMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    @Override
    public TaskManagementDto findTaskById(Long id) {
        TaskManagement task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return taskMapper.modelToDto(task);
    }

    @Override
    public List<TaskManagementDto> createTasks(TaskCreateRequest createRequest) {
        List<TaskManagement> createdTasks = new ArrayList<>();
        for (TaskCreateRequest.RequestItem item : createRequest.getRequests()) {
            TaskManagement newTask = new TaskManagement();
            newTask.setReferenceId(item.getReferenceId());
            newTask.setReferenceType(item.getReferenceType());
            newTask.setTask(item.getTask());
            newTask.setAssigneeId(item.getAssigneeId());
            newTask.setPriority(item.getPriority());
            newTask.setTaskDeadlineTime(item.getTaskDeadlineTime());
            newTask.setStatus(TaskStatus.ASSIGNED);
            newTask.setDescription("New task created.");
            createdTasks.add(taskRepository.save(newTask));
        }
        return taskMapper.modelListToDtoList(createdTasks);
    }

    @Override
    public List<TaskManagementDto> updateTasks(UpdateTaskRequest updateRequest) {
        List<TaskManagement> updatedTasks = new ArrayList<>();
        for (UpdateTaskRequest.RequestItem item : updateRequest.getRequests()) {
            TaskManagement task = taskRepository.findById(item.getTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + item.getTaskId()));

            if (item.getTaskStatus() != null) {
                task.setStatus(item.getTaskStatus());
            }
            if (item.getDescription() != null) {
                task.setDescription(item.getDescription());
            }
            updatedTasks.add(taskRepository.save(task));
        }
        return taskMapper.modelListToDtoList(updatedTasks);
    }

    @Override
    public String assignByReference(AssignByReferenceRequest request) {
        List<Task> applicableTasks = Task.getTasksByReferenceType(request.getReferenceType());
        List<TaskManagement> existingTasks = taskRepository.findByReferenceIdAndReferenceType(
                request.getReferenceId(), request.getReferenceType());

        for (Task taskType : applicableTasks) {
            Optional<TaskManagement> existingTaskOpt = existingTasks.stream()
                    .filter(t -> t.getTask() == taskType && t.getStatus() != TaskStatus.COMPLETED && t.getStatus() != TaskStatus.CANCELLED)
                    .findFirst();

            existingTaskOpt.ifPresent(taskToCancel -> {
                taskToCancel.setStatus(TaskStatus.CANCELLED);
                taskRepository.save(taskToCancel);
            });

            TaskManagement newTask = new TaskManagement();
            newTask.setReferenceId(request.getReferenceId());
            newTask.setReferenceType(request.getReferenceType());
            newTask.setTask(taskType);
            newTask.setAssigneeId(request.getAssigneeId());
            newTask.setStatus(TaskStatus.ASSIGNED);
            taskRepository.save(newTask);
        }
        return "Tasks assigned successfully for reference " + request.getReferenceId();
    }

    @Override
    public List<TaskManagementDto> fetchTasksByDate(TaskFetchByDateRequest request) {
        List<TaskManagement> tasks = taskRepository.findByAssigneeIdIn(request.getAssigneeIds());

        LocalDate startDate = Instant.ofEpochMilli(request.getStartDate())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate endDate = Instant.ofEpochMilli(request.getEndDate())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        List<TaskManagement> filteredTasks = tasks.stream()
                .filter(task -> {
                    if (task.getStatus() == TaskStatus.CANCELLED || task.getTaskDeadlineTime() == null) {
                        return false;
                    }

                    LocalDate taskDate = Instant.ofEpochMilli(task.getTaskDeadlineTime())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    boolean isWithinRange = !taskDate.isBefore(startDate) && !taskDate.isAfter(endDate);
                    boolean isOpenAndBefore =
                            (task.getStatus() == TaskStatus.ASSIGNED || task.getStatus() == TaskStatus.STARTED)
                                    && taskDate.isBefore(startDate);

                    return isWithinRange || isOpenAndBefore;
                })
                .collect(Collectors.toList());

        return taskMapper.modelListToDtoList(filteredTasks);
    }

    // ✅ New Feature 1: Update Task Priority
    @Override
    public TaskManagementDto updatePriority(Long taskId, Priority priority) {
        TaskManagement task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        task.setPriority(priority);
        TaskManagement updated = taskRepository.save(task);
        return taskMapper.modelToDto(updated);
    }

    // ✅ New Feature 2: Fetch Tasks by Priority
    @Override
    public List<TaskManagementDto> fetchTasksByPriority(Priority priority) {
        List<TaskManagement> allTasks = taskRepository.findAll();
        List<TaskManagement> filtered = allTasks.stream()
                .filter(task -> priority.equals(task.getPriority()))
                .collect(Collectors.toList());
        return taskMapper.modelListToDtoList(filtered);
    }

    @Override
    public void addComment(Long taskId, CommentDto commentDto) {
        commentDto.setTimestamp(Instant.now());
        commentDto.setTaskId(taskId);
        commentStore.computeIfAbsent(taskId, k -> new ArrayList<>()).add(commentDto);

        logActivity(taskId, "Comment added", commentDto.getCreatedBy());
    }

    @Override
    public List<Object> getTaskHistory(Long taskId) {
        List<CommentDto> comments = commentStore.getOrDefault(taskId, new ArrayList<>());
        List<ActivityLogDto> activities = activityStore.getOrDefault(taskId, new ArrayList<>());

        List<Object> fullHistory = new ArrayList<>();
        fullHistory.addAll(comments);
        fullHistory.addAll(activities);

        fullHistory.sort((a, b) -> {
            Instant timeA = (a instanceof CommentDto) ? ((CommentDto) a).getTimestamp() : ((ActivityLogDto) a).getTimestamp();
            Instant timeB = (b instanceof CommentDto) ? ((CommentDto) b).getTimestamp() : ((ActivityLogDto) b).getTimestamp();
            return timeA.compareTo(timeB);
        });

        return fullHistory;
    }


    private void logActivity(Long taskId, String message, String createdBy) {
        ActivityLogDto log = new ActivityLogDto();
        log.setMessage(message);
        log.setCreatedBy(createdBy);
        log.setTimestamp(Instant.now());

        activityStore.computeIfAbsent(taskId, k -> new ArrayList<>()).add(log);
    }

}
