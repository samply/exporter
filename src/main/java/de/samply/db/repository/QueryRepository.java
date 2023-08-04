package de.samply.db.repository;

import de.samply.db.model.Query;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface QueryRepository extends JpaRepository<Query, Long> {

  @Modifying
  @org.springframework.data.jpa.repository.Query("UPDATE Query SET archivedAt =?2 WHERE id = ?1")
  void setArchiveQuery (Long queryId, Instant archivedAt);

}
