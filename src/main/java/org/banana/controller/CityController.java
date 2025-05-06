package org.banana.controller;

import lombok.RequiredArgsConstructor;
import org.banana.dto.city.CityDto;
import org.banana.service.CityService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/city")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @GetMapping
    public List<CityDto> getAllCities() {
        return cityService.findAll();
    }

    @GetMapping("/{pattern}")
    public List<CityDto> searchCities(@PathVariable String pattern) {
        return cityService.findByNameLike(pattern);
    }

    @PostMapping("/{name}")
    @PreAuthorize("hasRole('ADMIN')")
    public CityDto addCity(@PathVariable String name) {
        return cityService.addCity(name);
    }
}
