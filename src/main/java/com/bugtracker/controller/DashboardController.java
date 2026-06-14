package com.bugtracker.controller;

import com.bugtracker.model.User;
import com.bugtracker.service.DashboardService;
import com.bugtracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserService userService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/developer-stats")
    public ResponseEntity<Map<String, Object>> getDeveloperStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> user = userService.getUserByEmail(auth.getName());
        
        if (user.isPresent() && user.get().getRole() == User.Role.DEVELOPER) {
            Map<String, Object> stats = dashboardService.getDeveloperStats(user.get().getId());
            return ResponseEntity.ok(stats);
        }
        
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/developer-stats/{developerId}")
    public ResponseEntity<Map<String, Object>> getDeveloperStatsById(@PathVariable Long developerId) {
        Map<String, Object> stats = dashboardService.getDeveloperStats(developerId);
        return ResponseEntity.ok(stats);
    }
}
