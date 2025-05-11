package org.banana.service;

import org.banana.dto.history.SaleHistoryAddRequestDto;
import org.banana.dto.history.SaleHistoryMapper;
import org.banana.dto.history.SaleHistoryResponseDto;
import org.banana.dto.history.SaleHistoryTotalForAdvertisementsResponseDto;
import org.banana.entity.Advertisement;
import org.banana.entity.SaleHistory;
import org.banana.entity.User;
import org.banana.exception.AdvertisementNotFoundException;
import org.banana.exception.SaleHistoryAccessDeniedException;
import org.banana.exception.SaleHistoryAdvertisementQuantityIsLowerThanExpectedException;
import org.banana.exception.SaleHistoryNotFoundException;
import org.banana.repository.AdvertisementRepository;
import org.banana.repository.SaleHistoryRepository;
import org.banana.repository.UserRepository;
import org.banana.security.UserRole;
import org.banana.security.dto.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleHistoryServiceImplTest {

    @Mock
    private SaleHistoryRepository saleHistoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AdvertisementRepository advertisementRepository;
    @Mock
    private SaleHistoryMapper saleHistoryMapper;

    @InjectMocks
    private SaleHistoryServiceImpl saleHistoryService;

    private Advertisement advertisement;
    private UUID advertisementId;
    private UUID userId;
    private User owner;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        advertisementId = UUID.randomUUID();
        owner = new User();
        owner.setId(UUID.randomUUID());

        advertisement = new Advertisement();
        advertisement.setId(advertisementId);
        advertisement.setQuantity(5);
        advertisement.setUser(owner);
    }

    @Test
    void addSale_whenAdvertisementNotFound_thenShouldThrowAdvertisementNotFoundException() {
        when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.empty());
        SaleHistoryAddRequestDto dto = new SaleHistoryAddRequestDto(advertisementId, 1);

        assertThatThrownBy(() -> saleHistoryService.addSale(dto))
                .isInstanceOf(AdvertisementNotFoundException.class)
                .hasMessageContaining(advertisementId.toString());
    }

    @Test
    void addSale_whenAdvertisementQuantityTooLow_thenShouldThrowSaleHistoryAdvertisementQuantityIsLowerThanExpectedException() {
        advertisement.setQuantity(1);
        when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.of(advertisement));

        SaleHistoryAddRequestDto dto = new SaleHistoryAddRequestDto(advertisementId, 5);

        assertThatThrownBy(() -> saleHistoryService.addSale(dto))
                .isInstanceOf(SaleHistoryAdvertisementQuantityIsLowerThanExpectedException.class);
    }

    @Test
    void addSale_whenValidRequest_thenShouldReturnSaleHistoryResponseDto() {
        mockCurrentUser(userId, UserRole.ROLE_USER);
        SaleHistory saleHistory = new SaleHistory(advertisement, userId, 2, LocalDateTime.now());
        ArgumentCaptor<SaleHistory> historyArgumentCaptor = ArgumentCaptor.forClass(SaleHistory.class);
        when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.of(advertisement));
        when(saleHistoryRepository.save(any(SaleHistory.class))).thenReturn(saleHistory);
        when(advertisementRepository.save(any(Advertisement.class))).thenReturn(advertisement);
        when(saleHistoryMapper.fromSaleHistoryToSaleHistoryResponseDto(saleHistory)).thenReturn(mock(SaleHistoryResponseDto.class));

        SaleHistoryAddRequestDto dto = new SaleHistoryAddRequestDto(advertisementId, 2);
        SaleHistoryResponseDto response = saleHistoryService.addSale(dto);
        verify(saleHistoryRepository).save(historyArgumentCaptor.capture());
        assertThat(historyArgumentCaptor.getValue()).extracting("advertisement", "buyerId", "quantity")
                .contains(advertisement, userId, 2);
        assertThat(response).isNotNull();
        assertThat(advertisement.getQuantity()).isEqualTo(3);
    }

    @Test
    void cancelSale_whenOwner_thenDeletesAndRestoresQuantity() {
        UUID saleId = UUID.randomUUID();
        advertisement.setQuantity(5);
        SaleHistory sale = new SaleHistory(advertisement, userId, 2, LocalDateTime.now());
        sale.setId(saleId);

        when(saleHistoryRepository.findById(saleId)).thenReturn(Optional.of(sale));

        mockCurrentUser(owner.getId(), UserRole.ROLE_USER);

        saleHistoryService.cancelSale(saleId);

        verify(advertisementRepository).save(advertisement);
        assertThat(advertisement.getQuantity()).isEqualTo(7);
        verify(saleHistoryRepository).delete(sale);
    }

    @Test
    void cancelSale_whenAdmin_thenDeletesAndRestoresQuantity() {
        UUID saleId = UUID.randomUUID();
        advertisement.setQuantity(5);
        SaleHistory sale = new SaleHistory(advertisement, userId, 2, LocalDateTime.now());
        sale.setId(saleId);

        when(saleHistoryRepository.findById(saleId)).thenReturn(Optional.of(sale));
        mockCurrentUser(UUID.randomUUID(), UserRole.ROLE_ADMIN);

        saleHistoryService.cancelSale(saleId);

        verify(advertisementRepository).save(advertisement);
        assertThat(advertisement.getQuantity()).isEqualTo(7);
        verify(saleHistoryRepository).delete(sale);
    }

    @Test
    void cancelSale_whenNotAuthorized_thenShouldThrowAccessDeniedException() {
        UUID saleId = UUID.randomUUID();
        SaleHistory sale = new SaleHistory(advertisement, userId, 2, LocalDateTime.now());
        sale.setId(saleId);

        when(saleHistoryRepository.findById(saleId)).thenReturn(Optional.of(sale));
        mockCurrentUser(UUID.randomUUID(), UserRole.ROLE_USER);

        assertThrows(AccessDeniedException.class, () -> saleHistoryService.cancelSale(saleId));
    }

    @Test
    void cancelSale_whenSaleNotFound_thenShouldThrowSaleHistoryNotFoundException() {
        UUID saleId = UUID.randomUUID();

        when(saleHistoryRepository.findById(saleId)).thenReturn(Optional.empty());

        assertThrows(SaleHistoryNotFoundException.class, () -> saleHistoryService.cancelSale(saleId));
    }

    @Test
    void getSalesByAdvertisementId_shouldThrow_whenAdvertisementNotFound() {
        when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleHistoryService.getSalesByAdvertisementId(advertisementId))
                .isInstanceOf(AdvertisementNotFoundException.class)
                .hasMessageContaining(advertisementId.toString());
    }

    @Test
    void getSalesByAdvertisementId_shouldThrow_whenUserNotOwnerAndNotAdmin() {
        mockCurrentUser(UUID.randomUUID(), UserRole.ROLE_USER);

        when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.of(advertisement));

        assertThatThrownBy(() -> saleHistoryService.getSalesByAdvertisementId(advertisementId))
                .isInstanceOf(SaleHistoryAccessDeniedException.class);
    }

    @Test
    void getSalesByAdvertisementId_shouldReturnSales_whenOwner() {

        List<SaleHistoryResponseDto> expected = List.of();
        mockCurrentUser(owner.getId(), UserRole.ROLE_USER);

        when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.of(advertisement));
        when(saleHistoryRepository.getSalesByAdvertisementId(advertisementId)).thenReturn(expected);

        List<SaleHistoryResponseDto> result = saleHistoryService.getSalesByAdvertisementId(advertisementId);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getSalesByAdvertisementId_shouldReturnSales_whenAdmin() {
        mockCurrentUser(UUID.randomUUID(), UserRole.ROLE_ADMIN);

        List<SaleHistoryResponseDto> expected = List.of();

        when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.of(advertisement));
        when(saleHistoryRepository.getSalesByAdvertisementId(advertisementId)).thenReturn(expected);

        List<SaleHistoryResponseDto> result = saleHistoryService.getSalesByAdvertisementId(advertisementId);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getTotalForSalesInAdvertisements_shouldReturnAggregatedSales() {
        mockCurrentUser(owner.getId(), UserRole.ROLE_USER);

        List<SaleHistoryTotalForAdvertisementsResponseDto> expected = List.of();

        when(saleHistoryRepository.getTotalForSalesInAdvertisements(owner.getId())).thenReturn(expected);

        List<SaleHistoryTotalForAdvertisementsResponseDto> result = saleHistoryService.getTotalForSalesInAdvertisements();

        assertThat(result).isEqualTo(expected);
    }

    private void mockCurrentUser(UUID id, UserRole role) {
        UserPrincipal principal = new UserPrincipal(id, "First", "Last", "123", "user", "pass", role);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
