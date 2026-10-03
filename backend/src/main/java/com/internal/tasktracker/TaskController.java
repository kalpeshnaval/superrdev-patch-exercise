package com.internal.tasktracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);
    private static final int MAX_PAGE_SIZE = 100;

    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping("/api/tasks")
    public ResponseEntity<?> searchTasks(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        // Normalize query input; escape LIKE wildcards so "%" or "_" are matched literally
        String query = q == null ? "" : q.trim();
        String searchTerm = "%" + escapeLike(query.toLowerCase()) + "%";

        // Parse status filter; unknown values are a client error, not a 500
        String normalizedStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                normalizedStatus = TaskStatus.valueOf(status.trim().toUpperCase()).name();
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid status '" + status + "'. Allowed: " + Arrays.toString(TaskStatus.values()));
            }
        }

        // Clamp paging input so bad values can't produce negative offsets or unbounded pages
        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);

        log.debug("searchTasks q=\"{}\" status={} page={} pageSize={}",
                query, normalizedStatus, safePage, safePageSize);

        Page<Task> results = taskRepository.searchTasks(searchTerm, normalizedStatus,
                PageRequest.of(safePage - 1, safePageSize));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", results.getContent());
        response.put("total", results.getTotalElements());
        response.put("page", safePage);
        response.put("pageSize", safePageSize);

        return ResponseEntity.ok(response);
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
