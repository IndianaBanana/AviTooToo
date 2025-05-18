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
import org.banana.exception.SaleHistoryUnexpectedException;
import org.banana.repository.AdvertisementRepository;
import org.banana.repository.SaleHistoryRepository;
import org.banana.repository.UserRepository;
import org.banana.security.UserRole;
import org.banana.security.dto.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private UserPrincipal principal;

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

        principal = new UserPrincipal(userId, "First", "Last", "123", "user", "pass", UserRole.ROLE_USER);
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
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
    void addSale_whenAdvertisementIsAlreadyClosed_thenShouldThrowSaleHistoryAdvertisementAlreadyClosedException() {
        advertisement.setCloseDate(LocalDateTime.now());
        when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.of(advertisement));

        SaleHistoryAddRequestDto dto = new SaleHistoryAddRequestDto(advertisementId, 1);

        assertThatThrownBy(() -> saleHistoryService.addSale(dto))
                .isInstanceOf(AdvertisementNotFoundException.class);
    }

    @Test
    void addSale_whenValidRequestAndUpdateQuantityIsSuccessful_thenShouldReturnSaleHistoryResponseDto() {
        SaleHistory saleHistory = new SaleHistory(advertisement, userId, 2, LocalDateTime.now());
        ArgumentCaptor<SaleHistory> historyArgumentCaptor = ArgumentCaptor.forClass(SaleHistory.class);
        SaleHistoryAddRequestDto dto = new SaleHistoryAddRequestDto(advertisementId, 2);
        advertisement.setQuantity(5);
        advertisement.setCloseDate(null);

        when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.of(advertisement));
        when(saleHistoryRepository.save(any(SaleHistory.class))).thenReturn(saleHistory);
        when(advertisementRepository.updateAdvertisementQuantity(
                advertisementId,
                advertisement.getQuantity(),
                advertisement.getQuantity() - dto.getQuantity()))
                .thenReturn(1);
        when(saleHistoryMapper.fromSaleHistoryToSaleHistoryResponseDto(saleHistory)).thenReturn(mock(SaleHistoryResponseDto.class));

        SaleHistoryResponseDto response = saleHistoryService.addSale(dto);

        verify(saleHistoryRepository).save(historyArgumentCaptor.capture());
        assertThat(historyArgumentCaptor.getValue()).extracting("advertisement", "buyerId", "quantity")
                .contains(advertisement, userId, 2);
        assertThat(response).isNotNull();
    }

    @Test
    void addSale_whenUpdateAdvertisementQuantityUnsuccessful_thenShouldThrowAdvertisementNotFoundException() {
        SaleHistory saleHistory = new SaleHistory(advertisement, userId, 2, LocalDateTime.now());
        ArgumentCaptor<SaleHistory> historyArgumentCaptor = ArgumentCaptor.forClass(SaleHistory.class);
        SaleHistoryAddRequestDto dto = new SaleHistoryAddRequestDto(advertisementId, 2);

        when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.of(advertisement));
        when(advertisementRepository.updateAdvertisementQuantity(
                advertisementId,
                advertisement.getQuantity(),
                advertisement.getQuantity() - dto.getQuantity()))
                .thenReturn(0);

        assertThatThrownBy(() -> saleHistoryService.addSale(dto)).isInstanceOf(SaleHistoryUnexpectedException.class);
    }

    @ParameterizedTest
    @CsvSource({"ROLE_USER", "ROLE_ADMIN"})
    void deleteSale_whenAllValidAndUpdateQuantityIsSuccessful_thenDeletesAndRestoresQuantity(UserRole role) {
        UUID saleId = UUID.randomUUID();
        advertisement.setQuantity(5);
        advertisement.setCloseDate(null);
        principal.setId(UserRole.ROLE_USER.equals(role) ? owner.getId() : UUID.randomUUID());
        principal.setRole(role);
        SaleHistory sale = new SaleHistory(advertisement, userId, 2, LocalDateTime.now());
        sale.setId(saleId);

        when(saleHistoryRepository.findById(saleId)).thenReturn(Optional.of(sale));

        when(advertisementRepository.updateAdvertisementQuantity(
                advertisementId,
                advertisement.getQuantity(),
                advertisement.getQuantity() + sale.getQuantity()))
                .thenReturn(1);

        saleHistoryService.deleteSale(saleId);

        verify(saleHistoryRepository).delete(sale);
    }

    @Test
    void deleteSale_whenUpdateAdvertisementQuantityUnsuccessful_thenShouldThrowSaleHistoryUnexpectedException() {
        UUID saleId = UUID.randomUUID();
        advertisement.setQuantity(5);
        advertisement.setCloseDate(null);
        SaleHistory sale = new SaleHistory(advertisement, userId, 2, LocalDateTime.now());
        principal.setId(owner.getId());
        sale.setId(saleId);

        when(saleHistoryRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.of(advertisement));

        when(advertisementRepository.updateAdvertisementQuantity(
                advertisementId,
                advertisement.getQuantity(),
                advertisement.getQuantity() + sale.getQuantity()))
                .thenReturn(0);

        assertThatThrownBy(() -> saleHistoryService.deleteSale(saleId))
                .isInstanceOf(SaleHistoryUnexpectedException.class);
    }

    @Test
    void deleteSale_whenNotAuthorized_thenShouldThrowAccessDeniedException() {
        UUID saleId = UUID.randomUUID();
        SaleHistory sale = new SaleHistory(advertisement, userId, 2, LocalDateTime.now());
        sale.setId(saleId);
        principal.setId(UUID.randomUUID());

        when(saleHistoryRepository.findById(saleId)).thenReturn(Optional.of(sale));

        assertThrows(SaleHistoryAccessDeniedException.class, () -> saleHistoryService.deleteSale(saleId));
    }

    @Test
    void deleteSale_whenSaleNotFound_thenShouldThrowSaleHistoryNotFoundException() {
        UUID saleId = UUID.randomUUID();

        when(saleHistoryRepository.findById(saleId)).thenReturn(Optional.empty());

        assertThrows(SaleHistoryNotFoundException.class, () -> saleHistoryService.deleteSale(saleId));
    }

    @Test
    void deleteSale_whenAdvertisementClosed_thenShouldThrowAdvertisementNotFoundException() {
        UUID saleId = UUID.randomUUID();
        principal.setId(owner.getId());
        SaleHistory sale = new SaleHistory(advertisement, userId, 2, LocalDateTime.now());
        sale.setId(saleId);
        advertisement.setCloseDate(LocalDateTime.now());

        when(saleHistoryRepository.findById(saleId)).thenReturn(Optional.of(sale));

        assertThrows(AdvertisementNotFoundException.class, () -> saleHistoryService.deleteSale(saleId));
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
        principal.setId(UUID.randomUUID());

        when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.of(advertisement));

        assertThatThrownBy(() -> saleHistoryService.getSalesByAdvertisementId(advertisementId))
                .isInstanceOf(SaleHistoryAccessDeniedException.class);
    }

    @ParameterizedTest
    @CsvSource({"ROLE_USER", "ROLE_ADMIN"})
    void getSalesByAdvertisementId_shouldReturnSales_whenOwnerOrAdmin(UserRole role) {

        List<SaleHistoryResponseDto> expected = List.of();
        principal.setId(UserRole.ROLE_USER.equals(role) ? owner.getId() : UUID.randomUUID());
        principal.setRole(role);

        when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.of(advertisement));
        when(saleHistoryRepository.getSalesByAdvertisementId(advertisementId)).thenReturn(expected);

        List<SaleHistoryResponseDto> result = saleHistoryService.getSalesByAdvertisementId(advertisementId);

        assertThat(result).isEqualTo(expected);
    }


    @Test
    void getTotalForSalesInAdvertisements_shouldReturnAggregatedSales() {
        principal.setId(owner.getId());

        List<SaleHistoryTotalForAdvertisementsResponseDto> expected = List.of();

        when(saleHistoryRepository.getTotalForSalesInAdvertisements(owner.getId())).thenReturn(expected);

        List<SaleHistoryTotalForAdvertisementsResponseDto> result = saleHistoryService.getTotalForSalesInAdvertisements();

        assertThat(result).isEqualTo(expected);
    }

//    private void mockCurrentUser(UUID id, UserRole role) {
//        principal = new UserPrincipal(id, "First", "Last", "123", "user", "pass", role);
//        Authentication auth = mock(Authentication.class);
//        when(auth.getPrincipal()).thenReturn(principal);
//        SecurityContextHolder.getContext().setAuthentication(auth);
//    }
}
