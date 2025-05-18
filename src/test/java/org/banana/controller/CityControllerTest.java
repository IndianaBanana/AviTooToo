package org.banana.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.banana.config.SecurityConfig;
import org.banana.dto.city.CityDto;
import org.banana.exception.CityAlreadyExistsException;
import org.banana.security.service.JwtService;
import org.banana.service.CityService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CityController.class)
@Import({SecurityConfig.class})
class CityControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CityService cityService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @WithMockUser(username = "bob")
    void getAllCities_whenCalled_thenReturnsCityList() throws Exception {
        List<CityDto> cities = singletonList(new CityDto(UUID.randomUUID(), "Paris"));
        when(cityService.findAll()).thenReturn(cities);
        mvc.perform(get("/api/v1/city"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(cities)));
    }

    @Test
    @WithMockUser(username = "bob")
    void searchCities_whenCalledWithPattern_thenReturnsFilteredCityList() throws Exception {
        List<CityDto> cities = singletonList(new CityDto(UUID.randomUUID(), "London"));
        when(cityService.findByNameLike("Lon")).thenReturn(cities);

        mvc.perform(get("/api/v1/city/Lon"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(cities)));
    }

    @Test
    @WithMockUser(username = "bob", roles = {"ADMIN"})
    void addCity_whenUserIsAdmin_thenReturnsNewCity() throws Exception {

        CityDto added = new CityDto(UUID.randomUUID(), "Berlin");
        when(cityService.addCity("Berlin")).thenReturn(added);

        mvc.perform(post("/api/v1/city/Berlin"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/city/" + added.getName()))
                .andExpect(content().json(objectMapper.writeValueAsString(added)));
    }

    @Test
    @WithMockUser(username = "bob")
    void addCity_whenUserIsNotAdmin_thenReturnsForbidden() throws Exception {

        mvc.perform(post("/api/v1/city/Rome"))
                .andExpect(status().isForbidden());
    }

    @Test
    void addCity_whenNotAuthenticated_thenReturnsUnauthorized() throws Exception {
        mvc.perform(post("/api/v1/city/Warsaw"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(username = "bob", roles = {"ADMIN"})
    void addCity_whenCityAlreadyExists_thenReturnsConflict() throws Exception {
        when(cityService.addCity("Berlin")).thenThrow(new CityAlreadyExistsException("Berlin"));

        mvc.perform(post("/api/v1/city/Berlin"))
                .andExpect(status().isConflict());
    }
}
