package com.bugtracker.controller;

import com.bugtracker.model.Bug;
import com.bugtracker.model.BugStatus;
import com.bugtracker.model.Severity;
import com.bugtracker.model.Priority;
import com.bugtracker.model.Project;
import com.bugtracker.model.User;
import com.bugtracker.service.BugService;
import com.bugtracker.service.UserService;
import com.bugtracker.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bugs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BugController {

    @Autowired
    private BugService bugService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    // Public endpoint for testing without authentication
    @GetMapping("/public")
    public List<Bug> getAllBugsPublic() {
        return bugService.getAllBugs();
    }

    // Simple public endpoint that returns all bugs (for debugging)
    @GetMapping("/public-simple")
    public List<Bug> getAllBugsPublicSimple() {
        return bugService.getAllBugs();
    }

    // Test endpoint for bug creation
    @GetMapping("/test-create")
    public ResponseEntity<String> testCreateBug() {
        try {
            Bug testBug = new Bug();
            testBug.setTitle("Test Bug");
            testBug.setDescription("Test Description");
            testBug.setSeverity(Severity.LOW);
            testBug.setPriority(Priority.LOW);
            testBug.setStatus(BugStatus.OPEN);
            testBug.setCreatedDate(LocalDateTime.now());
            
            // Set a project (use first available project)
            try {
                Project project = projectService.getAllProjects().get(0);
                testBug.setProject(project);
            } catch (Exception e) {
                return ResponseEntity.status(500).body("No projects available. Please create projects first.");
            }
            
            // Set a creator (use first available user)
            try {
                User user = userService.getAllUsers().get(0);
                testBug.setCreatedBy(user);
            } catch (Exception e) {
                return ResponseEntity.status(500).body("No users available. Please create users first.");
            }
            
            Bug createdBug = bugService.createBug(testBug);
            return ResponseEntity.ok("Bug created successfully: " + createdBug.getTitle());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Test failed: " + e.getMessage());
        }
    }

    @PostMapping("/public")
    public ResponseEntity<?> createBugPublic(@RequestBody BugRequest request) {
        try {
            Bug bug = new Bug();
            bug.setTitle(request.getTitle());
            bug.setDescription(request.getDescription());
            
            // Convert string severity to enum
            if (request.getSeverity() != null && !request.getSeverity().trim().isEmpty()) {
                try {
                    bug.setSeverity(Severity.valueOf(request.getSeverity().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    bug.setSeverity(Severity.MEDIUM); // Default value
                }
            }
            
            // Convert string priority to enum
            if (request.getPriority() != null && !request.getPriority().trim().isEmpty()) {
                try {
                    bug.setPriority(Priority.valueOf(request.getPriority().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    bug.setPriority(Priority.MEDIUM); // Default value
                }
            }
            
            bug.setStatus(BugStatus.OPEN);
            bug.setCreatedDate(LocalDateTime.now());
            
            // Set project if projectId is provided
            if (request.getProjectId() != null) {
                Optional<Project> project = projectService.getProjectById(request.getProjectId());
                project.ifPresent(bug::setProject);
            }
            
            // Set assigned user if assignedTo is provided
            if (request.getAssignedTo() != null) {
                Optional<User> user = userService.getUserById(request.getAssignedTo());
                user.ifPresent(bug::setAssignedTo);
            }
            
            // Set reporter if reporterId is provided
            if (request.getReporterId() != null) {
                Optional<User> reporter = userService.getUserById(request.getReporterId());
                reporter.ifPresent(bug::setCreatedBy);
            }
            
            Bug createdBug = bugService.createBug(bug);
            return ResponseEntity.ok(createdBug);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Failed to create bug: " + e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'TESTER')")
    public List<Bug> getAllBugs() {
        return bugService.getAllBugs();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bug> getBugById(@PathVariable Long id) {
        return bugService.getBugById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/project/{projectId}")
    public List<Bug> getBugsByProject(@PathVariable Long projectId) {
        return bugService.getBugsByProject(projectId);
    }

    @GetMapping("/my-created")
    @PreAuthorize("hasAnyRole('TESTER', 'ADMIN')")
    public List<Bug> getMyCreatedBugs() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> user = userService.getUserByEmail(auth.getName());
        return user.map(value -> bugService.getBugsByCreatedBy(value))
                   .orElse(List.of());
    }

    @GetMapping("/my-assigned")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    public List<Bug> getMyAssignedBugs() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> user = userService.getUserByEmail(auth.getName());
        return user.map(value -> bugService.getBugsByAssignedTo(value))
                   .orElse(List.of());
    }

    @GetMapping("/search")
    public List<Bug> searchBugs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) BugStatus status,
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) Long projectId) {
        return bugService.searchBugs(title, status, severity, projectId);
    }

    @GetMapping("/search-public")
    public List<Bug> searchBugsPublic(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) Long projectId) {
        BugStatus statusEnum = null;
        Severity severityEnum = null;
        
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = BugStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid status
            }
        }
        
        if (severity != null && !severity.trim().isEmpty()) {
            try {
                severityEnum = Severity.valueOf(severity.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid severity
            }
        }
        
        return bugService.searchBugs(title, statusEnum, severityEnum, projectId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TESTER', 'ADMIN')")
    public ResponseEntity<Bug> createBug(@RequestBody Bug bug) {
        try {
            Bug createdBug = bugService.createBug(bug);
            return ResponseEntity.ok(createdBug);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    public ResponseEntity<Bug> updateBug(@PathVariable Long id, @RequestBody Bug bugDetails) {
        try {
            Bug updatedBug = bugService.updateBug(id, bugDetails);
            return ResponseEntity.ok(updatedBug);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<Bug> assignBug(@PathVariable Long id, @RequestBody AssignBugRequest request) {
        try {
            Optional<User> developer = userService.getUserById(request.getDeveloperId());
            if (developer.isPresent()) {
                Bug assignedBug = bugService.assignBug(id, developer.get());
                return ResponseEntity.ok(assignedBug);
            }
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<Bug> updateBugStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        try {
            Bug updatedBug = bugService.updateBugStatus(id, request.getStatus());
            return ResponseEntity.ok(updatedBug);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBug(@PathVariable Long id) {
        try {
            bugService.deleteBug(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    public static class AssignBugRequest {
        private Long developerId;
        public Long getDeveloperId() { return developerId; }
        public void setDeveloperId(Long developerId) { this.developerId = developerId; }
    }

    public static class UpdateStatusRequest {
        private BugStatus status;
        public BugStatus getStatus() { return status; }
        public void setStatus(BugStatus status) { this.status = status; }
    }

    public static class BugRequest {
        private String title;
        private String description;
        private String severity;
        private String priority;
        private Long projectId;
        private Long assignedTo;
        private Long reporterId;

        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }

        public Long getAssignedTo() { return assignedTo; }
        public void setAssignedTo(Long assignedTo) { this.assignedTo = assignedTo; }

        public Long getReporterId() { return reporterId; }
        public void setReporterId(Long reporterId) { this.reporterId = reporterId; }
    }
}
