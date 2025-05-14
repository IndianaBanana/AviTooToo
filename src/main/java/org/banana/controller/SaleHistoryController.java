package org.banana.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.banana.dto.history.SaleHistoryAddRequestDto;
import org.banana.dto.history.SaleHistoryResponseDto;
import org.banana.dto.history.SaleHistoryTotalForAdvertisementsResponseDto;
import org.banana.service.SaleHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sale-history")
@RequiredArgsConstructor
public class SaleHistoryController {

    private final SaleHistoryService saleHistoryService;

    @PostMapping
    public ResponseEntity<SaleHistoryResponseDto> addSale(@RequestBody @Valid SaleHistoryAddRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saleHistoryService.addSale(requestDto));
    }

    @GetMapping("/advertisement/{adId}")
    public ResponseEntity<List<SaleHistoryResponseDto>> getSalesByAdvertisement(@PathVariable("adId") UUID advertisementId) {

        List<SaleHistoryResponseDto> sales =
                saleHistoryService.getSalesByAdvertisementId(advertisementId);

        return ResponseEntity.ok(sales);
    }

    @GetMapping("/totals")
    public ResponseEntity<List<SaleHistoryTotalForAdvertisementsResponseDto>> getTotals() {
        List<SaleHistoryTotalForAdvertisementsResponseDto> totals =
                saleHistoryService.getTotalForSalesInAdvertisements();

        return ResponseEntity.ok(totals);
    }

    @DeleteMapping("/{saleId}")
    public ResponseEntity<Void> cancelSale(@PathVariable UUID saleId) {
        saleHistoryService.cancelSale(saleId);
        return ResponseEntity.noContent().build();
    }
}
