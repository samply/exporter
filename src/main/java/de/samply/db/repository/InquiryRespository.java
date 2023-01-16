package de.samply.db.repository;

import de.samply.db.model.Inquiry;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InquiryRespository extends JpaRepository<Inquiry, Long> {

  List<Inquiry> findByArchivedAtIsNotNull();
  Page<Inquiry> findByArchivedAtIsNotNull(Pageable pageable);
  List<Inquiry> findByArchivedAtIsNullAndErrorIsNotNull();
  Page<Inquiry> findByArchivedAtIsNullAndErrorIsNotNull(Pageable pageable);
  List<Inquiry> findByArchivedAtIsNullAndErrorIsNull();
  Page<Inquiry> findByArchivedAtIsNullAndErrorIsNull(Pageable pageable);

}
