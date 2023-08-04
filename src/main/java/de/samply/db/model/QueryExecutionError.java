package de.samply.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "query_execution_error", schema = "samply")
public class QueryExecutionError {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "query_execution_id")
  private Long queryExecutionId;

  @Column(name = "error")
  private String error;

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

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

}
