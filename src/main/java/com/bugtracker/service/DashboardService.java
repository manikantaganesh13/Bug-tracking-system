package com.bugtracker.service;

import com.bugtracker.model.BugStatus;
import com.bugtracker.model.Severity;
import com.bugtracker.repository.BugRepository;
import com.bugtracker.repository.ProjectRepository;
import com.bugtracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private BugRepository bugRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Total counts
        stats.put("totalBugs", bugRepository.count());
        stats.put("totalProjects", projectRepository.count());
        stats.put("totalUsers", userRepository.count());

        // Bug status counts
        stats.put("openBugs", bugRepository.countByStatus(BugStatus.OPEN));
        stats.put("inProgressBugs", bugRepository.countByStatus(BugStatus.IN_PROGRESS));
        stats.put("resolvedBugs", bugRepository.countByStatus(BugStatus.RESOLVED));
        stats.put("closedBugs", bugRepository.countByStatus(BugStatus.CLOSED));

        // Bug severity counts
        stats.put("criticalBugs", bugRepository.countBySeverity(Severity.CRITICAL));
        stats.put("highBugs", bugRepository.countBySeverity(Severity.HIGH));
        stats.put("mediumBugs", bugRepository.countBySeverity(Severity.MEDIUM));
        stats.put("lowBugs", bugRepository.countBySeverity(Severity.LOW));

        return stats;
    }

    public Map<String, Object> getDeveloperStats(Long developerId) {
        Map<String, Object> stats = new HashMap<>();

        stats.put("assignedBugs", bugRepository.countByDeveloper(developerId));
        stats.put("openBugs", bugRepository.findByDeveloperAndStatuses(
            developerId, 
            java.util.List.of(BugStatus.OPEN, BugStatus.ASSIGNED)
        ).size());
        stats.put("inProgressBugs", bugRepository.findByDeveloperAndStatuses(
            developerId, 
            java.util.List.of(BugStatus.IN_PROGRESS)
        ).size());
        stats.put("resolvedBugs", bugRepository.findByDeveloperAndStatuses(
            developerId, 
            java.util.List.of(BugStatus.RESOLVED, BugStatus.CLOSED)
        ).size());

        return stats;
    }
}
