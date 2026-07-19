package com.monitoring.controller;

import com.monitoring.entity.Comment;
import com.monitoring.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CommentController {

    private final CommentRepository commentRepository;

    @PostMapping
    public ResponseEntity<Comment> createComment(@RequestBody Comment comment) {
        Comment saved = commentRepository.save(comment);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{processInstanceId}")
    public ResponseEntity<List<Comment>> getCommentsByProcessInstance(@PathVariable Long processInstanceId) {
        List<Comment> comments = commentRepository.findByProcessInstanceIdOrderByCreatedAtAsc(processInstanceId);
        return ResponseEntity.ok(comments);
    }
}
