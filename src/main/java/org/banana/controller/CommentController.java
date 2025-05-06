package org.banana.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.banana.dto.comment.CommentRequestDto;
import org.banana.dto.comment.CommentResponseDto;
import org.banana.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponseDto> addComment(@Valid @RequestBody CommentRequestDto requestDto) {
        CommentResponseDto createdComment = commentService.addComment(requestDto);
        return ResponseEntity.created(URI.create("api/v1/comment/"+createdComment.getId())).body(createdComment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponseDto> findCommentById(@PathVariable UUID commentId) {
        return ResponseEntity.ok(commentService.findCommentById(commentId));
    }

    @GetMapping("/advertisement/{advertisementId}")
    public ResponseEntity<List<CommentResponseDto>> findAllByAdvertisementId(@PathVariable UUID advertisementId,
                                                                             @RequestParam(defaultValue = "0") @Min(0) int page,
                                                                             @RequestParam(defaultValue = "10") @Min(1) int size) {
        return ResponseEntity.ok(commentService.findAllByAdvertisementId(advertisementId, page, size));
    }
}
