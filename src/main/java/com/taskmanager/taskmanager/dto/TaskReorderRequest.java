package com.taskmanager.taskmanager.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TaskReorderRequest {

    private List<Long> orderedIds;
    private Integer maxPriority;
}
