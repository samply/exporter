package de.samply.db.crud;

import java.util.List;

public class Page<T> {

  private List<T> elements;
  private Integer totalPages;
  private Boolean hasNextPage;

  private Long totalElements;

  public Page() {
  }

  public Page(org.springframework.data.domain.Page<T> dbPage) {
    elements = dbPage.getContent();
    totalElements = dbPage.getTotalElements();
    hasNextPage = dbPage.hasNext();
    totalElements = dbPage.getTotalElements();
  }

  public List<T> getElements() {
    return elements;
  }

  public void setElements(List<T> elements) {
    this.elements = elements;
  }

  public Integer getTotalPages() {
    return totalPages;
  }

  public void setTotalPages(Integer totalPages) {
    this.totalPages = totalPages;
  }

  public Boolean getHasNextPage() {
    return hasNextPage;
  }

  public void setHasNextPage(Boolean hasNextPage) {
    this.hasNextPage = hasNextPage;
  }

  public Long getTotalElements() {
    return totalElements;
  }

  public void setTotalElements(Long totalElements) {
    this.totalElements = totalElements;
  }

}
