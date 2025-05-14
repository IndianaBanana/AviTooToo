package org.banana.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.banana.config.SecurityConfig;
import org.banana.dto.advertisement.AdvertisementFilterDto;
import org.banana.dto.advertisement.AdvertisementRequestDto;
import org.banana.dto.advertisement.AdvertisementResponseDto;
import org.banana.exception.AdvertisementNotFoundException;
import org.banana.exception.AdvertisementTypeNotFoundException;
import org.banana.exception.AdvertisementUpdateException;
import org.banana.exception.CityNotFoundException;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.banana.exception.AdvertisementUpdateException.AdvertisementUpdateExceptionMessage.ALREADY_CLOSED;
import static org.banana.exception.AdvertisementUpdateException.AdvertisementUpdateExceptionMessage.ALREADY_PROMOTED;
import static org.banana.exception.AdvertisementUpdateException.AdvertisementUpdateExceptionMessage.NOT_OWNER;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
    @WithMockUser(username = "bob")
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
    @WithMockUser(username = "bob")
    void getAdvertisementById_whenAdvertisementDoesNotExist_thenReturnsNotFound() throws Exception {
        AdvertisementResponseDto dto = new AdvertisementResponseDto();
        dto.setId(UUID.randomUUID());

        when(advertisementService.findById(dto.getId())).thenThrow(new AdvertisementNotFoundException(dto.getId()));

        mvc.perform(get("/api/v1/advertisement/{id}", dto.getId()))
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(username = "bob")
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
    @WithMockUser(username = "bob")
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
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains("\"errors\":{\"size\":\"must be greater than or equal to 1\",\"page\":\"must be greater than or equal to 0\"}")));
    }

    @Test
    @WithMockUser(username = "bob")
    void getFilteredAdvertisements_whenFilterParametersAreInvalid_thenReturnsBadRequest() throws Exception {
        AdvertisementFilterDto filter = new AdvertisementFilterDto();
        filter.setMaxPrice(BigDecimal.valueOf(-1));
        filter.setMinPrice(BigDecimal.valueOf(-1));
        mvc.perform(post("/api/v1/advertisement/filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter))
                        .param("page", "-1")
                        .param("size", "-1")
                )
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains("\"errors\":{\"minPrice\":\"must be greater than or equal to 0.00\",\"maxPrice\":\"must be greater than or equal to 0.00\"}")));
    }

    @Test
    @WithMockUser
    void createAdvertisement_whenValid_thenCreated() throws Exception {
        AdvertisementRequestDto request = new AdvertisementRequestDto(
                UUID.randomUUID(), UUID.randomUUID(), "Title", "Desc", BigDecimal.valueOf(10), 1);
        AdvertisementResponseDto response = new AdvertisementResponseDto();
        response.setId(UUID.randomUUID());

        when(advertisementService.createAdvertisement(request)).thenReturn(response);

        mvc.perform(post("/api/v1/advertisement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/advertisement/" + response.getId()))
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @WithMockUser
    void createAdvertisement_whenInvalid_thenBadRequest() throws Exception {
        AdvertisementRequestDto invalid = new AdvertisementRequestDto();
        mvc.perform(post("/api/v1/advertisement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithAnonymousUser
    void createAdvertisement_whenNotAuthenticated_thenUnauthorized() throws Exception {
        AdvertisementRequestDto request = new AdvertisementRequestDto(
                UUID.randomUUID(), UUID.randomUUID(), "T", "D", BigDecimal.ZERO, 1);
        mvc.perform(post("/api/v1/advertisement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void createAdvertisement_whenAdvertisementTypeNotFound_thenThrowsException() throws Exception {
        AdvertisementRequestDto request = new AdvertisementRequestDto(
                UUID.randomUUID(), UUID.randomUUID(), "Title", "Desc", BigDecimal.valueOf(10), 1);

        when(advertisementService.createAdvertisement(request))
                .thenThrow(new AdvertisementTypeNotFoundException(request.getAdvertisementTypeId()));

        mvc.perform(post("/api/v1/advertisement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void createAdvertisement_whenCityNotFound_thenThrowsException() throws Exception {
        AdvertisementRequestDto request = new AdvertisementRequestDto(
                UUID.randomUUID(), UUID.randomUUID(), "Title", "Desc", BigDecimal.valueOf(10), 1);

        when(advertisementService.createAdvertisement(request))
                .thenThrow(new CityNotFoundException(request.getCityId()));

        mvc.perform(post("/api/v1/advertisement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // --- updateAdvertisement ---

    @Test
    @WithMockUser
    void updateAdvertisement_whenValid_thenOk() throws Exception {
        UUID id = UUID.randomUUID();
        AdvertisementRequestDto request = new AdvertisementRequestDto(
                UUID.randomUUID(), UUID.randomUUID(), "New title", "New desc", BigDecimal.valueOf(5), 2);
        AdvertisementResponseDto response = new AdvertisementResponseDto();
        response.setId(id);

        when(advertisementService.updateAdvertisement(eq(id), eq(request))).thenReturn(response);

        mvc.perform(put("/api/v1/advertisement/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @WithMockUser
    void updateAdvertisement_whenNotFound_thenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        AdvertisementRequestDto request = new AdvertisementRequestDto(
                UUID.randomUUID(), UUID.randomUUID(), "t", "d", BigDecimal.ZERO, 1);

        when(advertisementService.updateAdvertisement(eq(id), eq(request)))
                .thenThrow(new AdvertisementNotFoundException(id));

        mvc.perform(put("/api/v1/advertisement/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains(id.toString())));
    }

    @Test
    @WithMockUser
    void updateAdvertisement_whenInvalid_thenBadRequest() throws Exception {
        UUID id = UUID.randomUUID();
        AdvertisementRequestDto invalid = new AdvertisementRequestDto();

        mvc.perform(put("/api/v1/advertisement/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithAnonymousUser
    void updateAdvertisement_whenNotAuthenticated_thenUnauthorized() throws Exception {
        mvc.perform(put("/api/v1/advertisement/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdvertisementRequestDto())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void updateAdvertisement_whenNotOwner_thenBadRequest() throws Exception {
        UUID id = UUID.randomUUID();
        AdvertisementRequestDto request = new AdvertisementRequestDto(UUID.randomUUID(), UUID.randomUUID(), "t", "d", BigDecimal.ZERO, 1);
        when(advertisementService.updateAdvertisement(eq(id), eq(request))).thenThrow(new AdvertisementUpdateException(NOT_OWNER));

        mvc.perform(put("/api/v1/advertisement/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains(NOT_OWNER.getDescription())));
    }


    @Test
    @WithMockUser
    void updateAdvertisement_whenAlreadyClosed_thenBadRequest() throws Exception {
        UUID id = UUID.randomUUID();
        AdvertisementRequestDto request = new AdvertisementRequestDto(UUID.randomUUID(), UUID.randomUUID(), "t", "d", BigDecimal.ZERO, 1);
        when(advertisementService.updateAdvertisement(eq(id), eq(request))).thenThrow(new AdvertisementUpdateException(ALREADY_CLOSED));

        mvc.perform(put("/api/v1/advertisement/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains(ALREADY_CLOSED.getDescription())));
    }


    // --- closeAdvertisement ---

    @Test
    @WithMockUser
    void closeAdvertisement_whenValid_thenOk() throws Exception {
        UUID id = UUID.randomUUID();
        AdvertisementResponseDto dto = new AdvertisementResponseDto();
        dto.setId(id);

        when(advertisementService.closeAdvertisement(id)).thenReturn(dto);

        mvc.perform(patch("/api/v1/advertisement/{id}/close", id))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void closeAdvertisement_whenAlreadyClosed_thenBadRequest() throws Exception {
        UUID id = UUID.randomUUID();
        when(advertisementService.closeAdvertisement(id))
                .thenThrow(new AdvertisementUpdateException(ALREADY_CLOSED));

        mvc.perform(patch("/api/v1/advertisement/{id}/close", id))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains(ALREADY_CLOSED.getDescription())));
    }

    @Test
    @WithMockUser
    void closeAdvertisement_whenNotOwnerOrNotAdmin_thenBadRequest() throws Exception {
        UUID id = UUID.randomUUID();
        when(advertisementService.closeAdvertisement(id))
                .thenThrow(new AdvertisementUpdateException(NOT_OWNER));

        mvc.perform(patch("/api/v1/advertisement/{id}/close", id))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains(NOT_OWNER.getDescription())));
    }

    @Test
    @WithMockUser
    void closeAdvertisement_whenNotFound_thenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(advertisementService.closeAdvertisement(id))
                .thenThrow(new AdvertisementNotFoundException(id));

        mvc.perform(patch("/api/v1/advertisement/{id}/close", id))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains(String.valueOf(id))));
    }

    @Test
    @WithAnonymousUser
    void closeAdvertisement_whenNotAuthenticated_thenUnauthorized() throws Exception {
        mvc.perform(patch("/api/v1/advertisement/{id}/close", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // --- promoteAdvertisement ---

    @Test
    @WithMockUser
    void promoteAdvertisement_whenValid_thenOk() throws Exception {
        UUID id = UUID.randomUUID();
        AdvertisementResponseDto dto = new AdvertisementResponseDto();
        dto.setId(id);

        when(advertisementService.promoteAdvertisement(id)).thenReturn(dto);

        mvc.perform(patch("/api/v1/advertisement/{id}/promote", id))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void promoteAdvertisement_whenNotFound_thenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(advertisementService.promoteAdvertisement(id))
                .thenThrow(new AdvertisementNotFoundException(id));

        mvc.perform(patch("/api/v1/advertisement/{id}/promote", id))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains(String.valueOf(id))));
    }

    @Test
    @WithMockUser
    void promoteAdvertisement_whenAlreadyPromoted_thenBadRequest() throws Exception {
        UUID id = UUID.randomUUID();
        when(advertisementService.promoteAdvertisement(id))
                .thenThrow(new AdvertisementUpdateException(ALREADY_PROMOTED));

        mvc.perform(patch("/api/v1/advertisement/{id}/promote", id))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains(ALREADY_PROMOTED.getDescription())));
    }


    @Test
    @WithMockUser
    void promoteAdvertisement_whenNotOwner_thenBadRequest() throws Exception {
        UUID id = UUID.randomUUID();
        when(advertisementService.promoteAdvertisement(id))
                .thenThrow(new AdvertisementUpdateException(NOT_OWNER));

        mvc.perform(patch("/api/v1/advertisement/{id}/promote", id))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains(NOT_OWNER.getDescription())));
    }

    @Test
    @WithMockUser
    void promoteAdvertisement_whenAlreadyClosed_thenBadRequest() throws Exception {
        UUID id = UUID.randomUUID();
        when(advertisementService.promoteAdvertisement(id))
                .thenThrow(new AdvertisementUpdateException(ALREADY_CLOSED));

        mvc.perform(patch("/api/v1/advertisement/{id}/promote", id))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains(ALREADY_CLOSED.getDescription())));
    }

    @Test
    @WithAnonymousUser
    void promoteAdvertisement_whenNotAuthenticated_thenUnauthorized() throws Exception {
        mvc.perform(patch("/api/v1/advertisement/{id}/promote", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // --- deleteAdvertisement ---

    @Test
    @WithMockUser
    void deleteAdvertisement_whenValid_thenNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        mvc.perform(delete("/api/v1/advertisement/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteAdvertisement_whenNotOwnerOrNotAdmin_thenBadRequest() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new AdvertisementUpdateException(NOT_OWNER))
                .when(advertisementService).deleteById(id);

        mvc.perform(delete("/api/v1/advertisement/{id}", id))
                .andExpect(status().isConflict());
    }


    @Test
    @WithMockUser
    void deleteAdvertisement_whenNotFound_thenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new AdvertisementNotFoundException(id))
                .when(advertisementService).deleteById(id);

        mvc.perform(delete("/api/v1/advertisement/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains(String.valueOf(id))));
    }

    @Test
    @WithAnonymousUser
    void deleteAdvertisement_whenNotAuthenticated_thenUnauthorized() throws Exception {
        mvc.perform(delete("/api/v1/advertisement/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}

