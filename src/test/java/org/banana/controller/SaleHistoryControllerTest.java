package org.banana.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.banana.security.config.SecurityConfig;
import org.banana.dto.history.SaleHistoryAddRequestDto;
import org.banana.dto.history.SaleHistoryResponseDto;
import org.banana.dto.history.SaleHistoryTotalForAdvertisementsResponseDto;
import org.banana.exception.AdvertisementNotFoundException;
import org.banana.exception.SaleHistoryAccessDeniedException;
import org.banana.exception.SaleHistoryAdvertisementQuantityIsLowerThanExpectedException;
import org.banana.exception.SaleHistoryNotFoundException;
import org.banana.exception.SaleHistoryUnexpectedException;
import org.banana.security.service.JwtService;
import org.banana.service.SaleHistoryService;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SaleHistoryController.class)
@Import(SecurityConfig.class)
class SaleHistoryControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SaleHistoryService saleHistoryService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    // --- addSale ---

    @Test
    @WithMockUser
    void addSale_whenValid_thenCreated() throws Exception {
        SaleHistoryAddRequestDto req = new SaleHistoryAddRequestDto(UUID.randomUUID(), 2);
        SaleHistoryResponseDto resp = new SaleHistoryResponseDto(
                UUID.randomUUID(), "Title", req.getAdvertisementId(), UUID.randomUUID(), LocalDateTime.now(), req.getQuantity());
        when(saleHistoryService.addSale(req)).thenReturn(resp);

        mvc.perform(post("/api/v1/sale-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(resp)));
    }

    @Test
    @WithMockUser
    void addSale_whenInvalidDto_thenBadRequest() throws Exception {
        SaleHistoryAddRequestDto req = new SaleHistoryAddRequestDto(UUID.randomUUID(), 0);

        mvc.perform(post("/api/v1/sale-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("quantity")));
    }

    @Test
    @WithMockUser
    void addSale_whenAdNotFound_thenNotFound() throws Exception {
        SaleHistoryAddRequestDto req = new SaleHistoryAddRequestDto(UUID.randomUUID(), 1);
        when(saleHistoryService.addSale(req)).thenThrow(new AdvertisementNotFoundException(req.getAdvertisementId()));

        mvc.perform(post("/api/v1/sale-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void addSale_whenQuantityTooHigh_thenConflict() throws Exception {
        SaleHistoryAddRequestDto req = new SaleHistoryAddRequestDto(UUID.randomUUID(), 5);
        when(saleHistoryService.addSale(req))
                .thenThrow(new SaleHistoryAdvertisementQuantityIsLowerThanExpectedException(2, 5));

        mvc.perform(post("/api/v1/sale-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithAnonymousUser
    void addSale_whenAnonymous_thenUnauthorized() throws Exception {
        SaleHistoryAddRequestDto req = new SaleHistoryAddRequestDto(UUID.randomUUID(), 1);

        mvc.perform(post("/api/v1/sale-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser
    void addSale_whenUnexpectedException_thenInternalServerError() throws Exception {
        SaleHistoryAddRequestDto req = new SaleHistoryAddRequestDto(UUID.randomUUID(), 1);
        when(saleHistoryService.addSale(req))
                .thenThrow(new SaleHistoryUnexpectedException());

        mvc.perform(post("/api/v1/sale-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }

    // --- getSalesByAdvertisement ---

    @Test
    @WithMockUser
    void getSalesByAdvertisement_whenValid_thenOk() throws Exception {
        UUID adId = UUID.randomUUID();
        List<SaleHistoryResponseDto> list = List.of(
                new SaleHistoryResponseDto(UUID.randomUUID(), "T1", adId, UUID.randomUUID(), LocalDateTime.now(), 1)
        );
        when(saleHistoryService.getSalesByAdvertisementId(adId)).thenReturn(list);

        mvc.perform(get("/api/v1/sale-history/advertisement/{adId}", adId))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(list)));
    }

    @Test
    @WithMockUser
    void getSalesByAdvertisement_whenAdNotFound_thenNotFound() throws Exception {
        UUID adId = UUID.randomUUID();
        when(saleHistoryService.getSalesByAdvertisementId(adId))
                .thenThrow(new AdvertisementNotFoundException(adId));

        mvc.perform(get("/api/v1/sale-history/advertisement/{adId}", adId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getSalesByAdvertisement_whenAccessDenied_thenForbidden() throws Exception {
        UUID adId = UUID.randomUUID();
        when(saleHistoryService.getSalesByAdvertisementId(adId))
                .thenThrow(new SaleHistoryAccessDeniedException());

        mvc.perform(get("/api/v1/sale-history/advertisement/{adId}", adId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void getSalesByAdvertisement_whenAnonymous_thenUnauthorized() throws Exception {
        mvc.perform(get("/api/v1/sale-history/advertisement/{adId}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // --- getTotals ---

    @Test
    @WithMockUser
    void getTotals_whenValid_thenOk() throws Exception {
        List<SaleHistoryTotalForAdvertisementsResponseDto> totals = List.of(
                new SaleHistoryTotalForAdvertisementsResponseDto(
                        UUID.randomUUID(), "Ad", new BigDecimal("100"), 10L, LocalDateTime.now().minusDays(1), LocalDateTime.now()
                )
        );
        when(saleHistoryService.getTotalForSalesInAdvertisements()).thenReturn(totals);

        mvc.perform(get("/api/v1/sale-history/totals"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(totals)));
    }

    @Test
    @WithAnonymousUser
    void getTotals_whenAnonymous_thenUnauthorized() throws Exception {
        mvc.perform(get("/api/v1/sale-history/totals"))
                .andExpect(status().isUnauthorized());
    }

    // --- deleteSale ---

    @Test
    @WithMockUser
    void cancelSale_whenValid_thenNoContent() throws Exception {
        UUID saleId = UUID.randomUUID();

        mvc.perform(delete("/api/v1/sale-history/{saleId}", saleId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void cancelSale_whenNotFound_thenNotFound() throws Exception {
        UUID saleId = UUID.randomUUID();
        doThrow(new SaleHistoryNotFoundException(saleId)).when(saleHistoryService).deleteSale(saleId);

        mvc.perform(delete("/api/v1/sale-history/{saleId}", saleId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void cancelSale_whenAccessDenied_thenForbidden() throws Exception {
        UUID saleId = UUID.randomUUID();
        doThrow(new SaleHistoryAccessDeniedException()).when(saleHistoryService).deleteSale(saleId);

        mvc.perform(delete("/api/v1/sale-history/{saleId}", saleId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void cancelSale_whenAnonymous_thenUnauthorized() throws Exception {
        mvc.perform(delete("/api/v1/sale-history/{saleId}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void cancelSale_whenUnexpectedException_thenInternalServerError() throws Exception {
        UUID saleId = UUID.randomUUID();
        doThrow(new SaleHistoryUnexpectedException()).when(saleHistoryService).deleteSale(saleId);

        mvc.perform(delete("/api/v1/sale-history/{saleId}", saleId))
                .andExpect(status().isInternalServerError());
    }
}
