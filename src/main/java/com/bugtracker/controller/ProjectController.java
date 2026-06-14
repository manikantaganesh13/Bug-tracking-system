package com.bugtracker.controller;

import com.bugtracker.model.Project;
import com.bugtracker.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @GetMapping
    public List<Project> getAllProjects() {
        return projectService.getAllProjects().stream()
                .sorted((p1, p2) -> Long.compare(p1.getId(), p2.getId()))
                .collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/create-samples")
    public ResponseEntity<String> createSampleProjects() {
        try {
            // Create sample projects
            Project project1 = new Project();
            project1.setName("Web Application");
            project1.setDescription("Main web application project");
            projectService.createProject(project1);

            Project project2 = new Project();
            project2.setName("Mobile App");
            project2.setDescription("Mobile application development");
            projectService.createProject(project2);

            Project project3 = new Project();
            project3.setName("API Services");
            project3.setDescription("Backend API services");
            projectService.createProject(project3);

            return ResponseEntity.ok("Sample projects created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to create sample projects: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        return projectService.getProjectById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        try {
            Project createdProject = projectService.createProject(project);
            return ResponseEntity.ok(createdProject);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @RequestBody Project projectDetails) {
        try {
            Project updatedProject = projectService.updateProject(id, projectDetails);
            return ResponseEntity.ok(updatedProject);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        try {
            projectService.deleteProject(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/bug-count")
    public ResponseEntity<Long> getBugCountForProject(@PathVariable Long id) {
        try {
            long bugCount = projectService.getBugCountForProject(id);
            return ResponseEntity.ok(bugCount);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
