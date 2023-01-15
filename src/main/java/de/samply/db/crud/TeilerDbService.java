package de.samply.db.crud;

import de.samply.db.model.Inquiry;
import de.samply.db.model.Query;
import de.samply.db.model.QueryExecution;
import de.samply.db.model.QueryExecutionError;
import de.samply.db.model.QueryExecutionFile;
import de.samply.db.model.Status;
import de.samply.db.repository.InquiryRespository;
import de.samply.db.repository.QueryExecutionErrorRepository;
import de.samply.db.repository.QueryExecutionFileRepository;
import de.samply.db.repository.QueryExecutionRepository;
import de.samply.db.repository.QueryRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeilerDbService {

  private QueryRepository queryRepository;
  private QueryExecutionRepository queryExecutionRepository;
  private QueryExecutionFileRepository queryExecutionFileRepository;
  private QueryExecutionErrorRepository queryExecutionErrorRepository;
  private InquiryRespository inquiryRespository;

  public TeilerDbService(
      @Autowired QueryRepository queryRepository,
      @Autowired QueryExecutionRepository queryExecutionRepository,
      @Autowired QueryExecutionFileRepository queryExecutionFileRepository,
      @Autowired QueryExecutionErrorRepository queryExecutionErrorRepository,
      @Autowired InquiryRespository inquiryRespository) {
    this.queryRepository = queryRepository;
    this.queryExecutionRepository = queryExecutionRepository;
    this.queryExecutionFileRepository = queryExecutionFileRepository;
    this.queryExecutionErrorRepository = queryExecutionErrorRepository;
    this.inquiryRespository = inquiryRespository;
  }

  @Transactional
  public Query fetchQuery(Long queryId) {
    return queryRepository.findById(queryId).get();
  }

  @Transactional
  public Long saveQueryAndGetQueryId(Query query) {
    return queryRepository.saveAndFlush(query).getId();
  }

  @Transactional
  public List<Query> fetchAllQueries() {
    return queryRepository.findAll();
  }

  @Transactional
  public Page<Query> fetchAllQueries(int page, int pageSize) {
    return new Page<>(queryRepository.findAll(PageRequest.of(page, pageSize)));
  }

  @Transactional
  public List<Inquiry> fetchActiveInquiries() {
    return inquiryRespository.findByArchivedAtIsNullAndErrorIsNull();
  }

  @Transactional
  public Page<Inquiry> fetchActiveInquiries(int page, int pageSize) {
    return new Page<>(
        inquiryRespository.findByArchivedAtIsNullAndErrorIsNull(PageRequest.of(page, pageSize)));
  }


  @Transactional
  public List<Inquiry> fetchErrorInquiries() {
    return inquiryRespository.findByArchivedAtIsNullAndErrorIsNotNull();
  }

  @Transactional
  public Page<Inquiry> fetchErrorInquiries(int page, int pageSize) {
    return new Page<>(
        inquiryRespository.findByArchivedAtIsNullAndErrorIsNotNull(PageRequest.of(page, pageSize)));
  }

  @Transactional
  public List<Inquiry> fetchArchivedInquiries() {
    return inquiryRespository.findByArchivedAtIsNotNull();
  }

  @Transactional
  public Page<Inquiry> fetchArchivedInquiries(int page, int pageSize) {
    return new Page<>(inquiryRespository.findByArchivedAtIsNotNull(PageRequest.of(page, pageSize)));
  }

  @Transactional
  public Optional<Inquiry> fetchInquiry(Long queryId) {
    return inquiryRespository.findById(queryId);
  }

  @Transactional
  public Long saveQueryExecutionAndGetExecutionId(QueryExecution queryExecution) {
    return queryExecutionRepository.saveAndFlush(queryExecution).getId();
  }

  @Transactional
  public void saveQueryExecutionFile(QueryExecutionFile queryExecutionFile) {
    queryExecutionFileRepository.saveAndFlush(queryExecutionFile);
  }

  @Transactional
  public void setQueryExecutionAsOk(Long queryExecutionId) {
    queryExecutionRepository.updateStatus(queryExecutionId, Status.OK);
  }

  @Transactional
  public void setQueryExecutionAsError(Long queryExecutionId) {
    queryExecutionRepository.updateStatus(queryExecutionId, Status.ERROR);
  }

  @Transactional
  public Optional<QueryExecution> fetchQueryExecution(Long queryExecutionId) {
    return queryExecutionRepository.findById(queryExecutionId);
  }

  @Transactional
  public List<QueryExecutionFile> fetchQueryExecutionFilesByQueryExecutionId(
      Long queryExecutionId) {
    return queryExecutionFileRepository.findByQueryExecutionId(queryExecutionId);
  }

  @Transactional
  public Long saveQueryExecutionErrorAndGetId(QueryExecutionError queryExecutionError) {
    return queryExecutionErrorRepository.saveAndFlush(queryExecutionError).getId();
  }

  @Transactional
  public List<QueryExecutionError> fetchQueryExecutionErrorByQueryExecutionId(
      Long queryExecutionId) {
    return queryExecutionErrorRepository.findByQueryExecutionId(queryExecutionId);
  }

}
