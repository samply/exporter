package de.samply.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "query_execution_file", schema = "samply")
public class QueryExecutionFile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "query_execution_id")
  private Long queryExecutionId;

  @Column(name = "file_path")
  private String filePath;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getQueryExecutionId() {
    return queryExecutionId;
  }

  public void setQueryExecutionId(Long queryExecutionId) {
    this.queryExecutionId = queryExecutionId;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

}
