package org.banana.service;

import org.banana.dto.comment.CommentRequestDto;
import org.banana.dto.comment.CommentResponseDto;

import java.util.UUID;

/**
 * Created by Banana on 27.04.2025
 */
public interface CommentService {

    // todo: удалить тестовую штуку
    void printComments();

    CommentResponseDto addComment(CommentRequestDto requestDto);

    void deleteComment(UUID commentId);

}
