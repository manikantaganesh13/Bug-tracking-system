package com.bugtracker.controller;

import com.bugtracker.model.Bug;
import com.bugtracker.model.Comment;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private BugService bugService;

    @GetMapping("/bug/{bugId}")
    public List<Comment> getCommentsByBug(@PathVariable Long bugId) {
        return commentService.getCommentsByBugId(bugId);
    }

    @GetMapping("/my-comments")
    public List<Comment> getMyComments() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> user = userService.getUserByEmail(auth.getName());
        return user.map(value -> commentService.getCommentsByUserId(value.getId()))
                   .orElse(List.of());
    }

    @PostMapping
    public ResponseEntity<Comment> createComment(@RequestBody CommentRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Optional<User> user = userService.getUserByEmail(auth.getName());
            Optional<Bug> bug = bugService.getBugById(request.getBugId());

            if (user.isPresent() && bug.isPresent()) {
                Comment comment = commentService.createComment(
                    request.getCommentText(), bug.get(), user.get());
                return ResponseEntity.ok(comment);
            }
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Comment> updateComment(@PathVariable Long id, @RequestBody CommentRequest request) {
        try {
            Comment commentDetails = new Comment();
            commentDetails.setCommentText(request.getCommentText());
            
            Comment updatedComment = commentService.updateComment(id, commentDetails);
            return ResponseEntity.ok(updatedComment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        try {
            commentService.deleteComment(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    public static class CommentRequest {
        private String commentText;
        private Long bugId;

        public String getCommentText() { return commentText; }
        public void setCommentText(String commentText) { this.commentText = commentText; }
        public Long getBugId() { return bugId; }
        public void setBugId(Long bugId) { this.bugId = bugId; }
    }
}
