package de.samply.db.crud;

import de.samply.db.model.Query;
import de.samply.db.model.QueryExecution;
import de.samply.db.model.QueryExecutionError;
import de.samply.db.model.QueryExecutionFile;
import de.samply.db.model.Status;
import de.samply.db.repository.QueryExecutionErrorRepository;
import de.samply.db.repository.QueryExecutionFileRepository;
import de.samply.db.repository.QueryExecutionRepository;
import de.samply.db.repository.QueryRepository;
import java.util.Collection;
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

  public TeilerDbService(
      @Autowired QueryRepository queryRepository,
      @Autowired QueryExecutionRepository queryExecutionRepository,
      @Autowired QueryExecutionFileRepository queryExecutionFileRepository,
      @Autowired QueryExecutionErrorRepository queryExecutionErrorRepository) {
    this.queryRepository = queryRepository;
    this.queryExecutionRepository = queryExecutionRepository;
    this.queryExecutionFileRepository = queryExecutionFileRepository;
    this.queryExecutionErrorRepository = queryExecutionErrorRepository;
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
  public List<Query> fetchAllQueries(int page, int pageSize) {
    return queryRepository.findAll(PageRequest.of(page, pageSize)).stream().toList();
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
