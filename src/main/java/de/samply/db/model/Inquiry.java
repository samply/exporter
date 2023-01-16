package de.samply.db.model;

import de.samply.converter.Format;
import de.samply.teiler.TeilerConst;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.Formula;

@Entity
@Table(name = "query", schema = "samply")
public class Inquiry {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "query")
  private String query;

  @Column(name = "format")
  @Enumerated(EnumType.STRING)
  private Format format;

  //@Column(name = "label")
  @Formula("(CASE WHEN label IS NULL THEN '" + TeilerConst.INQUIRY_NULL_LABEL + "' ELSE label END)")
  private String label;

  //@Column(name = "description")
  @Formula("(CASE WHEN description IS NULL THEN '" + TeilerConst.INQUIRY_NULL_DESCRIPTION
      + "' ELSE description END)")
  private String description;

  @Column(name = "contact_id")
  private String contactId;

  @Formula("TO_CHAR(created_at, 'YYYY-MM-DD HH24:MI')")
  private String receivedAt;

  @Formula("TO_CHAR(archived_at, 'YYYY-MM-DD HH24:MI')")
  private String archivedAt;

  @Formula("(SELECT e.template_id FROM samply.query q JOIN samply.query_execution e ON q.id = e.query_id ORDER BY e.executed_at DESC LIMIT 1)")
  private String templateId;

  @Formula("(SELECT TO_CHAR(e.executed_at, 'YYYY-MM-DD HH24:MI') FROM samply.query q JOIN samply.query_execution e ON q.id = e.query_id ORDER BY e.executed_at DESC LIMIT 1)")
  private String lastExecutedAt;

  @Formula("(SELECT er.error FROM samply.query q JOIN samply.query_execution e ON q.id = e.query_id LEFT JOIN samply.query_execution_error er ON e.id = er.query_execution_id ORDER BY e.executed_at DESC LIMIT 1)")
  private String error;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public Format getFormat() {
    return format;
  }

  public void setFormat(Format format) {
    this.format = format;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getContactId() {
    return contactId;
  }

  public void setContactId(String contactId) {
    this.contactId = contactId;
  }

  public String getReceivedAt() {
    return receivedAt;
  }

  public void setReceivedAt(String receivedAt) {
    this.receivedAt = receivedAt;
  }

  public String getArchivedAt() {
    return archivedAt;
  }

  public void setArchivedAt(String archivedAt) {
    this.archivedAt = archivedAt;
  }

  public String getTemplateId() {
    return templateId;
  }

  public void setTemplateId(String templateId) {
    this.templateId = templateId;
  }

  public String getLastExecutedAt() {
    return lastExecutedAt;
  }

  public void setLastExecutedAt(String lastExecutedAt) {
    this.lastExecutedAt = lastExecutedAt;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

}
