package com.bugtracker.controller;

import com.bugtracker.dto.BugDTO;
import com.bugtracker.dto.CommentDTO;
import com.bugtracker.model.Bug;
import com.bugtracker.model.BugStatus;
import com.bugtracker.model.Comment;
import com.bugtracker.model.Severity;
import com.bugtracker.model.User;
import com.bugtracker.service.BugService;
import com.bugtracker.service.CommentService;
import com.bugtracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PublicController {

    @Autowired
    private BugService bugService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Public controller is working!");
    }

    @GetMapping("/bugs-simple")
    public ResponseEntity<String> getBugsSimple() {
        try {
            List<Bug> bugs = bugService.getAllBugs();
            return ResponseEntity.ok("Found " + bugs.size() + " bugs");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/bugs")
    public List<BugDTO> getAllBugs() {
        List<Bug> bugs = bugService.getAllBugs();
        return bugs.stream()
                .sorted((b1, b2) -> Long.compare(b1.getId(), b2.getId()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private BugDTO convertToDTO(Bug bug) {
        BugDTO dto = new BugDTO();
        dto.setId(bug.getId());
        dto.setTitle(bug.getTitle());
        dto.setDescription(bug.getDescription());
        dto.setSeverity(bug.getSeverity());
        dto.setPriority(bug.getPriority());
        dto.setStatus(bug.getStatus());
        dto.setCreatedDate(bug.getCreatedDate());
        dto.setUpdatedDate(bug.getUpdatedDate());
        
        // Handle related entities safely
        if (bug.getProject() != null) {
            dto.setProjectName(bug.getProject().getName());
        }
        if (bug.getCreatedBy() != null) {
            dto.setCreatedByName(bug.getCreatedBy().getName());
        }
        if (bug.getAssignedTo() != null) {
            dto.setAssignedToName(bug.getAssignedTo().getName());
            dto.setAssignedToId(bug.getAssignedTo().getId());
        }
        
        return dto;
    }

    @GetMapping("/bugs/search")
    public List<BugDTO> searchBugs(
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
        
        List<Bug> bugs = bugService.searchBugs(title, statusEnum, severityEnum, projectId);
        return bugs.stream()
                .sorted((b1, b2) -> Long.compare(b1.getId(), b2.getId()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/bugs/{id}")
    public ResponseEntity<BugDTO> getBugById(@PathVariable Long id) {
        return bugService.getBugById(id)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/bugs/{id}")
    public ResponseEntity<BugDTO> updateBug(@PathVariable Long id, @RequestBody BugDTO bugDTO) {
        try {
            Optional<Bug> bugOpt = bugService.getBugById(id);
            if (bugOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Bug bug = bugOpt.get();
            
            // Update basic fields
            if (bugDTO.getTitle() != null) {
                bug.setTitle(bugDTO.getTitle());
            }
            if (bugDTO.getDescription() != null) {
                bug.setDescription(bugDTO.getDescription());
            }
            if (bugDTO.getSeverity() != null) {
                bug.setSeverity(bugDTO.getSeverity());
            }
            if (bugDTO.getPriority() != null) {
                bug.setPriority(bugDTO.getPriority());
            }
            if (bugDTO.getStatus() != null) {
                bug.setStatus(bugDTO.getStatus());
            }
            
            // Handle assignedTo update
            if (bugDTO.getAssignedToId() != null) {
                Optional<User> assignedUser = userService.getUserById(bugDTO.getAssignedToId());
                assignedUser.ifPresent(bug::setAssignedTo);
            }
            
            bug.setUpdatedDate(java.time.LocalDateTime.now());
            Bug updatedBug = bugService.updateBug(id, bug);
            return ResponseEntity.ok(convertToDTO(updatedBug));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/bugs/{id}/status")
    public ResponseEntity<?> updateBugStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            if (status != null) {
                Optional<Bug> bugOpt = bugService.getBugById(id);
                if (bugOpt.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                
                Bug bug = bugOpt.get();
                try {
                    BugStatus statusEnum = BugStatus.valueOf(status.toUpperCase());
                    bug.setStatus(statusEnum);
                    bug.setUpdatedDate(java.time.LocalDateTime.now());
                    Bug updatedBug = bugService.updateBug(id, bug);
                    return ResponseEntity.ok(convertToDTO(updatedBug));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body("Invalid status: " + status);
                }
            }
            return ResponseEntity.badRequest().body("Status is required");
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/comments/bug/{bugId}")
    public List<CommentDTO> getCommentsByBug(@PathVariable Long bugId) {
        System.out.println("DEBUG: Fetching comments for bugId: " + bugId);
        List<Comment> comments = commentService.getCommentsByBugId(bugId);
        System.out.println("DEBUG: Found " + comments.size() + " comments");
        
        List<CommentDTO> result = comments.stream()
                .map(comment -> {
                    System.out.println("DEBUG: Processing comment - ID: " + comment.getId() + 
                                     ", Text: " + comment.getCommentText() + 
                                     ", User: " + (comment.getUser() != null ? comment.getUser().getName() + " (Role: " + comment.getUser().getRole() + ")" : "null"));
                    return convertToCommentDTO(comment);
                })
                .collect(Collectors.toList());
        
        System.out.println("DEBUG: Returning " + result.size() + " comment DTOs");
        return result;
    }

    @PostMapping("/comments")
    public ResponseEntity<?> createComment(@RequestBody Map<String, Object> request) {
        try {
            String commentText = (String) request.get("commentText");
            Long bugId = Long.valueOf(request.get("bugId").toString());
            
            System.out.println("DEBUG: Creating comment with bugId: " + bugId + ", commentText: " + commentText);
            
            // Try to get authenticated user first
            User user = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("DEBUG: Authentication object: " + auth);
            if (auth != null && auth.getName() != null && !auth.getName().equals("anonymousUser")) {
                System.out.println("DEBUG: Trying to find user by email: " + auth.getName());
                Optional<User> authUser = userService.getUserByEmail(auth.getName());
                user = authUser.orElse(null);
                System.out.println("DEBUG: Found authenticated user: " + (user != null ? user.getName() + " (ID: " + user.getId() + ", Role: " + user.getRole() + ")" : "null"));
            }
            
            // If no authenticated user, try to use userId from request
            if (user == null && request.containsKey("userId")) {
                try {
                    Long userId = Long.valueOf(request.get("userId").toString());
                    System.out.println("DEBUG: Trying to find user by userId from request: " + userId);
                    Optional<User> userFromRequest = userService.getUserById(userId);
                    user = userFromRequest.orElse(null);
                    System.out.println("DEBUG: Found user from request: " + (user != null ? user.getName() + " (ID: " + user.getId() + ", Role: " + user.getRole() + ")" : "null"));
                } catch (Exception e) {
                    System.out.println("DEBUG: Invalid userId in request: " + e.getMessage());
                }
            }
            
            // If still no user, use the first available user as fallback (for public access)
            if (user == null) {
                System.out.println("DEBUG: Using fallback - first available user");
                Optional<User> userOpt = userService.getAllUsers().stream().findFirst();
                user = userOpt.orElse(null);
                System.out.println("DEBUG: Fallback user: " + (user != null ? user.getName() + " (ID: " + user.getId() + ", Role: " + user.getRole() + ")" : "null"));
            }
            
            Optional<Bug> bugOpt = bugService.getBugById(bugId);
            if (bugOpt.isEmpty() || user == null) {
                return ResponseEntity.badRequest().body("Invalid bug ID or user not found");
            }
            
            // Create comment manually (bypassing CommentService for public access)
            Comment comment = new Comment();
            comment.setCommentText(commentText);
            comment.setBug(bugOpt.get());
            comment.setUser(user);
            comment.setCreatedDate(java.time.LocalDateTime.now());
            
            // Save comment directly using repository
            Comment savedComment = commentService.createComment(comment);
            System.out.println("DEBUG: Saved comment with user: " + savedComment.getUser().getName() + " (Role: " + savedComment.getUser().getRole() + ")");
            return ResponseEntity.ok(convertToCommentDTO(savedComment));
        } catch (Exception e) {
            System.out.println("DEBUG: Error creating comment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to create comment: " + e.getMessage());
        }
    }

    private CommentDTO convertToCommentDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setCommentText(comment.getCommentText());
        dto.setUserName(comment.getUser() != null ? comment.getUser().getName() : "Unknown User");
        dto.setUserRole(comment.getUser() != null ? comment.getUser().getRole().toString() : "UNKNOWN");
        dto.setCreatedDate(comment.getCreatedDate());
        return dto;
    }

    @GetMapping("/test-debug")
    public ResponseEntity<Map<String, Object>> testDebug() {
        Map<String, Object> debug = new HashMap<>();
        
        // Test authentication
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        debug.put("authentication", auth != null ? auth.getName() : "null");
        debug.put("authDetails", auth != null ? auth.getDetails() : "null");
        
        // Test users
        List<User> users = userService.getAllUsers();
        debug.put("totalUsers", users.size());
        if (!users.isEmpty()) {
            User firstUser = users.get(0);
            debug.put("firstUser", Map.of(
                "id", firstUser.getId(),
                "name", firstUser.getName(),
                "email", firstUser.getEmail(),
                "role", firstUser.getRole().toString()
            ));
        }
        
        // Test recent comments
        List<Comment> recentComments = commentService.getAllComments().stream()
                .limit(5)
                .collect(Collectors.toList());
        debug.put("recentComments", recentComments.stream()
                .map(comment -> Map.of(
                    "id", comment.getId(),
                    "text", comment.getCommentText(),
                    "userName", comment.getUser() != null ? comment.getUser().getName() : "null",
                    "userRole", comment.getUser() != null ? comment.getUser().getRole().toString() : "null"
                ))
                .collect(Collectors.toList()));
        
        return ResponseEntity.ok(debug);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers().stream()
                    .sorted((u1, u2) -> Long.compare(u1.getId(), u2.getId()))
                    .collect(Collectors.toList());
            
            // Debug logging
            System.out.println("DEBUG: All users in database:");
            users.forEach(user -> System.out.println("  - ID: " + user.getId() + ", Name: " + user.getName() + ", Email: " + user.getEmail() + ", Role: " + user.getRole()));
            
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/developers")
    public ResponseEntity<List<User>> getDevelopers() {
        try {
            List<User> developers = userService.getAllUsers().stream()
                    .filter(user -> user.getRole() == User.Role.DEVELOPER)
                    .sorted((u1, u2) -> Long.compare(u1.getId(), u2.getId()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(developers);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
