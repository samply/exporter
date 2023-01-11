package de.samply.db.crud;

import de.samply.db.model.Query;
import de.samply.db.repository.QueryRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TeilerDbService {

  private QueryRepository queryRepository;

  public TeilerDbService(@Autowired QueryRepository queryRepository) {
    this.queryRepository = queryRepository;
  }

  public Query fetchQuery (String queryId){
    return queryRepository.findById(Long.valueOf(queryId)).get();
  }

  public String saveQueryAndGetQueryId (Query query){
    return queryRepository.save(query).getId().toString();
  }

  public List<Query> fetchAllQueries(){
    return queryRepository.findAll();
  }

  public List<Query> fetchAllQueries(int page, int pageSize){
    return queryRepository.findAll(PageRequest.of(page, pageSize)).stream().toList();
  }


}
