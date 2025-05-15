package org.banana.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.banana.dto.comment.CommentMapper;
import org.banana.dto.comment.CommentRequestDto;
import org.banana.dto.comment.CommentResponseDto;
import org.banana.entity.Comment;
import org.banana.entity.User;
import org.banana.exception.AddingCommentWhenParentCommenterIsNullException;
import org.banana.exception.AdvertisementNotFoundException;
import org.banana.exception.CommentInAdvertisementNotFoundException;
import org.banana.exception.CommentNotFoundException;
import org.banana.exception.UserDeleteCommentException;
import org.banana.exception.UserNotFoundException;
import org.banana.repository.AdvertisementRepository;
import org.banana.repository.CommentRepository;
import org.banana.repository.UserRepository;
import org.banana.security.UserRole;
import org.banana.security.dto.UserPrincipal;
import org.banana.util.SecurityUtils;
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
    private final AdvertisementRepository advertisementRepository;


    @Override
    @Transactional
    public CommentResponseDto addComment(CommentRequestDto requestDto) {
        UserPrincipal currentUserPrincipal = SecurityUtils.getCurrentUserPrincipal();
        UUID rootCommentId = null;
        if (requestDto.getParentCommentId() != null) {
            Comment parent = commentRepository.findById(requestDto.getParentCommentId())
                    .orElseThrow(() -> new CommentNotFoundException(requestDto.getParentCommentId()));
            if (parent.getCommenter() == null) throw new AddingCommentWhenParentCommenterIsNullException();
            if (!parent.getAdvertisementId().equals(requestDto.getAdvertisementId()))
                throw new CommentInAdvertisementNotFoundException(requestDto.getParentCommentId(), requestDto.getAdvertisementId());

            rootCommentId = parent.getRootCommentId();
            if (rootCommentId == null) {
                rootCommentId = requestDto.getParentCommentId();
            }
        }
        User currentUser = userRepository.findById(currentUserPrincipal.getId())
                .orElseThrow(() -> new UserNotFoundException(currentUserPrincipal.getId()));
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
        UserPrincipal currentUser = SecurityUtils.getCurrentUserPrincipal();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (comment.getCommenter() == null) throw new UserDeleteCommentException();

        boolean isOwner = comment.getCommenter().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole().equals(UserRole.ROLE_ADMIN);
        if (!isOwner && !isAdmin) throw new UserDeleteCommentException();

        comment.setCommenter(null);
        comment.setCommentText("Comment deleted");
        commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponseDto findCommentById(UUID commentId) {
        Comment comment = commentRepository.findFetchedById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        return commentMapper.fromCommentToCommentResponseDto(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDto> findAllByAdvertisementId(UUID advertisementId, int page, int size) {
        if (!advertisementRepository.existsById(advertisementId))
            throw new AdvertisementNotFoundException(advertisementId);

        List<CommentResponseDto> allRootComments = commentRepository
                .findAllRootCommentsByAdvertisementId(advertisementId, page * size, size);

        List<CommentResponseDto> allCommentsInRootIds = commentRepository
                .findAllCommentsInRootIds(allRootComments.stream().map(CommentResponseDto::getId).toList());

        List<CommentResponseDto> allComments = new ArrayList<>();
        allComments.addAll(allRootComments);
        allComments.addAll(allCommentsInRootIds);
        return allComments;
    }
}
