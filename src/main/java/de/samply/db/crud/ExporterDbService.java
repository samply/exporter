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
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExporterDbService {

  private QueryRepository queryRepository;
  private QueryExecutionRepository queryExecutionRepository;
  private QueryExecutionFileRepository queryExecutionFileRepository;
  private QueryExecutionErrorRepository queryExecutionErrorRepository;
  private InquiryRespository inquiryRespository;

  public ExporterDbService(
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
  public Optional<Query> fetchQuery(Long queryId) {
    return queryRepository.findById(queryId);
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
  public List<QueryExecution> fetchAllQueryExecutions() {
    return queryExecutionRepository.findAll();
  }

  @Transactional
  public Page<QueryExecution> fetchAllQueryExecutions(int page, int pageSize) {
    return new Page<>(queryExecutionRepository.findAll(PageRequest.of(page, pageSize)));
  }

  @Transactional
  public List<QueryExecutionError> fetchAllQueryExecutionErrors() {
    return queryExecutionErrorRepository.findAll();
  }

  @Transactional
  public Page<QueryExecutionError> fetchAllQueryExecutionErrors(int page, int pageSize) {
    return new Page<>(queryExecutionErrorRepository.findAll(PageRequest.of(page, pageSize)));
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
  public void archiveQuery(Long queryId) {
    queryRepository.setArchiveQuery(queryId, Instant.now());
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
  public Status getQueryExecutionStatus(Long queryExecutionId){
    QueryExecution queryExecution = queryExecutionRepository.getById(queryExecutionId);
    return (queryExecution != null) ? queryExecution.getStatus() : null;
  }

  @Transactional
  public Optional<QueryExecution> fetchQueryExecution(Long queryExecutionId) {
    return queryExecutionRepository.findById(queryExecutionId);
  }

  @Transactional
  public List<QueryExecution> fetchRunningQueryExecutions() {
    return queryExecutionRepository.findByStatus(Status.RUNNING);
  }


  @Transactional
  public List<QueryExecution> fetchQueryExecutionByQueryId(Long queryId) {
    return queryExecutionRepository.findByQueryId(queryId);
  }

  @Transactional
  public List<QueryExecution> fetchQueryExecutionByQueryId(Long queryId, int page, int pageSize) {
    return queryExecutionRepository.findByQueryId(queryId, PageRequest.of(page, pageSize));
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

  @Transactional
  public List<QueryExecutionError> fetchQueryExecutionErrorByQueryExecutionId(
          Long queryExecutionId, int page, int pageSize) {
    return queryExecutionErrorRepository.findByQueryExecutionId(queryExecutionId, PageRequest.of(page, pageSize));
  }

}
