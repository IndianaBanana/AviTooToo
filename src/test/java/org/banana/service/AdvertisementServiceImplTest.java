package org.banana.service;

import org.banana.dto.advertisement.AdvertisementFilterDto;
import org.banana.dto.advertisement.AdvertisementMapper;
import org.banana.dto.advertisement.AdvertisementRequestDto;
import org.banana.dto.advertisement.AdvertisementResponseDto;
import org.banana.entity.Advertisement;
import org.banana.entity.AdvertisementType;
import org.banana.entity.City;
import org.banana.entity.User;
import org.banana.exception.AdvertisementNotFoundException;
import org.banana.exception.AdvertisementTypeNotFoundException;
import org.banana.exception.AdvertisementUpdateException;
import org.banana.exception.CityNotFoundException;
import org.banana.exception.UserNotFoundException;
import org.banana.repository.AdvertisementRepository;
import org.banana.repository.AdvertisementTypeRepository;
import org.banana.repository.CityRepository;
import org.banana.repository.UserRepository;
import org.banana.security.UserRole;
import org.banana.security.dto.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.banana.exception.AdvertisementUpdateException.AdvertisementUpdateExceptionMessage.ADVERTISEMENT_CLOSED;
import static org.banana.exception.AdvertisementUpdateException.AdvertisementUpdateExceptionMessage.ADVERTISEMENT_NOT_CLOSED;
import static org.banana.exception.AdvertisementUpdateException.AdvertisementUpdateExceptionMessage.ALREADY_PROMOTED;
import static org.banana.exception.AdvertisementUpdateException.AdvertisementUpdateExceptionMessage.NOT_OWNER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdvertisementServiceImplTest {

    @InjectMocks
    private AdvertisementServiceImpl advertisementService;

    @Mock
    private AdvertisementRepository advertisementRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private AdvertisementTypeRepository advertisementTypeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdvertisementMapper advertisementMapper;

    private UUID userId;
    private UserPrincipal principal;

    @BeforeEach
    void setupSecurityContext() {
        userId = UUID.randomUUID();
        principal = new UserPrincipal(
                userId, "First", "Last", "phone", "user@example.com", "pass", UserRole.ROLE_USER
        );
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // -------- findById --------

    @Test
    void findById_whenExists_thenReturnDto() {
        UUID id = UUID.randomUUID();
        AdvertisementResponseDto dto = new AdvertisementResponseDto();
        when(advertisementRepository.findDtoById(id)).thenReturn(Optional.of(dto));

        AdvertisementResponseDto result = advertisementService.findById(id);

        assertSame(dto, result);
    }

    @Test
    void findById_whenNotFound_thenShouldThrowAdvertisementNotFoundException() {
        UUID id = UUID.randomUUID();
        when(advertisementRepository.findDtoById(id)).thenReturn(Optional.empty());

        AdvertisementNotFoundException ex = assertThrows(AdvertisementNotFoundException.class, () -> advertisementService.findById(id));
        assertTrue(ex.getMessage().contains(id.toString()));
    }

    // -------- findAllFiltered --------

    @Test
    void findAllFiltered_whenSearchParam_thenReturnsList() {
        AdvertisementFilterDto filter = new AdvertisementFilterDto();
        filter.setSearchParam("100%_test\\value");
        List<AdvertisementResponseDto> dtos = List.of(new AdvertisementResponseDto());
        when(advertisementRepository.findAllFiltered(filter, 1, 10)).thenReturn(dtos);

        List<AdvertisementResponseDto> result = advertisementService.findAllFiltered(filter, 1, 10);

        assertEquals(dtos, result);
    }

    @Test
    void findAllFiltered_whenNoSearchParam_thenReturnsList() {
        AdvertisementFilterDto filter = new AdvertisementFilterDto();
        filter.setSearchParam(null);
        List<AdvertisementResponseDto> dtos = Collections.emptyList();
        when(advertisementRepository.findAllFiltered(filter, 0, 5)).thenReturn(dtos);

        List<AdvertisementResponseDto> result = advertisementService.findAllFiltered(filter, 0, 5);

        assertSame(dtos, result);
    }

    // -------- deleteAdvertisement --------

    @Test
    void deleteAdvertisement_whenOwner_thenDeletes() {
        UUID id = UUID.randomUUID();
        Advertisement ad = new Advertisement();
        User owner = new User();
        owner.setId(userId);
        ad.setUser(owner);
        when(advertisementRepository.findById(id)).thenReturn(Optional.of(ad));

        advertisementService.deleteAdvertisement(id);

        verify(advertisementRepository).delete(ad);
    }

    @Test
    void deleteAdvertisement_whenAdmin_thenDeletes() {
        principal.setRole(UserRole.ROLE_ADMIN);
        UUID id = UUID.randomUUID();
        Advertisement ad = new Advertisement();
        User owner = new User();
        owner.setId(UUID.randomUUID());
        ad.setUser(owner);
        when(advertisementRepository.findById(id)).thenReturn(Optional.of(ad));

        advertisementService.deleteAdvertisement(id);

        verify(advertisementRepository).delete(ad);
    }

    @Test
    void deleteAdvertisement_whenNotOwnerAndNotAdmin_thenShouldThrowAdvertisementUpdateException() {
        UUID id = UUID.randomUUID();
        Advertisement ad = new Advertisement();
        User owner = new User();
        owner.setId(UUID.randomUUID());
        ad.setUser(owner);
        when(advertisementRepository.findById(id)).thenReturn(Optional.of(ad));

        AdvertisementUpdateException ex = assertThrows(AdvertisementUpdateException.class, () -> advertisementService.deleteAdvertisement(id));
        assertTrue(ex.getMessage().contains(NOT_OWNER.getDescription()));
    }

    @Test
    void deleteAdvertisement_whenNotFound_thenShouldThrowAdvertisementNotFoundException() {
        UUID id = UUID.randomUUID();
        when(advertisementRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(AdvertisementNotFoundException.class, () -> advertisementService.deleteAdvertisement(id));
    }

    // -------- addAdvertisement --------

    @Test
    void addAdvertisement_whenAllValid_thenReturnsDto() {
        UUID typeId = UUID.randomUUID(), cityId = UUID.randomUUID();
        AdvertisementRequestDto req = new AdvertisementRequestDto(cityId, typeId, "T", "D", BigDecimal.ONE, 1);

        AdvertisementType type = new AdvertisementType(typeId, "t");
        City city = new City(cityId, "c");
        User user = new User(userId, "F", "L", "p", "u", "pass", UserRole.ROLE_USER);
        Advertisement ad = new Advertisement();
        Advertisement saved = new Advertisement();
        AdvertisementResponseDto dto = new AdvertisementResponseDto();
        ArgumentCaptor<Advertisement> captor = ArgumentCaptor.forClass(Advertisement.class);
        when(advertisementTypeRepository.findById(typeId)).thenReturn(Optional.of(type));
        when(cityRepository.findById(cityId)).thenReturn(Optional.of(city));
        when(userRepository.findFetchedById(principal.getId())).thenReturn(Optional.of(user));
        when(advertisementMapper.advertisementRequestDtoToAdvertisement(req)).thenReturn(ad);
        when(advertisementRepository.save(captor.capture())).thenReturn(saved);
        when(advertisementMapper.advertisementToAdvertisementResponseDto(saved)).thenReturn(dto);

        AdvertisementResponseDto result = advertisementService.addAdvertisement(req);
        Advertisement capturedAd = captor.getValue();
        assertEquals(type, capturedAd.getAdvertisementType());
        assertEquals(city, capturedAd.getCity());
        assertEquals(user, capturedAd.getUser());
        assertSame(dto, result);
    }

    @Test
    void addAdvertisement_whenTypeNotFound_thenShouldThrowAdvertisementTypeNotFoundException() {
        AdvertisementRequestDto req = new AdvertisementRequestDto();
        when(advertisementTypeRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(AdvertisementTypeNotFoundException.class, () -> advertisementService.addAdvertisement(req));
    }

    @Test
    void addAdvertisement_whenCityNotFound_thenShouldThrowCityNotFoundException() {
        AdvertisementRequestDto req = new AdvertisementRequestDto();

        when(advertisementTypeRepository.findById(any()))
                .thenReturn(Optional.of(new AdvertisementType()));
        when(cityRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(CityNotFoundException.class, () -> advertisementService.addAdvertisement(req));
    }

    @Test
    void addAdvertisement_whenUserNotFound_thenShouldThrowUserNotFoundException() {
        AdvertisementRequestDto req = new AdvertisementRequestDto();

        when(advertisementTypeRepository.findById(any()))
                .thenReturn(Optional.of(new AdvertisementType()));
        when(cityRepository.findById(any()))
                .thenReturn(Optional.of(new City()));
        when(userRepository.findFetchedById(any())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> advertisementService.addAdvertisement(req));
    }

//  -------- updateAdvertisement --------

    @Test
    void updateAdvertisement_whenValid_thenReturnsDto() {
        UUID adId = UUID.randomUUID();
        AdvertisementRequestDto req = new AdvertisementRequestDto(
                UUID.randomUUID(), UUID.randomUUID(), "NewTitle", "NewDesc", BigDecimal.valueOf(99), 10);

        Advertisement existing = new Advertisement();
        User owner = new User();
        owner.setId(userId);
        existing.setUser(owner);

        City newCity = new City(req.getCityId(), "CityName");
        AdvertisementType newType = new AdvertisementType(req.getAdvertisementTypeId(), "TypeName");
        Advertisement saved = new Advertisement();
        saved.setId(adId);
        AdvertisementResponseDto dto = new AdvertisementResponseDto();
        dto.setId(adId);

        when(advertisementRepository.findById(adId)).thenReturn(Optional.of(existing));
        when(cityRepository.findById(req.getCityId())).thenReturn(Optional.of(newCity));
        when(advertisementTypeRepository.findById(req.getAdvertisementTypeId())).thenReturn(Optional.of(newType));
        when(userRepository.findFetchedById(userId)).thenReturn(Optional.of(new User()));
        when(advertisementRepository.save(any(Advertisement.class))).thenReturn(saved);
        when(advertisementMapper.advertisementToAdvertisementResponseDto(saved)).thenReturn(dto);

        AdvertisementResponseDto result = advertisementService.updateAdvertisement(adId, req);

        assertSame(dto, result);
        ArgumentCaptor<Advertisement> captor = ArgumentCaptor.forClass(Advertisement.class);
        verify(advertisementRepository).save(captor.capture());
        Advertisement toSave = captor.getValue();
        assertEquals(newCity, toSave.getCity());
        assertEquals(newType, toSave.getAdvertisementType());
        assertEquals(req.getTitle(), toSave.getTitle());
        assertEquals(req.getDescription(), toSave.getDescription());
        assertEquals(req.getPrice(), toSave.getPrice());
        assertEquals(req.getQuantity(), toSave.getQuantity());
    }

    @Test
    void updateAdvertisement_whenNotFound_thenThrowsAdvertisementNotFoundException() {
        UUID adId = UUID.randomUUID();
        when(advertisementRepository.findById(adId)).thenReturn(Optional.empty());
        assertThrows(AdvertisementNotFoundException.class,
                () -> advertisementService.updateAdvertisement(adId, new AdvertisementRequestDto()));
    }

    @Test
    void updateAdvertisement_whenNotOwner_thenThrowsAdvertisementUpdateException() {
        UUID adId = UUID.randomUUID();
        Advertisement existing = new Advertisement();
        User other = new User();
        other.setId(UUID.randomUUID());
        existing.setUser(other);
        when(advertisementRepository.findById(adId)).thenReturn(Optional.of(existing));

        assertThrows(AdvertisementUpdateException.class,
                () -> advertisementService.updateAdvertisement(adId, new AdvertisementRequestDto()));
    }

    @Test
    void updateAdvertisement_whenCityNotFound_thenThrowsCityNotFoundException() {
        UUID adId = UUID.randomUUID();
        UUID cityId = UUID.randomUUID();
        UUID typeId = UUID.randomUUID();
        Advertisement existing = new Advertisement();
        User owner = new User();
        owner.setId(userId);
        existing.setUser(owner);
        AdvertisementRequestDto req = new AdvertisementRequestDto();
        req.setCityId(cityId);
        req.setAdvertisementTypeId(typeId);

        when(advertisementTypeRepository.findById(typeId)).thenReturn(Optional.of(new AdvertisementType()));
        when(advertisementRepository.findById(adId)).thenReturn(Optional.of(existing));
        when(cityRepository.findById(cityId)).thenReturn(Optional.empty());

        assertThrows(CityNotFoundException.class, () -> advertisementService.updateAdvertisement(adId, req));
    }

    @Test
    void updateAdvertisement_whenTypeNotFound_thenThrowsAdvertisementTypeNotFoundException() {
        UUID adId = UUID.randomUUID();
        UUID typeId = UUID.randomUUID();
        Advertisement existing = new Advertisement();
        User owner = new User();
        owner.setId(userId);
        existing.setUser(owner);
        AdvertisementRequestDto req = new AdvertisementRequestDto();
        req.setCityId(adId);
        req.setAdvertisementTypeId(typeId);

        when(advertisementRepository.findById(adId)).thenReturn(Optional.of(existing));
        when(advertisementTypeRepository.findById(typeId)).thenReturn(Optional.empty());

        assertThrows(AdvertisementTypeNotFoundException.class,
                () -> advertisementService.updateAdvertisement(adId, req));
    }


    @Test
    void updateAdvertisement_whenAlreadyClosed_thenThrowsAdvertisementUpdateException() {
        UUID adId = UUID.randomUUID();
        Advertisement existing = new Advertisement();
        existing.setCloseDate(LocalDateTime.now());
        User owner = new User();
        owner.setId(userId);
        existing.setUser(owner);

        when(advertisementRepository.findById(adId)).thenReturn(Optional.of(existing));

        AdvertisementUpdateException exception = assertThrows(AdvertisementUpdateException.class,
                () -> advertisementService.updateAdvertisement(adId, new AdvertisementRequestDto()));

        assertTrue(exception.getMessage().contains(ADVERTISEMENT_CLOSED.getDescription()));
    }

    @Test
    void updateAdvertisement_whenUserNotFound_thenThrowsUserNotFoundException() {
        UUID adId = UUID.randomUUID();
        UUID cityId = UUID.randomUUID(), typeId = UUID.randomUUID();
        Advertisement existing = new Advertisement();
        User owner = new User();
        owner.setId(userId);
        existing.setUser(owner);
        AdvertisementRequestDto req = new AdvertisementRequestDto(cityId, typeId, "t", "d", BigDecimal.ZERO, 1);

        when(advertisementRepository.findById(adId)).thenReturn(Optional.of(existing));
        when(cityRepository.findById(cityId)).thenReturn(Optional.of(new City()));
        when(advertisementTypeRepository.findById(typeId)).thenReturn(Optional.of(new AdvertisementType()));
        when(userRepository.findFetchedById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> advertisementService.updateAdvertisement(adId, req));
    }

    // -------- closeAdvertisement --------

    @Test
    void closeAdvertisement_whenOwnerAndOpen_thenReturnsDto() {
        UUID adId = UUID.randomUUID();
        Advertisement advertisement = new Advertisement();
        advertisement.setId(adId);
        User ur = new User();
        ur.setId(userId);
        advertisement.setUser(ur);
        advertisement.setCloseDate(null);

        when(advertisementRepository.findFetchedById(adId)).thenReturn(Optional.of(advertisement));
        when(advertisementRepository.save(advertisement)).thenReturn(advertisement);
        when(advertisementMapper.advertisementToAdvertisementResponseDto(advertisement)).thenReturn(new AdvertisementResponseDto());
        advertisementService.closeAdvertisement(adId);

        assertNotNull(advertisement.getCloseDate());
    }

    @Test
    void closeAdvertisement_whenNotFound_thenShouldThrowAdvertisementNotFoundException() {
        UUID adId = UUID.randomUUID();
        when(advertisementRepository.findFetchedById(adId)).thenReturn(Optional.empty());

        assertThrows(AdvertisementNotFoundException.class, () -> advertisementService.closeAdvertisement(adId));
    }

    @Test
    void closeAdvertisement_whenAlreadyClosed_thenShouldThrowAdvertisementUpdateException() {
        UUID adId = UUID.randomUUID();
        Advertisement advertisement = new Advertisement();
        advertisement.setId(adId);
        User ur = new User();
        ur.setId(userId);
        advertisement.setUser(ur);
        advertisement.setCloseDate(LocalDateTime.now());

        when(advertisementRepository.findFetchedById(adId)).thenReturn(Optional.of(advertisement));

        AdvertisementUpdateException ex = assertThrows(AdvertisementUpdateException.class, () -> advertisementService.closeAdvertisement(adId));
        assertTrue(ex.getMessage().contains(ADVERTISEMENT_CLOSED.getDescription()));
    }

    @Test
    void closeAdvertisement_whenNotOwner_thenShouldThrowAdvertisementUpdateException() {
        UUID adId = UUID.randomUUID();
        Advertisement advertisement = new Advertisement();
        advertisement.setId(adId);
        User user = new User();
        user.setId(UUID.randomUUID());
        advertisement.setUser(user);
        advertisement.setCloseDate(null);

        when(advertisementRepository.findFetchedById(adId)).thenReturn(Optional.of(advertisement));

        AdvertisementUpdateException ex = assertThrows(AdvertisementUpdateException.class, () -> advertisementService.closeAdvertisement(adId));
        assertTrue(ex.getMessage().contains(NOT_OWNER.getDescription()));
    }

    //---------reopenAdvertisement--------

    @Test
    void reopenAdvertisement_whenNotFound_thenShouldThrowAdvertisementNotFoundException() {
        UUID adId = UUID.randomUUID();
        when(advertisementRepository.findFetchedById(adId)).thenReturn(Optional.empty());

        assertThrows(AdvertisementNotFoundException.class, () -> advertisementService.reopenAdvertisement(adId));
    }

    @Test
    void reopenAdvertisement_whenNotOwner_thenShouldThrowAdvertisementUpdateException() {
        UUID adId = UUID.randomUUID();
        Advertisement advertisement = new Advertisement();
        advertisement.setId(adId);
        User user = new User();
        user.setId(UUID.randomUUID());
        advertisement.setUser(user);
        advertisement.setCloseDate(LocalDateTime.now());

        when(advertisementRepository.findFetchedById(adId)).thenReturn(Optional.of(advertisement));

        AdvertisementUpdateException ex = assertThrows(AdvertisementUpdateException.class, () -> advertisementService.reopenAdvertisement(adId));
        assertTrue(ex.getMessage().contains(NOT_OWNER.getDescription()));
    }

    @Test
    void reopenAdvertisement_whenNotClosed_thenShouldThrowAdvertisementUpdateException() {
        UUID adId = UUID.randomUUID();
        Advertisement advertisement = new Advertisement();
        advertisement.setId(adId);
        User ur = new User();
        ur.setId(userId);
        advertisement.setUser(ur);
        advertisement.setCloseDate(null);

        when(advertisementRepository.findFetchedById(adId)).thenReturn(Optional.of(advertisement));

        AdvertisementUpdateException ex = assertThrows(AdvertisementUpdateException.class, () -> advertisementService.reopenAdvertisement(adId));
        assertTrue(ex.getMessage().contains(ADVERTISEMENT_NOT_CLOSED.getDescription()));
    }

    @Test
    void reopenAdvertisement_whenValid_thenReturnsDto() {
        UUID adId = UUID.randomUUID();
        Advertisement advertisement = new Advertisement();
        advertisement.setId(adId);
        User user = new User();
        user.setId(userId);
        advertisement.setUser(user);
        advertisement.setCloseDate(LocalDateTime.now());

        when(advertisementRepository.findFetchedById(adId)).thenReturn(Optional.of(advertisement));
        when(advertisementRepository.save(advertisement)).thenReturn(advertisement);
        when(advertisementMapper.advertisementToAdvertisementResponseDto(advertisement)).thenReturn(new AdvertisementResponseDto());
        advertisementService.reopenAdvertisement(adId);

        assertNull(advertisement.getCloseDate());
    }

    // -------- promoteAdvertisement --------

    @Test
    void promoteAdvertisement_whenOwnerAndNotPromotedAndOpen_thenReturnsDto() {
        UUID adId = UUID.randomUUID();
        Advertisement advertisement = new Advertisement();
        advertisement.setId(adId);
        User user = new User();
        user.setId(userId);
        advertisement.setUser(user);
        advertisement.setIsPromoted(false);
        advertisement.setCloseDate(null);

        when(advertisementRepository.findFetchedById(adId)).thenReturn(Optional.of(advertisement));
        when(advertisementRepository.save(advertisement)).thenReturn(advertisement);
        when(advertisementMapper.advertisementToAdvertisementResponseDto(advertisement)).thenReturn(new AdvertisementResponseDto());
        advertisementService.promoteAdvertisement(adId);

        assertTrue(advertisement.getIsPromoted());
    }

    @Test
    void promoteAdvertisement_whenNotFound_thenShouldThrowAdvertisementNotFoundException() {
        UUID adId = UUID.randomUUID();
        when(advertisementRepository.findFetchedById(adId)).thenReturn(Optional.empty());

        assertThrows(AdvertisementNotFoundException.class,
                () -> advertisementService.promoteAdvertisement(adId));
    }

    @Test
    void promoteAdvertisement_whenAlreadyPromoted_thenShouldThrowAdvertisementUpdateException() {
        UUID adId = UUID.randomUUID();
        Advertisement advertisement = new Advertisement();
        advertisement.setId(adId);
        User user = new User();
        user.setId(userId);
        advertisement.setUser(user);
        advertisement.setCloseDate(null);
        advertisement.setIsPromoted(true);

        when(advertisementRepository.findFetchedById(adId)).thenReturn(Optional.of(advertisement));

        AdvertisementUpdateException ex = assertThrows(
                AdvertisementUpdateException.class,
                () -> advertisementService.promoteAdvertisement(adId)
        );
        assertTrue(ex.getMessage().contains(ALREADY_PROMOTED.getDescription()));
    }

    @Test
    void promoteAdvertisement_whenAlreadyClosed_thenShouldThrowAdvertisementUpdateException() {
        UUID adId = UUID.randomUUID();
        Advertisement advertisement = new Advertisement();
        advertisement.setId(adId);
        User user = new User();
        user.setId(userId);
        advertisement.setUser(user);
        advertisement.setCloseDate(LocalDateTime.now());

        when(advertisementRepository.findFetchedById(adId)).thenReturn(Optional.of(advertisement));

        AdvertisementUpdateException ex = assertThrows(
                AdvertisementUpdateException.class,
                () -> advertisementService.promoteAdvertisement(adId)
        );
        assertTrue(ex.getMessage().contains(ADVERTISEMENT_CLOSED.getDescription()));
    }

    @Test
    void promoteAdvertisement_whenNotOwner_thenShouldThrowAdvertisementUpdateException() {
        UUID adId = UUID.randomUUID();
        Advertisement advertisement = new Advertisement();
        advertisement.setId(adId);
        User user = new User();
        user.setId(UUID.randomUUID());
        advertisement.setUser(user);
        advertisement.setCloseDate(null);

        when(advertisementRepository.findFetchedById(adId)).thenReturn(Optional.of(advertisement));

        AdvertisementUpdateException ex = assertThrows(
                AdvertisementUpdateException.class,
                () -> advertisementService.promoteAdvertisement(adId)
        );
        assertTrue(ex.getMessage().contains(NOT_OWNER.getDescription()));
    }
}
