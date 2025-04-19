package org.banana.repository;

import org.banana.entity.SaleHistory;
import org.banana.repository.crud.AbstractCrudRepositoryImpl;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class SaleHistoryRepositoryImpl extends AbstractCrudRepositoryImpl<SaleHistory, UUID> implements SaleHistoryRepository {

    public SaleHistoryRepositoryImpl() {
        super(SaleHistory.class);
    }
}
