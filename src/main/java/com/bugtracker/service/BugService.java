package com.bugtracker.service;

import com.bugtracker.model.Bug;
import com.bugtracker.model.BugStatus;
import com.bugtracker.model.Severity;
import com.bugtracker.model.Project;
import com.bugtracker.model.User;
import com.bugtracker.repository.BugRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BugService {

    @Autowired
    private BugRepository bugRepository;

    @Autowired
    private UserService userService;

    public List<Bug> getAllBugs() {
        return bugRepository.findAll();
    }

    public Optional<Bug> getBugById(Long id) {
        return bugRepository.findById(id);
    }

    public List<Bug> getBugsByProject(Long projectId) {
        return bugRepository.findByProjectId(projectId);
    }

    public List<Bug> getBugsByCreatedBy(User createdBy) {
        return bugRepository.findByCreatedBy(createdBy);
    }

    public List<Bug> getBugsByAssignedTo(User assignedTo) {
        return bugRepository.findByAssignedTo(assignedTo);
    }

    public List<Bug> getBugsByStatus(BugStatus status) {
        return bugRepository.findByStatus(status);
    }

    public List<Bug> getBugsBySeverity(Severity severity) {
        return bugRepository.findBySeverity(severity);
    }

    public List<Bug> searchBugs(String title, BugStatus status, Severity severity, Long projectId) {
        return bugRepository.searchBugs(title, status, severity, projectId);
    }

    public Bug createBug(Bug bug) {
        try {
            // Validate required fields
            if (bug.getTitle() == null || bug.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Title is required");
            }
            if (bug.getDescription() == null || bug.getDescription().trim().isEmpty()) {
                throw new IllegalArgumentException("Description is required");
            }
            if (bug.getSeverity() == null) {
                throw new IllegalArgumentException("Severity is required");
            }
            
            // Set created by from authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                Optional<User> user = userService.getUserByEmail(auth.getName());
                user.ifPresent(u -> bug.setCreatedBy(u));
            }
            
            return bugRepository.save(bug);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bug: " + e.getMessage());
        }
    }

    public Bug updateBug(Long id, Bug bugDetails) {
        Bug bug = bugRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bug not found"));

        bug.setTitle(bugDetails.getTitle());
        bug.setDescription(bugDetails.getDescription());
        bug.setSeverity(bugDetails.getSeverity());
        bug.setPriority(bugDetails.getPriority());
        bug.setStatus(bugDetails.getStatus());
        bug.setAssignedTo(bugDetails.getAssignedTo());

        return bugRepository.save(bug);
    }

    public Bug assignBug(Long bugId, User developer) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new RuntimeException("Bug not found"));

        bug.setAssignedTo(developer);
        if (bug.getStatus() == BugStatus.OPEN) {
            bug.setStatus(BugStatus.ASSIGNED);
        }

        return bugRepository.save(bug);
    }

    public Bug updateBugStatus(Long bugId, BugStatus status) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new RuntimeException("Bug not found"));

        bug.setStatus(status);
        return bugRepository.save(bug);
    }

    public void deleteBug(Long id) {
        Bug bug = bugRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bug not found"));
        bugRepository.delete(bug);
    }

    public long countBugsByStatus(BugStatus status) {
        return bugRepository.countByStatus(status);
    }

    public long countBugsBySeverity(Severity severity) {
        return bugRepository.countBySeverity(severity);
    }

    public long countBugsByDeveloper(Long developerId) {
        return bugRepository.countByDeveloper(developerId);
    }

    public List<Bug> getBugsForDeveloperByStatuses(Long developerId, List<BugStatus> statuses) {
        return bugRepository.findByDeveloperAndStatuses(developerId, statuses);
    }
}
