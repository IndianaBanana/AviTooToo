package org.banana.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.banana.dto.comment.CommentMapper;
import org.banana.dto.comment.CommentRequestDto;
import org.banana.dto.comment.CommentResponseDto;
import org.banana.entity.Comment;
import org.banana.entity.User;
import org.banana.exception.CommentNotFoundException;
import org.banana.repository.CommentRepository;
import org.banana.repository.UserRepository;
import org.banana.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Banana on 25.04.2025
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public void printComments() {
        List<Comment> allRootComments = commentRepository.findAllRootComments();
        List<Comment> allCommentsInRootIds = commentRepository.findAllCommentsInRootIds(allRootComments.stream().map(Comment::getCommentId).toList());
        List<Comment> allComments = new ArrayList<>();
        allComments.addAll(allRootComments);
        allComments.addAll(allCommentsInRootIds);
        System.out.println(allComments);
    }

    @Override
    @Transactional
    public CommentResponseDto addComment(CommentRequestDto requestDto) {
        User currentUser = getCurrentUser();
        Comment comment = commentRepository.findById(requestDto.getParentCommentId())
                .orElseThrow(() -> new CommentNotFoundException(requestDto.getParentCommentId()));
        Comment save = commentRepository.save(new Comment(
                comment.getAdvertisementId(),
                comment.getRootCommentId(),
                comment.getParentCommentId(),
                requestDto.getCommentText(),
                currentUser
        ));
        return commentMapper.fromCommentToCommentResponseDto(save);
    }

    @Override
    public void deleteComment(UUID commentId) {

    }

    private User getCurrentUser() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getUser();
    }
}
