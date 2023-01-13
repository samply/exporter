package de.samply.db.repository;

import de.samply.db.model.QueryExecutionFile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueryExecutionFileRepository extends JpaRepository<QueryExecutionFile, Long> {

  List<QueryExecutionFile> findByQueryExecutionId(Long queryExecutionId);

}
