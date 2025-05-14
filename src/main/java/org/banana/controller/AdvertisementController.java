package org.banana.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.banana.dto.advertisement.AdvertisementFilterDto;
import org.banana.dto.advertisement.AdvertisementRequestDto;
import org.banana.dto.advertisement.AdvertisementResponseDto;
import org.banana.service.AdvertisementService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/advertisement")
@RequiredArgsConstructor
@Validated
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    @GetMapping("/{id}")
    public ResponseEntity<AdvertisementResponseDto> getAdvertisementById(@PathVariable UUID id) {
        return ResponseEntity.ok(advertisementService.findById(id));
    }

    @PostMapping("/filter")
    public ResponseEntity<List<AdvertisementResponseDto>> getFilteredAdvertisements(
            @Valid @RequestBody AdvertisementFilterDto filter,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        return ResponseEntity.ok(advertisementService.findAllFiltered(filter, page, size));
    }

    @PostMapping
    public ResponseEntity<AdvertisementResponseDto> createAdvertisement(@RequestBody @Valid AdvertisementRequestDto requestDto) {
        AdvertisementResponseDto created = advertisementService.createAdvertisement(requestDto);
        URI uri = URI.create(String.format("/api/v1/advertisement/%s", created.getId()));
        return ResponseEntity.created(uri).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdvertisementResponseDto> updateAdvertisement(@PathVariable("id") UUID advertisementId, @RequestBody @Valid AdvertisementRequestDto requestDto) {
        return ResponseEntity.ok(advertisementService.updateAdvertisement(advertisementId, requestDto));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<AdvertisementResponseDto> closeAdvertisement(@PathVariable UUID id) {
        return ResponseEntity.ok(advertisementService.closeAdvertisement(id));
    }

    @PatchMapping("/{id}/promote")
    public ResponseEntity<AdvertisementResponseDto> promoteAdvertisement(@PathVariable UUID id) {
        return ResponseEntity.ok(advertisementService.promoteAdvertisement(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdvertisement(@PathVariable UUID id) {
        advertisementService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
