package org.banana.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.banana.config.SecurityConfig;
import org.banana.dto.comment.CommentRequestDto;
import org.banana.dto.comment.CommentResponseDto;
import org.banana.exception.AddingCommentWhenParentCommenterIsNullException;
import org.banana.exception.AdvertisementNotFoundException;
import org.banana.exception.CommentNotFoundException;
import org.banana.exception.UserDeleteCommentException;
import org.banana.security.service.JwtService;
import org.banana.service.CommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@Import(SecurityConfig.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    // --- addComment ---

    @Test
    @WithMockUser
    void addComment_whenValid_thenCreated() throws Exception {
        CommentRequestDto req = new CommentRequestDto();
        req.setAdvertisementId(UUID.randomUUID());
        req.setCommentText("Nice ad");

        CommentResponseDto resp = new CommentResponseDto();
        resp.setId(UUID.randomUUID());
        when(commentService.addComment(req)).thenReturn(resp);

        mvc.perform(post("/api/v1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/comment/" + resp.getId())))
                .andExpect(content().json(objectMapper.writeValueAsString(resp)));
    }

    @Test
    @WithMockUser
    void addComment_whenInvalidDto_thenBadRequest() throws Exception {
        CommentRequestDto req = new CommentRequestDto();

        mvc.perform(post("/api/v1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("advertisementId")))
                .andExpect(content().string(containsString("commentText")));
    }

    @Test
    @WithMockUser
    void addComment_whenParentNotFound_thenNotFound() throws Exception {
        CommentRequestDto req = new CommentRequestDto();
        req.setAdvertisementId(UUID.randomUUID());
        req.setParentCommentId(UUID.randomUUID());
        req.setCommentText("Reply");

        when(commentService.addComment(req)).thenThrow(new CommentNotFoundException(req.getParentCommentId()));

        mvc.perform(post("/api/v1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void addComment_whenAdvertisementNotFound_thenNotFound() throws Exception {
        CommentRequestDto req = new CommentRequestDto();
        req.setAdvertisementId(UUID.randomUUID());
        req.setCommentText("Hello");

        when(commentService.addComment(req)).thenThrow(new AdvertisementNotFoundException(req.getAdvertisementId()));

        mvc.perform(post("/api/v1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void addComment_whenParentHasNoCommenter_thenConflict() throws Exception {
        CommentRequestDto req = new CommentRequestDto();
        req.setAdvertisementId(UUID.randomUUID());
        req.setParentCommentId(UUID.randomUUID());
        req.setCommentText("Reply");

        when(commentService.addComment(req)).thenThrow(new AddingCommentWhenParentCommenterIsNullException());

        mvc.perform(post("/api/v1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithAnonymousUser
    void addComment_whenAnonymous_thenUnauthorized() throws Exception {
        CommentRequestDto req = new CommentRequestDto();
        req.setAdvertisementId(UUID.randomUUID());
        req.setCommentText("Hi");

        mvc.perform(post("/api/v1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // --- deleteComment ---

    @Test
    @WithMockUser
    void deleteComment_whenValid_thenNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        mvc.perform(delete("/api/v1/comment/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteComment_whenNotFound_thenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new CommentNotFoundException(id)).when(commentService).deleteComment(id);

        mvc.perform(delete("/api/v1/comment/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deleteComment_whenNotAllowed_thenForbidden() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new UserDeleteCommentException()).when(commentService).deleteComment(id);

        mvc.perform(delete("/api/v1/comment/{id}", id))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void deleteComment_whenAnonymous_thenUnauthorized() throws Exception {
        mvc.perform(delete("/api/v1/comment/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // --- findCommentById ---

    @Test
    @WithMockUser
    void findCommentById_whenExists_thenOk() throws Exception {
        UUID id = UUID.randomUUID();
        CommentResponseDto resp = new CommentResponseDto();
        resp.setId(id);
        when(commentService.findCommentById(id)).thenReturn(resp);

        mvc.perform(get("/api/v1/comment/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(resp)));
    }

    @Test
    @WithMockUser
    void findCommentById_whenNotFound_thenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(commentService.findCommentById(id)).thenThrow(new CommentNotFoundException(id));

        mvc.perform(get("/api/v1/comment/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    void findCommentById_whenAnonymous_thenUnauthorized() throws Exception {
        mvc.perform(get("/api/v1/comment/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // --- findAllByAdvertisementId ---

    @Test
    @WithMockUser
    void findAllByAdvertisementId_whenValid_thenOk() throws Exception {
        UUID adId = UUID.randomUUID();
        List<CommentResponseDto> list = Collections.singletonList(new CommentResponseDto());
        when(commentService.findAllByAdvertisementId(adId, 0, 10)).thenReturn(list);

        mvc.perform(get("/api/v1/comment/advertisement/{adId}", adId))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(list)));
    }

    @Test
    @WithMockUser
    void findAllByAdvertisementId_whenBadParams_thenBadRequest() throws Exception {
        UUID adId = UUID.randomUUID();
        mvc.perform(get("/api/v1/comment/advertisement/{adId}", adId)
                        .param("page", "-1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void findAllByAdvertisementId_whenAdNotFound_thenNotFound() throws Exception {
        UUID adId = UUID.randomUUID();
        when(commentService.findAllByAdvertisementId(adId, 0, 10))
                .thenThrow(new AdvertisementNotFoundException(adId));

        mvc.perform(get("/api/v1/comment/advertisement/{adId}", adId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    void findAllByAdvertisementId_whenAnonymous_thenUnauthorized() throws Exception {
        mvc.perform(get("/api/v1/comment/advertisement/{adId}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}
