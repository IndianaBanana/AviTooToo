package org.banana.repository;

import org.banana.entity.SaleHistory;
import org.banana.repository.crud.CrudRepository;

import java.util.UUID;

public interface SaleHistoryRepository extends CrudRepository<SaleHistory, UUID> {

}
