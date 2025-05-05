package org.banana.repository;

import jakarta.persistence.TypedQuery;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.banana.dto.advertisement.AdvertisementFilterDto;
import org.banana.dto.advertisement.AdvertisementResponseDto;
import org.banana.entity.Advertisement;
import org.banana.repository.crud.AbstractCrudRepositoryImpl;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class AdvertisementRepositoryImpl extends AbstractCrudRepositoryImpl<Advertisement, UUID> implements AdvertisementRepository {

    public AdvertisementRepositoryImpl() {
        super(Advertisement.class);
    }

    @Override
    public List<AdvertisementResponseDto> findAllFiltered(@NotNull AdvertisementFilterDto filter,
                                                          @Min(1) int page,
                                                          @Min(20) int size) {
        Query<AdvertisementResponseDto> query = getAdvertisementResponseDtoTypedQuery(filter);

        if (filter.getCityIds() != null && !filter.getCityIds().isEmpty()) {
            query.setParameter("cityIds", filter.getCityIds());
        }
        if (filter.getAdvertisementTypeIds() != null && !filter.getAdvertisementTypeIds().isEmpty()) {
            query.setParameter("typeIds", filter.getAdvertisementTypeIds());
        }
        if (filter.getSearchParam() != null && !filter.getSearchParam().isBlank()) {
            query.setParameter("search", "%" + filter.getSearchParam().toLowerCase() + "%");
        }
        if (filter.getMinPrice() != null) {
            query.setParameter("minPrice", filter.getMinPrice());
        }
        if (filter.getMaxPrice() != null) {
            query.setParameter("maxPrice", filter.getMaxPrice());
        }

        query.setFirstResult(page * size);
        query.setMaxResults(size);

        return query.getResultList();
    }

    private Query<AdvertisementResponseDto> getAdvertisementResponseDtoTypedQuery(AdvertisementFilterDto filter) {
        System.out.println(findAll());
        StringBuilder jpql = new StringBuilder("""
        SELECT new org.banana.dto.advertisement.AdvertisementResponseDto(
            a.advertisementId,
            new org.banana.dto.user.UserResponseDto(
                u.userId,
                u.firstName,
                u.lastName,
                u.phone,
                u.username,
                ur.averageRating,
                ur.ratingCount
            ),
            c.name,
            at.name,
            a.title,
            a.description,
            a.price,
            a.quantity,
            a.isPaid,
            a.createDate,
            a.closeDate
        )
        FROM Advertisement a
        JOIN a.city c
        JOIN a.advertisementType at
        JOIN a.user u
        LEFT JOIN UserRatingView ur ON ur.userId = u.userId
        WHERE 1=1
        """);

        if (filter.getCityIds() != null && !filter.getCityIds().isEmpty()) {
            jpql.append(" AND a.city.cityId IN :cityIds");
        }
        if (filter.getAdvertisementTypeIds() != null && !filter.getAdvertisementTypeIds().isEmpty()) {
            jpql.append(" AND a.advertisementType.advertisementTypeId IN :typeIds");
        }
        if (filter.getSearchParam() != null && !filter.getSearchParam().isBlank()) {
            System.out.println("Search param: " + filter.getSearchParam());
            jpql.append(" AND (LOWER(a.title) LIKE :search OR LOWER(a.description) LIKE :search)");
        }
        if (filter.getMinPrice() != null) {
            jpql.append(" AND a.price >= :minPrice");
        }
        if (filter.getMaxPrice() != null) {
            jpql.append(" AND a.price <= :maxPrice");
        }

        jpql.append(" ORDER BY a.isPaid DESC, ur.averageRating DESC, ur.ratingCount DESC, a.createDate ASC");

        Query<AdvertisementResponseDto> query = getSession()
                .createQuery(jpql.toString(), AdvertisementResponseDto.class);
        return query;
    }
}
