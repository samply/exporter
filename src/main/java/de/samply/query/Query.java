package de.samply.query;

import de.samply.converter.Format;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Table(name = "query")
public class Query {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "query")
  private String query;

  @Column(name = "format")
  private Format format;

  @Column(name = "label")
  private String label;

  @Column(name = "description")
  private String description;

  @Column(name = "contact_id")
  private String contactId;

  @Column(name = "expiration_date")
  private Date expirationDate;

  @Column(name = "archived_at")
  private Timestamp archivedAt;

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

  public Date getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(Date expirationDate) {
    this.expirationDate = expirationDate;
  }

  public Timestamp getArchivedAt() {
    return archivedAt;
  }

  public void setArchivedAt(Timestamp archivedAt) {
    this.archivedAt = archivedAt;
  }

}
