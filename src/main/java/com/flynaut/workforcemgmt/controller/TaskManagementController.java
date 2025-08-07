package com.flynaut.workforcemgmt.controller;

import com.flynaut.workforcemgmt.dto.*;
import com.flynaut.workforcemgmt.model.enums.Priority;
import com.flynaut.workforcemgmt.model.response.Response;
import com.flynaut.workforcemgmt.service.TaskManagementService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/task-mgmt")
public class TaskManagementController {

    private final TaskManagementService taskManagementService;

    public TaskManagementController(TaskManagementService taskManagementService) {
        this.taskManagementService = taskManagementService;
    }

    @GetMapping("/{id}")
    public Response<TaskManagementDto> getTaskById(@PathVariable Long id) {
        return new Response<>(taskManagementService.findTaskById(id));
    }

    @PostMapping("/create")
    public Response<List<TaskManagementDto>> createTasks(@RequestBody TaskCreateRequest request) {
        return new Response<>(taskManagementService.createTasks(request));
    }

    @PostMapping("/update")
    public Response<List<TaskManagementDto>> updateTasks(@RequestBody UpdateTaskRequest request) {
        return new Response<>(taskManagementService.updateTasks(request));
    }

    @PostMapping("/assign-by-ref")
    public Response<String> assignByReference(@RequestBody AssignByReferenceRequest request) {
        return new Response<>(taskManagementService.assignByReference(request));
    }

    @PostMapping("/fetch-by-date/v2")
    public Response<List<TaskManagementDto>> fetchByDate(@RequestBody TaskFetchByDateRequest request) {
        return new Response<>(taskManagementService.fetchTasksByDate(request));
    }

    // ✅ Update Task Priority
    @PutMapping("/{taskId}/priority")
    public Response<TaskManagementDto> updatePriority(
            @PathVariable Long taskId,
            @RequestBody UpdatePriorityRequest request
    ) {
        return new Response<>(taskManagementService.updatePriority(taskId, request.getPriority()));
    }

    // ✅ Fetch All Tasks by Priority
    @GetMapping("/priority/{priority}")
    public Response<List<TaskManagementDto>> getTasksByPriority(@PathVariable Priority priority) {
        return new Response<>(taskManagementService.fetchTasksByPriority(priority));
    }

    @PostMapping("/addComment")
    public Response<String> addComment(@RequestBody CommentDto commentDto) {
        taskManagementService.addComment(commentDto.getTaskId(), commentDto);
        return new Response<>("Comment added successfully.");
    }

    @GetMapping("/{taskId}/history")
    public Response<List<Object>> getTaskHistory(@PathVariable Long taskId) {
        return new Response<>(taskManagementService.getTaskHistory(taskId));
    }
}