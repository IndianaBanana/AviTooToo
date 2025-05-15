package org.banana.service;

import org.banana.dto.comment.CommentMapper;
import org.banana.dto.comment.CommentRequestDto;
import org.banana.dto.comment.CommentResponseDto;
import org.banana.dto.user.UserCommenterResponseDto;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private AdvertisementRepository advertisementRepository;

    @InjectMocks
    private CommentServiceImpl service;

    private UUID adId;
    private UUID parentId;
    private UUID rootId;
    private UUID currentUserId;
    private UserPrincipal principal;
    private User user;
    private Comment parentComment;

    @BeforeEach
    void init() {
        adId = UUID.randomUUID();
        parentId = UUID.randomUUID();
        rootId = UUID.randomUUID();
        currentUserId = UUID.randomUUID();

        user = new User(currentUserId, "fn", "ln", "phone", "user", "pass", UserRole.ROLE_USER);

        parentComment = new Comment(adId, null, null, "text", user, LocalDateTime.now());
        parentComment.setId(parentId);
    }

    @BeforeEach
    void setupSecurityContext() {
        principal = new UserPrincipal(currentUserId, "fn", "ln", "phone", "user", "pass", UserRole.ROLE_USER);
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void reset() {
        SecurityContextHolder.clearContext();
    }


    @Test
    void addComment_whenParentNotFound_thenShouldThrowCommentNotFoundException() {
        CommentRequestDto dto = new CommentRequestDto(adId, parentId, "txt");
        when(commentRepository.findById(parentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addComment(dto))
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessageContaining(parentId.toString());
    }

    @Test
    void addComment_whenParentCommenterIsNull_thenShouldThrowAddingCommentWhenParentCommenterIsNullException() {
        CommentRequestDto dto = new CommentRequestDto(adId, parentId, "txt");
        Comment noUser = new Comment(adId, null, null, "t", null, LocalDateTime.now());
        noUser.setId(parentId);
        when(commentRepository.findById(parentId)).thenReturn(Optional.of(noUser));

        assertThatThrownBy(() -> service.addComment(dto))
                .isInstanceOf(AddingCommentWhenParentCommenterIsNullException.class);
    }

    @Test
    void addComment_whenParentAdvertisementMismatch_thenShouldThrowCommentInAdvertisementNotFoundException() {
        CommentRequestDto dto = new CommentRequestDto(adId, parentId, "txt");
        User other = new User(UUID.randomUUID(), "fn", "ln", "p", "u", "pw", UserRole.ROLE_USER);
        Comment mismatch = new Comment(UUID.randomUUID(), null, null, "t", other, LocalDateTime.now());
        mismatch.setId(parentId);
        when(commentRepository.findById(parentId)).thenReturn(Optional.of(mismatch));

        assertThatThrownBy(() -> service.addComment(dto))
                .isInstanceOf(CommentInAdvertisementNotFoundException.class);
    }

    @Test
    void addComment_whenUserNotFound_thenShouldThrowUserNotFoundException() {
        CommentRequestDto dto = new CommentRequestDto();
        when(userRepository.findById(currentUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addComment(dto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(currentUserId.toString());
    }

    @Test
    void addComment_whenValidAndRootCommentIsNotNull_thenReturnsDto() {
        CommentRequestDto dto = new CommentRequestDto(adId, parentId, "txt");
        parentComment.setRootCommentId(rootId);
        parentComment.setParentCommentId(rootId);
        when(commentRepository.findById(parentId)).thenReturn(Optional.of(parentComment));
        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(user));

        Comment toSave = new Comment(adId, parentId, rootId, "txt", user, LocalDateTime.now());
        when(commentRepository.save(any())).thenReturn(toSave);
        CommentResponseDto resp = new CommentResponseDto();
        when(commentMapper.fromCommentToCommentResponseDto(toSave)).thenReturn(resp);
        ArgumentCaptor<Comment> commentArgumentCaptor = ArgumentCaptor.forClass(Comment.class);
        CommentResponseDto result = service.addComment(dto);
        verify(commentRepository).save(commentArgumentCaptor.capture());
        Comment saved = commentArgumentCaptor.getValue();
        assertAll(
                () -> assertThat(saved.getAdvertisementId()).isEqualTo(adId),
                () -> assertThat(saved.getParentCommentId()).isEqualTo(parentId),
                () -> assertThat(saved.getRootCommentId()).isEqualTo(rootId),
                () -> assertThat(saved.getCommentText()).isEqualTo("txt"),
                () -> assertThat(saved.getCommenter()).isEqualTo(user)
        );
        assertThat(result).isEqualTo(resp);
    }

    @Test
    void addComment_whenValidAndRootCommentIsNull_thenReturnsDto() {
        CommentRequestDto dto = new CommentRequestDto(adId, parentId, "txt");
        when(commentRepository.findById(parentId)).thenReturn(Optional.of(parentComment));
        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(user));

        Comment toSave = new Comment(adId, parentId, parentId, "txt", user, LocalDateTime.now());
        when(commentRepository.save(any())).thenReturn(toSave);
        CommentResponseDto resp = new CommentResponseDto();
        when(commentMapper.fromCommentToCommentResponseDto(toSave)).thenReturn(resp);
        ArgumentCaptor<Comment> commentArgumentCaptor = ArgumentCaptor.forClass(Comment.class);
        CommentResponseDto result = service.addComment(dto);
        verify(commentRepository).save(commentArgumentCaptor.capture());
        Comment saved = commentArgumentCaptor.getValue();
        assertAll(
                () -> assertThat(saved.getAdvertisementId()).isEqualTo(adId),
                () -> assertThat(saved.getParentCommentId()).isEqualTo(parentId),
                () -> assertThat(saved.getRootCommentId()).isEqualTo(parentId),
                () -> assertThat(saved.getCommentText()).isEqualTo("txt"),
                () -> assertThat(saved.getCommenter()).isEqualTo(user)
        );
        assertThat(result).isEqualTo(resp);
    }


    @Test
    void deleteComment_whenCommentNotFound_thenShouldThrowCommentNotFoundException() {
        UUID id = UUID.randomUUID();
        when(commentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteComment(id))
                .isInstanceOf(CommentNotFoundException.class);
    }

    @Test
    void deleteComment_whenCommenterNull_thenShouldThrowUserDeleteCommentException() {
        UUID id = UUID.randomUUID();
        Comment c = new Comment(adId, null, null, "t", null, LocalDateTime.now());
        c.setId(id);
        when(commentRepository.findById(id)).thenReturn(Optional.of(c));

        assertThatThrownBy(() -> service.deleteComment(id))
                .isInstanceOf(UserDeleteCommentException.class);
    }

    @Test
    void deleteComment_whenNotOwnerOrAdmin_thenShouldThrowUserDeleteCommentException() {
        UUID id = UUID.randomUUID();
        User other = new User(UUID.randomUUID(), "fn", "ln", "p", "u", "pw", UserRole.ROLE_USER);
        Comment c = new Comment(adId, null, null, "t", other, LocalDateTime.now());
        c.setId(id);
        when(commentRepository.findById(id)).thenReturn(Optional.of(c));

        assertThatThrownBy(() -> service.deleteComment(id))
                .isInstanceOf(UserDeleteCommentException.class);
    }

    @Test
    void deleteComment_whenOwner_thenDeletes() {
        UUID id = UUID.randomUUID();
        Comment c = new Comment(adId, null, null, "t", user, LocalDateTime.now());
        c.setId(id);
        when(commentRepository.findById(id)).thenReturn(Optional.of(c));

        service.deleteComment(id);

        assertThat(c.getCommenter()).isNull();
        assertThat(c.getCommentText()).isEqualTo("Comment deleted");
        verify(commentRepository).save(c);
    }

    @Test
    void deleteComment_whenAdmin_thenDeletes() {
        UUID id = UUID.randomUUID();
        user.setId(UUID.randomUUID());
        Comment c = new Comment(adId, null, null, "t", user, LocalDateTime.now());
        c.setId(id);
        principal.setRole(UserRole.ROLE_ADMIN);
        when(commentRepository.findById(id)).thenReturn(Optional.of(c));

        service.deleteComment(id);

        assertThat(c.getCommenter()).isNull();
        assertThat(c.getCommentText()).isEqualTo("Comment deleted");
        verify(commentRepository).save(c);
    }


    @Test
    void findCommentById_whenCommentNotFound_thenShouldThrowCommentNotFoundException() {
        UUID id = UUID.randomUUID();
        when(commentRepository.findFetchedById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findCommentById(id))
                .isInstanceOf(CommentNotFoundException.class);
    }

    @Test
    void findCommentById_whenCommentFound_thenReturnsDto() {
        UUID id = UUID.randomUUID();
        Comment c = new Comment(adId, null, null, "t", user, LocalDateTime.now());
        c.setId(id);
        when(commentRepository.findFetchedById(id)).thenReturn(Optional.of(c));
        CommentResponseDto dto = new CommentResponseDto();
        when(commentMapper.fromCommentToCommentResponseDto(c)).thenReturn(dto);

        CommentResponseDto result = service.findCommentById(id);
        assertThat(result).isSameAs(dto);
    }


    @Test
    void findAllByAdvertisementId_whenValid_thenReturnsListOfDto() {
        int page = 0, size = 2;
        CommentResponseDto dtoWithRootNull = new CommentResponseDto(
                rootId,
                adId,
                new UserCommenterResponseDto(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName()
                ),
                null,
                null,
                "t2",
                LocalDateTime.now()
        );
        CommentResponseDto dtoWithRootNotNull = new CommentResponseDto(
                UUID.randomUUID(),
                adId,
                new UserCommenterResponseDto(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName()
                ),
                rootId,
                rootId,
                "t2",
                LocalDateTime.now()
        );
        when(advertisementRepository.existsById(adId)).thenReturn(true);
        when(commentRepository.findAllRootCommentsByAdvertisementId(adId, 0, 2))
                .thenReturn(List.of(dtoWithRootNull));
        when(commentRepository.findAllCommentsInRootIds(List.of(dtoWithRootNull.getId())))
                .thenReturn(List.of(dtoWithRootNotNull));
        List<CommentResponseDto> dtos = List.of(dtoWithRootNull, dtoWithRootNotNull);

        List<CommentResponseDto> result = service.findAllByAdvertisementId(adId, page, size);
        assertThat(result).isEqualTo(dtos);
    }

    @Test
    void findAllByAdvertisementId_whenAdvertisementDoesNotExist_callsRepoAndMaps() {
        int page = 0, size = 2;
        when(advertisementRepository.existsById(adId)).thenReturn(false);

        assertThrows(AdvertisementNotFoundException.class, () -> service.findAllByAdvertisementId(adId, page, size));
    }

    private void mockCurrentUser(UUID id, UserRole role) {
        principal = new UserPrincipal(id, "First", "Last", "123", "user", "pass", role);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
