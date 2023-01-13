package de.samply.db.repository;

import de.samply.db.model.QueryExecutionError;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueryExecutionErrorRepository extends JpaRepository<QueryExecutionError, Long> {

  List<QueryExecutionError> findByQueryExecutionId(Long queryExecutionId);

}
