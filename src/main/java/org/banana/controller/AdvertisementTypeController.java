package org.banana.controller;

import lombok.RequiredArgsConstructor;
import org.banana.dto.advertisement.type.AdvertisementTypeDto;
import org.banana.service.AdvertisementTypeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/advertisement-type")
@RequiredArgsConstructor
public class AdvertisementTypeController {

    private final AdvertisementTypeService advertisementTypeService;

    @GetMapping
    public List<AdvertisementTypeDto> getAllAdvertisementTypes() {
        return advertisementTypeService.findAll();
    }

    @GetMapping("/{pattern}")
    public List<AdvertisementTypeDto> searchAdvertisementTypes(@PathVariable String pattern) {
        return advertisementTypeService.findByNameLike(pattern);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{name}")
    public AdvertisementTypeDto addAdvertisementType(@PathVariable String name) {
        return advertisementTypeService.addAdvertisementType(name);
    }
}
