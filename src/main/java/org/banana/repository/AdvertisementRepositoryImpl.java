package org.banana.repository;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.banana.dto.advertisement.AdvertisementFilterDto;
import org.banana.dto.advertisement.AdvertisementResponseDto;
import org.banana.entity.Advertisement;
import org.banana.repository.crud.AbstractCrudRepositoryImpl;
import org.banana.util.JpqlHelper;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@Slf4j
public class AdvertisementRepositoryImpl extends AbstractCrudRepositoryImpl<Advertisement, UUID> implements AdvertisementRepository {

    public static final String UPDATE_ADVERTISEMENT_QUANTITY = """
            update Advertisement a set a.quantity = :newQuantity
            where a.id = :id
            and a.quantity = :oldQuantity
            and a.closeDate is null""";
    private static final String FIND_FULL_DTO = """
                select new org.banana.dto.advertisement.AdvertisementResponseDto(
                a.id,
                new org.banana.dto.user.UserResponseDto(
                    u.id,
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
                a.isPromoted,
                a.createDate,
                a.closeDate
            )
            from Advertisement a
            join a.city c
            join a.advertisementType at
            join a.user u
            left join UserRatingView ur on ur.userId = u.id
            where 1=1
            """;
    public static final String FIND_FULL_DTO_BY_ID = FIND_FULL_DTO + " and a.id = :id";
    private static final String FIND_FETCHED_BY_ID = "select a from Advertisement a join fetch a.city join fetch a.advertisementType join fetch a.user left join fetch a.user.userRatingView where a.id = :id";

    public AdvertisementRepositoryImpl() {
        super(Advertisement.class);
    }

    @Override
    public void detach(Advertisement advertisement) {
        getSession().detach(advertisement);
//        getSession().refresh(advertisement);
    }

    @Override
    public Optional<AdvertisementResponseDto> findDtoById(UUID id) {
        log.info("findDtoById({}) in {}", id, getClass().getSimpleName());
        return Optional.ofNullable(getSession()
                .createQuery(FIND_FULL_DTO_BY_ID, AdvertisementResponseDto.class)
                .setParameter("id", id)
                .getSingleResultOrNull());
    }

    @Override
    public Optional<Advertisement> findFetchedById(UUID id) {
        return Optional.ofNullable(getSession().createQuery(FIND_FETCHED_BY_ID, Advertisement.class)
                .setParameter("id", id)
                .getSingleResultOrNull());
    }

    @Override
    public List<AdvertisementResponseDto> findAllFiltered(@NotNull AdvertisementFilterDto filter, int page, int size) {
        log.info("findAllFiltered() in {}", getClass().getSimpleName());
        Map<String, Object> params = new HashMap<>();
        Query<AdvertisementResponseDto> query = getAdvertisementResponseDtoQuery(filter, params);

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() instanceof Collection<?>) {
                query.setParameterList(entry.getKey(), (Collection<?>) entry.getValue());
            } else {
                query.setParameter(entry.getKey(), entry.getValue());
            }
        }
        query.setFirstResult(page * size);
        query.setMaxResults(size);

        return query.getResultList();
    }

    @Override
    public int updateAdvertisementQuantity(UUID id, int oldQuantity, int newQuantity) {
        return getSession().createMutationQuery(UPDATE_ADVERTISEMENT_QUANTITY)
                .setParameter("id", id)
                .setParameter("oldQuantity", oldQuantity)
                .setParameter("newQuantity", newQuantity)
                .executeUpdate();
    }

    private Query<AdvertisementResponseDto> getAdvertisementResponseDtoQuery(AdvertisementFilterDto filter, Map<String, Object> params) {
        StringBuilder jpql = new StringBuilder();
        jpql.append(FIND_FULL_DTO);
        if (filter.isOnlyOpened())
            jpql.append(" and a.closeDate is null");

        if (filter.getCityIds() != null && !filter.getCityIds().isEmpty()) {
            jpql.append(" and a.city.id IN :cityIds");
            params.put("cityIds", filter.getCityIds());
        }

        if (filter.getAdvertisementTypeIds() != null && !filter.getAdvertisementTypeIds().isEmpty()) {
            jpql.append(" and a.advertisementType.id IN :typeIds");
            params.put("typeIds", filter.getAdvertisementTypeIds());
        }

        if (StringUtils.isNotBlank(filter.getSearchParam())) {
            jpql.append(" and (lower(a.title) like :search escape '\\' or lower(a.description) like :search escape '\\')");
            params.put("search", "%" + JpqlHelper.formatSearchParam(filter.getSearchParam()) + "%");
        }

        if (filter.getMinPrice() != null) {
            jpql.append(" and a.price >= :minPrice");
            params.put("minPrice", filter.getMinPrice());
        }

        if (filter.getMaxPrice() != null) {
            jpql.append(" and a.price <= :maxPrice");
            params.put("maxPrice", filter.getMaxPrice());
        }

        jpql.append(" order by a.isPromoted desc, ur.averageRating desc NULLS LAST, ur.ratingCount desc NULLS LAST, a.createDate desc");

        return getSession().createQuery(jpql.toString(), AdvertisementResponseDto.class);
    }
}
