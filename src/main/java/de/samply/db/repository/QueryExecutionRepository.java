package de.samply.db.repository;

import de.samply.db.model.QueryExecution;
import de.samply.db.model.Status;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QueryExecutionRepository extends JpaRepository<QueryExecution, Long> {

    @Modifying
    @Query("UPDATE QueryExecution SET status=?2 WHERE id=?1")
    void updateStatus(Long id, Status status);

    List<QueryExecution> findByQueryId(Long queryExecutionId);

    List<QueryExecution> findByQueryId(Long queryExecutionId, Pageable pageable);

    List<QueryExecution> findByStatus(Status status);

}
