package de.samply.db.repository;

import de.samply.db.model.QueryExecutionError;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;

@Repository
public interface QueryExecutionErrorRepository extends JpaRepository<QueryExecutionError, Long> {

    List<QueryExecutionError> findByQueryExecutionId(Long queryExecutionId);

    List<QueryExecutionError> findByQueryExecutionId(Long queryExecutionId, PageRequest pageRequest);

}
