package org.banana.service;

import org.banana.dto.advertisement.AdvertisementFilterDto;
import org.banana.dto.advertisement.AdvertisementMapper;
import org.banana.dto.advertisement.AdvertisementRequestDto;
import org.banana.dto.advertisement.AdvertisementResponseDto;
import org.banana.dto.advertisement.AdvertisementUpdateRequestDto;
import org.banana.dto.user.UserResponseDto;
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
import org.junit.jupiter.api.Nested;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.banana.exception.AdvertisementUpdateException.AdvertisementUpdateExceptionMessage.NOT_OWNER;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
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
    void findAllFiltered_whenSearchParamEscaping_thenReturnsList() {
        AdvertisementFilterDto filter = new AdvertisementFilterDto();
        filter.setSearchParam("100%_test\\value");
        List<AdvertisementResponseDto> dtos = List.of(new AdvertisementResponseDto());
        when(advertisementRepository.findAllFiltered(filter, 1, 10)).thenReturn(dtos);

        List<AdvertisementResponseDto> result = advertisementService.findAllFiltered(filter, 1, 10);

        assertEquals(dtos, result);
        assertEquals("100\\%\\_test\\\\value", filter.getSearchParam());
    }

//    @Test
//    void findAllFiltered_whenNoSearchParam_thenReturnsList() {
//        AdvertisementFilterDto filter = new AdvertisementFilterDto();
//        filter.setSearchParam(null);
//        List<AdvertisementResponseDto> dtos = Collections.emptyList();
//        when(advertisementRepository.findAllFiltered(filter, 0, 5)).thenReturn(dtos);
//
//        List<AdvertisementResponseDto> result = service.findAllFiltered(filter, 0, 5);
//
//        assertSame(dtos, result);
//    }

    // -------- deleteById --------

    @Test
    void deleteById_whenOwner_thenDeletes() {
        UUID id = UUID.randomUUID();
        Advertisement ad = new Advertisement();
        User owner = new User();
        owner.setId(userId);
        ad.setUser(owner);
        when(advertisementRepository.findById(id)).thenReturn(Optional.of(ad));

        advertisementService.deleteById(id);

        verify(advertisementRepository).delete(ad);
    }

    @Test
    void deleteById_whenAdmin_thenDeletes() {
        principal.setRole(UserRole.ROLE_ADMIN);
        UUID id = UUID.randomUUID();
        Advertisement ad = new Advertisement();
        User owner = new User();
        owner.setId(UUID.randomUUID());
        ad.setUser(owner);
        when(advertisementRepository.findById(id)).thenReturn(Optional.of(ad));

        advertisementService.deleteById(id);

        verify(advertisementRepository).delete(ad);
    }

    @Test
    void deleteById_whenNotOwnerAndNotAdmin_thenShouldThrowAdvertisementUpdateException() {
        UUID id = UUID.randomUUID();
        Advertisement ad = new Advertisement();
        User owner = new User();
        owner.setId(UUID.randomUUID());
        ad.setUser(owner);
        when(advertisementRepository.findById(id)).thenReturn(Optional.of(ad));

        AdvertisementUpdateException ex = assertThrows(AdvertisementUpdateException.class, () -> advertisementService.deleteById(id));
        assertTrue(ex.getMessage().contains(NOT_OWNER.getDescription()));
    }

    @Test
    void deleteById_whenNotFound_thenShouldThrowAdvertisementNotFoundException() {
        UUID id = UUID.randomUUID();
        when(advertisementRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(AdvertisementNotFoundException.class, () -> advertisementService.deleteById(id));
    }

    // -------- createAdvertisement --------

    @Test
    void createAdvertisement_whenAllValid_thenReturnsDto() {
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

        AdvertisementResponseDto result = advertisementService.createAdvertisement(req);
        Advertisement capturedAd = captor.getValue();
        assertEquals(type, capturedAd.getAdvertisementType());
        assertEquals(city, capturedAd.getCity());
        assertEquals(user, capturedAd.getUser());
        assertSame(dto, result);
    }

    @Test
    void createAdvertisement_whenTypeNotFound_thenShouldThrowAdvertisementTypeNotFoundException() {
        AdvertisementRequestDto req = new AdvertisementRequestDto();
        when(advertisementTypeRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(AdvertisementTypeNotFoundException.class, () -> advertisementService.createAdvertisement(req));
    }

    @Test
    void createAdvertisement_whenCityNotFound_thenShouldThrowCityNotFoundException() {
        AdvertisementRequestDto req = new AdvertisementRequestDto();

        when(advertisementTypeRepository.findById(any()))
                .thenReturn(Optional.of(new AdvertisementType()));
        when(cityRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(CityNotFoundException.class, () -> advertisementService.createAdvertisement(req));
    }

    @Test
    void createAdvertisement_whenUserNotFound_thenShouldThrowUserNotFoundException() {
        AdvertisementRequestDto req = new AdvertisementRequestDto();

        when(advertisementTypeRepository.findById(any()))
                .thenReturn(Optional.of(new AdvertisementType()));
        when(cityRepository.findById(any()))
                .thenReturn(Optional.of(new City()));
        when(userRepository.findFetchedById(any())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> advertisementService.createAdvertisement(req));
    }

    // -------- updateAdvertisement --------

//    @Test
//    void updateAdvertisement_whenAllValid_thenReturnsDto() {
//        UUID adId = UUID.randomUUID();
//        AdvertisementUpdateRequestDto req = new AdvertisementUpdateRequestDto();
//        req.setAdvertisementId(adId);
//        req.setTitle("new");
//        req.setDescription("desc");
//        req.setCityId(null);
//        req.setAdvertisementTypeId(null);
//        req.setPrice(BigDecimal.TEN);
//        req.setQuantity(5);
//
//        Advertisement ad = new Advertisement();
//        ad.setId(adId);
//        ad.setUser(new User(userId, "F", "L", "p", "u", "pass", UserRole.ROLE_USER));
//        ad.setCity(new City("old"));
//        ad.getCity().setId(UUID.randomUUID());
//        ad.setAdvertisementType(new AdvertisementType("old"));
//        ad.getAdvertisementType().setId(UUID.randomUUID());
//
//        AdvertisementResponseDto dto = new AdvertisementResponseDto();
//        when(advertisementRepository.findById(adId)).thenReturn(Optional.of(ad));
//        when(advertisementRepository.save(ad)).thenReturn(ad);
//        when(advertisementMapper.advertisementToAdvertisementResponseDto(ad)).thenReturn(dto);
//
//        AdvertisementResponseDto result = advertisementService.updateAdvertisement(req);
//        assertSame(dto, result);
//        assertEquals("new", ad.getTitle());
//        assertEquals("desc", ad.getDescription());
//        assertEquals(BigDecimal.TEN, ad.getPrice());
//        assertEquals(5, ad.getQuantity());
//    }
//
//    @Test
//    void updateAdvertisement_whenNotFound_thenShouldThrowAdvertisementNotFoundException() {
//        AdvertisementUpdateRequestDto req = new AdvertisementUpdateRequestDto();
//        req.setAdvertisementId(UUID.randomUUID());
//        when(advertisementRepository.findById(req.getAdvertisementId()))
//                .thenReturn(Optional.empty());
//
//        assertThrows(AdvertisementNotFoundException.class,
//                () -> advertisementService.updateAdvertisement(req));
//    }
//
//    @Test
//    void updateAdvertisement_whenAlreadyClosed_thenShouldThrowAdvertisementUpdateException() {
//        UUID adId = UUID.randomUUID();
//        AdvertisementUpdateRequestDto req = new AdvertisementUpdateRequestDto();
//        req.setAdvertisementId(adId);
//        Advertisement ad = new Advertisement();
//        ad.setId(adId);
//        ad.setCloseDate(LocalDateTime.now());
//        ad.setUser(new User(userId, "F", "L", "p", "u", "pass", UserRole.ROLE_USER));
//
//        when(advertisementRepository.findById(adId)).thenReturn(Optional.of(ad));
//
//        AdvertisementUpdateException ex = assertThrows(
//                AdvertisementUpdateException.class,
//                () -> advertisementService.updateAdvertisement(req)
//        );
//        assertTrue(ex.getMessage().contains("Advertisement is already closed"));
//    }
//
//    @Test
//    void updateAdvertisement_whenNotOwner_thenShouldThrowAdvertisementUpdateException() {
//        UUID adId = UUID.randomUUID();
//        AdvertisementUpdateRequestDto req = new AdvertisementUpdateRequestDto();
//        req.setAdvertisementId(adId);
//        Advertisement ad = new Advertisement();
//        ad.setId(adId);
//        ad.setUser(new User(UUID.randomUUID(), "F", "L", "p", "u", "pass", UserRole.ROLE_USER));
//        ad.setCloseDate(null);
//        when(advertisementRepository.findById(adId)).thenReturn(Optional.of(ad));
//
//        AdvertisementUpdateException ex = assertThrows(
//                AdvertisementUpdateException.class,
//                () -> advertisementService.updateAdvertisement(req)
//        );
//        assertTrue(ex.getMessage().contains("User is not the owner"));
//    }
//
//    @Test
//    void updateAdvertisement_whenCityNotFound_thenShouldThrowCityNotFoundException() {
//        UUID adId = UUID.randomUUID();
//        UUID newCityId = UUID.randomUUID();
//        AdvertisementUpdateRequestDto req = new AdvertisementUpdateRequestDto();
//        req.setAdvertisementId(adId);
//        req.setCityId(newCityId);
//
//        Advertisement ad = new Advertisement();
//        ad.setId(adId);
//        ad.setUser(new User(userId, "F", "L", "p", "u", "pass", UserRole.ROLE_USER));
//        ad.setCloseDate(null);
//        ad.setCity(new City("old"));
//        ad.getCity().setId(UUID.randomUUID());
//
//        when(advertisementRepository.findById(adId)).thenReturn(Optional.of(ad));
//        when(cityRepository.findById(newCityId)).thenReturn(Optional.empty());
//
//        assertThrows(CityNotFoundException.class,
//                () -> advertisementService.updateAdvertisement(req));
//    }
//
//    @Test
//    void updateAdvertisement_whenTypeNotFound_thenShouldThrowAdvertisementTypeNotFoundException() {
//        UUID adId = UUID.randomUUID();
//        UUID newTypeId = UUID.randomUUID();
//        AdvertisementUpdateRequestDto req = new AdvertisementUpdateRequestDto();
//        req.setAdvertisementId(adId);
//        req.setAdvertisementTypeId(newTypeId);
//
//        Advertisement ad = new Advertisement();
//        ad.setId(adId);
//        ad.setUser(new User(userId, "F", "L", "p", "u", "pass", UserRole.ROLE_USER));
//        ad.setCloseDate(null);
//        ad.setAdvertisementType(new AdvertisementType("old"));
//        ad.getAdvertisementType().setId(UUID.randomUUID());
//
//        when(advertisementRepository.findById(adId)).thenReturn(Optional.of(ad));
//        when(advertisementTypeRepository.findById(newTypeId)).thenReturn(Optional.empty());
//
//        assertThrows(AdvertisementTypeNotFoundException.class,
//                () -> advertisementService.updateAdvertisement(req));
//    }

    // -------- closeAdvertisement --------

    @Test
    void closeAdvertisement_whenOwnerAndOpen_thenReturnsDto() {
        UUID adId = UUID.randomUUID();
        AdvertisementResponseDto dto = new AdvertisementResponseDto();
        dto.setId(adId);
        UserResponseDto ur = new UserResponseDto();
        ur.setId(userId);
        dto.setUserResponseDto(ur);
        dto.setCloseDate(null);

        when(advertisementRepository.findDtoById(adId)).thenReturn(Optional.of(dto));

        AdvertisementResponseDto result = advertisementService.closeAdvertisement(adId);

        assertNotNull(result.getCloseDate());
        verify(advertisementRepository).closeAdvertisement(eq(adId), any());
    }

    @Test
    void closeAdvertisement_whenAdminAndOpen_thenReturnsDto() {
        principal.setRole(UserRole.ROLE_ADMIN);
        UUID adId = UUID.randomUUID();
        AdvertisementResponseDto dto = new AdvertisementResponseDto();
        dto.setId(adId);
        UserResponseDto ur = new UserResponseDto();
        ur.setId(UUID.randomUUID());
        dto.setUserResponseDto(ur);
        dto.setCloseDate(null);

        when(advertisementRepository.findDtoById(adId)).thenReturn(Optional.of(dto));

        AdvertisementResponseDto result = advertisementService.closeAdvertisement(adId);

        assertNotNull(result.getCloseDate());
        verify(advertisementRepository).closeAdvertisement(eq(adId), any());
    }

    @Test
    void closeAdvertisement_whenNotFound_thenShouldThrowAdvertisementNotFoundException() {
        UUID adId = UUID.randomUUID();
        when(advertisementRepository.findDtoById(adId)).thenReturn(Optional.empty());

        assertThrows(AdvertisementNotFoundException.class,
                () -> advertisementService.closeAdvertisement(adId));
    }

    @Test
    void closeAdvertisement_whenAlreadyClosed_thenShouldThrowAdvertisementUpdateException() {
        UUID adId = UUID.randomUUID();
        AdvertisementResponseDto dto = new AdvertisementResponseDto();
        dto.setId(adId);
        dto.setCloseDate(LocalDateTime.now());
        when(advertisementRepository.findDtoById(adId)).thenReturn(Optional.of(dto));

        AdvertisementUpdateException ex = assertThrows(
                AdvertisementUpdateException.class,
                () -> advertisementService.closeAdvertisement(adId)
        );
        assertTrue(ex.getMessage().contains("Advertisement is already closed"));
    }

    @Test
    void closeAdvertisement_whenNotOwner_thenShouldThrowAdvertisementUpdateException() {
        UUID adId = UUID.randomUUID();
        AdvertisementResponseDto dto = new AdvertisementResponseDto();
        dto.setId(adId);
        UserResponseDto ur = new UserResponseDto();
        ur.setId(UUID.randomUUID());
        dto.setUserResponseDto(ur);
        dto.setCloseDate(null);

        when(advertisementRepository.findDtoById(adId)).thenReturn(Optional.of(dto));

        AdvertisementUpdateException ex = assertThrows(
                AdvertisementUpdateException.class,
                () -> advertisementService.closeAdvertisement(adId)
        );
        assertTrue(ex.getMessage().contains("User is not the owner"));
    }

    // -------- promoteAdvertisement --------

    @Test
    void promoteAdvertisement_whenOwnerAndNotPromotedAndOpen_thenReturnsDto() {
        UUID adId = UUID.randomUUID();
        AdvertisementResponseDto dto = new AdvertisementResponseDto();
        dto.setId(adId);
        dto.setPromoted(false);
        dto.setCloseDate(null);
        UserResponseDto ur = new UserResponseDto();
        ur.setId(userId);
        dto.setUserResponseDto(ur);

        when(advertisementRepository.findDtoById(adId)).thenReturn(Optional.of(dto));

        AdvertisementResponseDto result = advertisementService.promoteAdvertisement(adId);

        assertTrue(result.isPromoted());
        verify(advertisementRepository).promoteAdvertisement(adId);
    }

    @Test
    void promoteAdvertisement_whenNotFound_thenShouldThrowAdvertisementNotFoundException() {
        UUID adId = UUID.randomUUID();
        when(advertisementRepository.findDtoById(adId)).thenReturn(Optional.empty());

        assertThrows(AdvertisementNotFoundException.class,
                () -> advertisementService.promoteAdvertisement(adId));
    }

    @Test
    void promoteAdvertisement_whenAlreadyPromoted_thenShouldThrowAdvertisementUpdateException() {
        UUID adId = UUID.randomUUID();
        AdvertisementResponseDto dto = new AdvertisementResponseDto();
        dto.setId(adId);
        dto.setPromoted(true);
        dto.setCloseDate(null);
        UserResponseDto ur = new UserResponseDto();
        ur.setId(userId);
        dto.setUserResponseDto(ur);

        when(advertisementRepository.findDtoById(adId)).thenReturn(Optional.of(dto));

        AdvertisementUpdateException ex = assertThrows(
                AdvertisementUpdateException.class,
                () -> advertisementService.promoteAdvertisement(adId)
        );
        assertTrue(ex.getMessage().contains("Advertisement is already promoted"));
    }

    @Test
    void promoteAdvertisement_whenAlreadyClosed_thenShouldThrowAdvertisementUpdateException() {
        UUID adId = UUID.randomUUID();
        AdvertisementResponseDto dto = new AdvertisementResponseDto();
        dto.setId(adId);
        dto.setPromoted(false);
        dto.setCloseDate(LocalDateTime.now());
        UserResponseDto ur = new UserResponseDto();
        ur.setId(userId);
        dto.setUserResponseDto(ur);

        when(advertisementRepository.findDtoById(adId)).thenReturn(Optional.of(dto));

        AdvertisementUpdateException ex = assertThrows(
                AdvertisementUpdateException.class,
                () -> advertisementService.promoteAdvertisement(adId)
        );
        assertTrue(ex.getMessage().contains("Advertisement is already closed"));
    }

    @Test
    void promoteAdvertisement_whenNotOwner_thenShouldThrowAdvertisementUpdateException() {
        UUID adId = UUID.randomUUID();
        AdvertisementResponseDto dto = new AdvertisementResponseDto();
        dto.setId(adId);
        dto.setPromoted(false);
        dto.setCloseDate(null);
        UserResponseDto ur = new UserResponseDto();
        ur.setId(UUID.randomUUID());
        dto.setUserResponseDto(ur);

        when(advertisementRepository.findDtoById(adId)).thenReturn(Optional.of(dto));

        AdvertisementUpdateException ex = assertThrows(
                AdvertisementUpdateException.class,
                () -> advertisementService.promoteAdvertisement(adId)
        );
        assertTrue(ex.getMessage().contains("User is not the owner"));
    }

    @Nested
    class UpdateAdvertisementTests {
        private final UUID ownerId = UUID.randomUUID();
        private final UUID nonOwnerId = UUID.randomUUID();
        private final UUID advertisementId = UUID.randomUUID();
        private final UUID existingCityId = UUID.randomUUID();
        private final UUID nonExistingCityId = UUID.randomUUID();
        private final UUID existingTypeId = UUID.randomUUID();
        private final UUID nonExistingTypeId = UUID.randomUUID();

        private Advertisement advertisement;
        private AdvertisementUpdateRequestDto requestDto;

        @BeforeEach
        void setup() {
            User owner = new User(ownerId, "Owner", "User", "phone", "owner@mail.com", "pass", UserRole.ROLE_USER);
            City city = new City(existingCityId, "Old City");
            AdvertisementType type = new AdvertisementType(existingTypeId, "Old Type");

            advertisement = new Advertisement();
            advertisement.setId(advertisementId);
            advertisement.setUser(owner);
            advertisement.setCity(city);
            advertisement.setAdvertisementType(type);
            advertisement.setTitle("Old Title");
            advertisement.setDescription("Old Description");
            advertisement.setPrice(BigDecimal.valueOf(100));
            advertisement.setQuantity(5);

            requestDto = new AdvertisementUpdateRequestDto();
            requestDto.setAdvertisementId(advertisementId);

            // Setup security context for owner by default
            setupSecurityContext(ownerId);
        }

        // Positive tests
        @Test
        void updateAdvertisement_whenAllFieldsValid_thenUpdateAllFields() {
            // Prepare
            requestDto.setTitle("New Title");
            requestDto.setDescription("New Description");
            requestDto.setCityId(existingCityId);
            requestDto.setAdvertisementTypeId(existingTypeId);
            requestDto.setPrice(BigDecimal.valueOf(200));
            requestDto.setQuantity(10);

            when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.of(advertisement));
//            when(cityRepository.findById(existingCityId)).thenReturn(Optional.of(new City(existingCityId, "New City")));
            when(advertisementTypeRepository.findById(existingTypeId)).thenReturn(Optional.of(new AdvertisementType(existingTypeId, "New Type")));
            when(advertisementRepository.save(any())).thenReturn(advertisement);

            // Execute
            AdvertisementResponseDto result = advertisementService.updateAdvertisement(requestDto);

            // Verify
            assertAll(
                    () -> assertEquals("New Title", advertisement.getTitle()),
                    () -> assertEquals("New Description", advertisement.getDescription()),
                    () -> assertEquals(existingCityId, advertisement.getCity().getId()),
                    () -> assertEquals(existingTypeId, advertisement.getAdvertisementType().getId()),
                    () -> assertEquals(200, advertisement.getPrice().intValue()),
                    () -> assertEquals(10, advertisement.getQuantity())
            );
        }

        @Test
        void updateAdvertisement_whenUpdateTitle_thenTitleChanged() {
            requestDto.setTitle("New Title");

            when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.of(advertisement));
            when(advertisementRepository.save(any())).thenReturn(advertisement);

            advertisementService.updateAdvertisement(requestDto);

            assertEquals("New Title", advertisement.getTitle());
        }

        @Test
        void updateAdvertisement_whenUpdateDescription_thenDescriptionChanged() {
            requestDto.setDescription("New Description");

            when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.of(advertisement));
            when(advertisementRepository.save(any())).thenReturn(advertisement);

            advertisementService.updateAdvertisement(requestDto);

            assertEquals("New Description", advertisement.getDescription());
        }

        @Test
        void updateAdvertisement_whenUpdateCity_thenCityChanged() {
            City newCity = new City(existingCityId, "New City");
            requestDto.setCityId(existingCityId);

            when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.of(advertisement));
            when(advertisementRepository.save(any())).thenReturn(advertisement);

            advertisementService.updateAdvertisement(requestDto);

            assertEquals(newCity, advertisement.getCity());
        }

        @Test
        void updateAdvertisement_whenUpdateType_thenTypeChanged() {
            AdvertisementType newType = new AdvertisementType(existingTypeId, "New Type");
            requestDto.setAdvertisementTypeId(existingTypeId);

            when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.of(advertisement));
            when(advertisementTypeRepository.findById(existingTypeId)).thenReturn(Optional.of(newType));
            when(advertisementRepository.save(any())).thenReturn(advertisement);

            advertisementService.updateAdvertisement(requestDto);

            assertEquals(newType, advertisement.getAdvertisementType());
        }

        @Test
        void updateAdvertisement_whenUpdatePrice_thenPriceChanged() {
            requestDto.setPrice(BigDecimal.valueOf(150));

            when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.of(advertisement));
            when(advertisementRepository.save(any())).thenReturn(advertisement);

            advertisementService.updateAdvertisement(requestDto);

            assertEquals(150, advertisement.getPrice().intValue());
        }

        @Test
        void updateAdvertisement_whenUpdateQuantity_thenQuantityChanged() {
            requestDto.setQuantity(8);

            when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.of(advertisement));
            when(advertisementRepository.save(any())).thenReturn(advertisement);

            advertisementService.updateAdvertisement(requestDto);

            assertEquals(8, advertisement.getQuantity());
        }

        // Negative tests
        @Test
        void updateAdvertisement_whenAdvertisementClosed_thenThrowAdvertisementUpdateException() {
            advertisement.setCloseDate(LocalDateTime.now());

            when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.of(advertisement));

            assertThrows(AdvertisementUpdateException.class,
                    () -> advertisementService.updateAdvertisement(requestDto),
                    "Should throw when updating closed advertisement");
        }

        @Test
        void updateAdvertisement_whenUserNotOwner_thenThrowAdvertisementUpdateException() {
            setupSecurityContext(nonOwnerId);

            when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.of(advertisement));

            assertThrows(AdvertisementUpdateException.class,
                    () -> advertisementService.updateAdvertisement(requestDto),
                    "Should throw when non-owner tries to update");
        }

        @Test
        void updateAdvertisement_whenCityNotFound_thenThrowCityNotFoundException() {
            requestDto.setCityId(nonExistingCityId);

            when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.of(advertisement));
            when(cityRepository.findById(nonExistingCityId)).thenReturn(Optional.empty());

            assertThrows(CityNotFoundException.class,
                    () -> advertisementService.updateAdvertisement(requestDto));
        }

        @Test
        void updateAdvertisement_whenTypeNotFound_thenThrowAdvertisementTypeNotFoundException() {
            requestDto.setAdvertisementTypeId(nonExistingTypeId);

            when(advertisementRepository.findById(advertisementId)).thenReturn(Optional.of(advertisement));
            when(advertisementTypeRepository.findById(nonExistingTypeId)).thenReturn(Optional.empty());

            assertThrows(AdvertisementTypeNotFoundException.class,
                    () -> advertisementService.updateAdvertisement(requestDto));
        }

        private void setupSecurityContext(UUID userId) {
            var principal = new UserPrincipal(
                    userId, "Test", "User", "phone", "test@mail.com", "pass", UserRole.ROLE_USER
            );
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(principal, null, List.of())
            );
        }
    }

}
