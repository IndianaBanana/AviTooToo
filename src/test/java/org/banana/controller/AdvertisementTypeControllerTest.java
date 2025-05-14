package org.banana.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.banana.config.SecurityConfig;
import org.banana.dto.advertisement.type.AdvertisementTypeDto;
import org.banana.exception.AdvertisementTypeAlreadyExistsException;
import org.banana.security.service.JwtService;
import org.banana.service.AdvertisementTypeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdvertisementTypeController.class)
@Import({SecurityConfig.class})
class AdvertisementTypeControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AdvertisementTypeService advertisementTypeService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @WithMockUser(username = "bob")
    void getAllAdvertisementTypes_whenCalled_thenReturnsList() throws Exception {
        List<AdvertisementTypeDto> types =
                singletonList(new AdvertisementTypeDto(UUID.randomUUID(), "Banner"));
        when(advertisementTypeService.findAll()).thenReturn(types);

        mvc.perform(get("/api/v1/advertisement-type"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(types)));
    }

    @Test
    @WithMockUser(username = "bob")
    void searchAdvertisementTypes_whenCalledWithPattern_thenReturnsFilteredList() throws Exception {
        List<AdvertisementTypeDto> types =
                singletonList(new AdvertisementTypeDto(UUID.randomUUID(), "Video"));
        when(advertisementTypeService.findByNameLike("Vid")).thenReturn(types);

        mvc.perform(get("/api/v1/advertisement-type/Vid"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(types)));
    }

    @Test
    @WithMockUser(username = "bob", roles = {"ADMIN"})
    void addAdvertisementType_whenUserIsAdmin_thenReturnsNewType() throws Exception {

        AdvertisementTypeDto added =
                new AdvertisementTypeDto(UUID.randomUUID(), "Popup");
        when(advertisementTypeService.addAdvertisementType("Popup")).thenReturn(added);

        mvc.perform(post("/api/v1/advertisement-type/Popup"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(added)));
    }

    @Test
    @WithMockUser(username = "bob")
    void addAdvertisementType_whenUserIsNotAdmin_thenReturnsForbidden() throws Exception {

        mvc.perform(post("/api/v1/advertisement-type/Interstitial"))
                .andExpect(status().isForbidden());
    }

    @Test
    void addAdvertisementType_whenNotAuthenticated_thenReturnsUnauthorized() throws Exception {
        mvc.perform(post("/api/v1/advertisement-type/Native"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "bob", roles = {"ADMIN"})
    void addAdvertisementType_whenAlreadyExists_thenReturnsConflict() throws Exception {
        when(advertisementTypeService.addAdvertisementType("Banner"))
                .thenThrow(new AdvertisementTypeAlreadyExistsException("Banner"));

        mvc.perform(post("/api/v1/advertisement-type/Banner"))
                .andExpect(status().isConflict());
    }
}
