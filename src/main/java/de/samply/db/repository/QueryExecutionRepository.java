package de.samply.db.repository;

import de.samply.db.model.QueryExecution;
import de.samply.db.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QueryExecutionRepository extends JpaRepository<QueryExecution, Long> {

  @Modifying
  @Query("UPDATE QueryExecution SET status=?2 WHERE id=?1")
  void updateStatus (Long id, Status status);

  QueryExecution getById(Long id);

}
