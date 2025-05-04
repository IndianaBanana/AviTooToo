package org.banana.repository;

import jakarta.persistence.TypedQuery;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.banana.dto.advertisement.AdvertisementFilterDto;
import org.banana.entity.Advertisement;
import org.banana.repository.crud.AbstractCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class AdvertisementRepositoryImpl extends AbstractCrudRepositoryImpl<Advertisement, UUID> implements AdvertisementRepository {

    public AdvertisementRepositoryImpl() {
        super(Advertisement.class);
    }

    @Override
    public List<Advertisement> findAllFiltered(@NotNull AdvertisementFilterDto filter, @Min(value = 1) int page, @Min(value = 20) int size) {
        TypedQuery<Advertisement> query = buildQuery(filter);

        if (filter.getCityIds() != null && !filter.getCityIds().isEmpty()) {
            query.setParameter("cityIds", filter.getCityIds());
        }
        if (filter.getAdvertisementTypeIds() != null && !filter.getAdvertisementTypeIds().isEmpty()) {
            query.setParameter("typeIds", filter.getAdvertisementTypeIds());
        }
        if (filter.getSearchParam() != null && !filter.getSearchParam().isBlank()) {
            String pattern = "%" + filter.getSearchParam().toLowerCase() + "%";
            query.setParameter("search", pattern);
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

    @Override
    public List<Advertisement> findAllByUserId(UUID id) {
        String jpql = "select from Advertisement a where a.user.userId=:id";
        return getSession().createQuery(jpql, Advertisement.class).setParameter("id", id).getResultList();
    }

    private TypedQuery<Advertisement> buildQuery(AdvertisementFilterDto filter) {
        String jpql = """
                SELECT a FROM Advertisement a
                join fetch a.city c
                join fetch a.advertisementType at
                join fetch a.user u
                join UserRatingView ur on u.userId = ur.userId
                WHERE 1=1
                """;
        if (filter.getCityIds() != null && !filter.getCityIds().isEmpty()) {
            jpql += " AND a.city.cityId IN :cityIds";
        }
        if (filter.getAdvertisementTypeIds() != null && !filter.getAdvertisementTypeIds().isEmpty()) {
            jpql += " AND a.advertisementType.advertisementTypeId IN :typeIds";
        }
        if (filter.getSearchParam() != null && !filter.getSearchParam().isBlank()) {
            jpql += " AND (LOWER(a.title) LIKE :search OR LOWER(a.description) LIKE :search)";
        }
        if (filter.getMinPrice() != null) {
            jpql += " AND a.price >= :minPrice";
        }
        if (filter.getMaxPrice() != null) {
            jpql += " AND a.price <= :maxPrice";
        }
        jpql += " ORDER BY a.isPaid DESC, ur.averageRating DESC, ur.ratingCount DESC, a.createDate ASC";

        return getSession().createQuery(jpql, Advertisement.class);
    }
}
