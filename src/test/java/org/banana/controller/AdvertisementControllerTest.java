package org.banana.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.banana.config.SecurityConfig;
import org.banana.dto.advertisement.AdvertisementFilterDto;
import org.banana.dto.advertisement.AdvertisementResponseDto;
import org.banana.exception.AdvertisementNotFoundException;
import org.banana.security.service.JwtService;
import org.banana.service.AdvertisementService;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdvertisementController.class)
@Import({SecurityConfig.class})
class AdvertisementControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdvertisementService advertisementService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "bob", roles = {"USER"})
    void getAdvertisementById_whenAdvertisementExists_thenReturnsAdvertisement() throws Exception {
        AdvertisementResponseDto dto = new AdvertisementResponseDto();
        dto.setId(UUID.randomUUID());

        when(advertisementService.findById(dto.getId())).thenReturn(dto);

        mvc.perform(get("/api/v1/advertisement/{id}", dto.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(dto)));
    }

    @Test
    @WithAnonymousUser
    void getAdvertisementById_whenNotAuthorized_thenReturnsUnauthorized() throws Exception {
        mvc.perform(get("/api/v1/advertisement/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "bob", roles = {"USER"})
    void getAdvertisementById_whenAdvertisementDoesNotExist_thenReturnsNotFound() throws Exception {
        AdvertisementResponseDto dto = new AdvertisementResponseDto();
        dto.setId(UUID.randomUUID());

        when(advertisementService.findById(dto.getId())).thenThrow(new AdvertisementNotFoundException(dto.getId()));

        mvc.perform(get("/api/v1/advertisement/{id}", dto.getId()))
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(username = "bob", roles = {"USER"})
    void getFilteredAdvertisements_whenFilterIsValid_thenReturnsList() throws Exception {
        AdvertisementFilterDto filter = new AdvertisementFilterDto();
        System.out.println(filter);
        List<AdvertisementResponseDto> dtos = Collections.emptyList();
        when(advertisementService.findAllFiltered(filter, 0, 5)).thenReturn(dtos);

        mvc.perform(post("/api/v1/advertisement/filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter))
                        .param("page", "0")
                        .param("size", "5")
                )
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(dtos)));
    }

    @Test
    @WithAnonymousUser
    void getFilteredAdvertisements_whenNotAuthorized_thenReturnsUnauthorized() throws Exception {
        AdvertisementFilterDto filter = new AdvertisementFilterDto();
        filter.setSearchParam("test");
        mvc.perform(post("/api/v1/advertisement/filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "bob", roles = {"USER"})
    void getFilteredAdvertisements_whenPagingParametersAreInvalid_thenReturnsBadRequest() throws Exception {
        AdvertisementFilterDto filter = new AdvertisementFilterDto();
        filter.setSearchParam("test");
        mvc.perform(post("/api/v1/advertisement/filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter))
                        .param("page", "-1")
                        .param("size", "-1")
                )
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains("padddddge")));
    }
}