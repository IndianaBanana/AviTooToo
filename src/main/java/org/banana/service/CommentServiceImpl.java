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
        log.info("addComment({}) in {}", requestDto, getClass().getSimpleName());
        UUID currentUserId = SecurityUtils.getCurrentUserPrincipal().getId();
        UUID parentCommentId = requestDto.getParentCommentId();
        UUID advertisementId = requestDto.getAdvertisementId();
        UUID rootCommentId = null;

        if (parentCommentId != null) {
            Comment parent = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new CommentNotFoundException(parentCommentId));

            if (parent.getCommenter() == null)
                throw new AddingCommentWhenParentCommenterIsNullException();

            if (!parent.getAdvertisementId().equals(advertisementId))
                throw new CommentInAdvertisementNotFoundException(parentCommentId, advertisementId);

            rootCommentId = parent.getRootCommentId();

            if (rootCommentId == null) rootCommentId = parentCommentId;
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(currentUserId));

        Comment comment = new Comment(
                advertisementId,
                rootCommentId,
                parentCommentId,
                requestDto.getCommentText(),
                currentUser,
                LocalDateTime.now()
        );

        comment = commentRepository.save(comment);

        log.info("comment added: {}", comment);
        return commentMapper.fromCommentToCommentResponseDto(comment);
    }

    /**
     * Физического удаления не происходит. По аналогии с хабром или ютубом,
     * мы просто обнуляем комментатора и заменяем текст комментария на "Comment deleted"
     */
    @Override
    @Transactional
    public void deleteComment(UUID commentId) {
        log.info("deleteComment({}) in {}", commentId, getClass().getSimpleName());
        UserPrincipal currentUser = SecurityUtils.getCurrentUserPrincipal();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (comment.getCommenter() == null) throw new UserDeleteCommentException();

        boolean isOwner = comment.getCommenter().getId().equals(currentUser.getId());
        boolean isAdmin = UserRole.ROLE_ADMIN.equals(currentUser.getRole());
        if (!isOwner && !isAdmin) throw new UserDeleteCommentException();

        comment.setCommenter(null);
        comment.setCommentText("Comment deleted");

        log.info("comment deleted: {}", comment);
        commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponseDto findCommentById(UUID commentId) {
        log.info("findCommentById({}) in {}", commentId, getClass().getSimpleName());

        Comment comment = commentRepository.findFetchedById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        return commentMapper.fromCommentToCommentResponseDto(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDto> findAllByAdvertisementId(UUID advertisementId, int page, int size) {
        log.info("findAllByAdvertisementId({}) in {}", advertisementId, getClass().getSimpleName());

        if (!advertisementRepository.existsById(advertisementId))
            throw new AdvertisementNotFoundException(advertisementId);

        // отберем заданное кол-во комментариев у которых нет родителей (рутовые)
        List<CommentResponseDto> allRootComments = commentRepository
                .findAllRootCommentsByAdvertisementId(advertisementId, page * size, size);
        // найдем всех детей рутовых комментариев
        List<CommentResponseDto> allCommentsInRootIds = commentRepository
                .findAllCommentsInRootIds(allRootComments.stream().map(CommentResponseDto::getId).toList());
        // Объединяем родителей и детей в плоский список. задача построения иерархии комментариев лежит на фронтенде,
        // чтобы снизить нагрузку на сервер
        List<CommentResponseDto> allComments = new ArrayList<>();
        allComments.addAll(allRootComments);
        allComments.addAll(allCommentsInRootIds);

        log.info("all comments: {}", allComments);
        return allComments;
    }
}
