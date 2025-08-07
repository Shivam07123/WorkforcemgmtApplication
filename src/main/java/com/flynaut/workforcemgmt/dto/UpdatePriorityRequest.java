package com.flynaut.workforcemgmt.dto;

import com.flynaut.workforcemgmt.model.enums.Priority;
import lombok.Data;

@Data
public class UpdatePriorityRequest {
    private Priority priority;
}