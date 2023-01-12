package de.samply.db.crud;

import de.samply.db.model.Query;
import de.samply.db.model.QueryExecution;
import de.samply.db.model.QueryExecutionFile;
import de.samply.db.model.Status;
import de.samply.db.repository.QueryExecutionFileRepository;
import de.samply.db.repository.QueryExecutionRepository;
import de.samply.db.repository.QueryRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeilerDbService {

  private QueryRepository queryRepository;
  private QueryExecutionRepository queryExecutionRepository;
  private QueryExecutionFileRepository queryExecutionFileRepository;

  public TeilerDbService(
      @Autowired QueryRepository queryRepository,
      @Autowired QueryExecutionRepository queryExecutionRepository,
      @Autowired QueryExecutionFileRepository queryExecutionFileRepository) {
    this.queryRepository = queryRepository;
    this.queryExecutionRepository = queryExecutionRepository;
    this.queryExecutionFileRepository = queryExecutionFileRepository;
  }

  @Transactional
  public Query fetchQuery (Long queryId){
    return queryRepository.findById(queryId).get();
  }

  @Transactional
  public Long saveQueryAndGetQueryId (Query query){
    return queryRepository.save(query).getId();
  }

  @Transactional
  public List<Query> fetchAllQueries(){
    return queryRepository.findAll();
  }

  @Transactional
  public List<Query> fetchAllQueries(int page, int pageSize){
    return queryRepository.findAll(PageRequest.of(page, pageSize)).stream().toList();
  }

  @Transactional
  public Long saveQueryExecutionAndGetExecutionId (QueryExecution queryExecution){
    return queryExecutionRepository.save(queryExecution).getId();
  }

  @Transactional
  public void saveQueryExecutionFile (QueryExecutionFile queryExecutionFile){
    queryExecutionFileRepository.save(queryExecutionFile);
  }

  @Transactional
  public void setQueryExecutionOk (Long queryExecutionId){
    queryExecutionRepository.updateStatus(queryExecutionId, Status.OK);
  }

  @Transactional
  public void setQueryExecutionError (Long queryExecutionId){
    queryExecutionRepository.updateStatus(queryExecutionId, Status.ERROR);
  }


}
