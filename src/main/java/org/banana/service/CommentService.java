package org.banana.service;

import org.banana.dto.comment.CommentRequestDto;
import org.banana.dto.comment.CommentResponseDto;

import java.util.List;
import java.util.UUID;

/**
 * Created by Banana on 27.04.2025
 */
public interface CommentService {

    CommentResponseDto addComment(CommentRequestDto requestDto);

    void deleteComment(UUID commentId);

    CommentResponseDto findCommentById(UUID commentId);

    List<CommentResponseDto> findAllByAdvertisementId(UUID advertisementId, int page, int size);
}
