package org.banana.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.banana.dto.comment.CommentMapper;
import org.banana.dto.comment.CommentRequestDto;
import org.banana.dto.comment.CommentResponseDto;
import org.banana.entity.Comment;
import org.banana.entity.User;
import org.banana.exception.CommentInAdvertisementNotFoundException;
import org.banana.exception.CommentNotFoundException;
import org.banana.exception.UserDeleteCommentException;
import org.banana.repository.CommentRepository;
import org.banana.repository.UserRepository;
import org.banana.security.UserPrincipal;
import org.banana.security.UserRole;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    public CommentResponseDto addComment(CommentRequestDto requestDto) {
        User currentUser = getCurrentUser();
        UUID rootCommentId = null;
        if (requestDto.getParentCommentId() != null) {
            Comment parent = commentRepository.findById(requestDto.getParentCommentId())
                    .orElseThrow(() -> new CommentNotFoundException(requestDto.getParentCommentId()));
            if (!parent.getAdvertisementId().equals(requestDto.getAdvertisementId()))
                throw new CommentInAdvertisementNotFoundException(requestDto.getParentCommentId(), requestDto.getAdvertisementId());

            rootCommentId = parent.getRootCommentId();
            if (rootCommentId == null) {
                rootCommentId = requestDto.getParentCommentId();
            }
        }
        Comment comment = new Comment(
                requestDto.getAdvertisementId(),
                rootCommentId,
                requestDto.getParentCommentId(),
                requestDto.getCommentText(),
                currentUser,
                LocalDateTime.now()
        );
        comment = commentRepository.save(comment);
        return commentMapper.fromCommentToCommentResponseDto(comment);
    }

    @Override
    @Transactional
    public void deleteComment(UUID commentId) {
        User currentUser = getCurrentUser();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        if (!comment.getCommenter().getId().equals(currentUser.getId()) && !currentUser.getRole().equals(UserRole.ROLE_ADMIN)) {
            throw new UserDeleteCommentException();
        }
        comment.setCommenter(null);
        comment.setCommentText("Comment deleted");
        commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponseDto findCommentById(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        return commentMapper.fromCommentToCommentResponseDto(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDto> findAllByAdvertisementId(UUID advertisementId, int page, int size) {
        List<Comment> allRootComments = commentRepository.findAllRootCommentsByAdvertisementId(advertisementId, page * size, size);
        List<Comment> allCommentsInRootIds = commentRepository.findAllCommentsInRootIds(allRootComments.stream().map(Comment::getId).toList());
        List<Comment> allComments = new ArrayList<>();
        allComments.addAll(allRootComments);
        allComments.addAll(allCommentsInRootIds);
        return commentMapper.fromCommentListToCommentResponseDtoList(allComments);
    }


    private User getCurrentUser() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getUser();
    }
}
