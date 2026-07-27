package com.kmkbe.helpers.base;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.io.Serializable;
import java.sql.Timestamp;

@Data
@MappedSuperclass
public class BaseEntity implements Serializable {

  private static final long serialVersionUID = -278971545601962170L;

  @Column(name = "created_by", length = 50, nullable = false)
  private String createdBy;

  @Column(name = "created_time", nullable = false, updatable = false)
  @CreationTimestamp
  private Timestamp createdTime;

  @Column(name = "updated_by", length = 50)
  private String updatedBy;

  @Column(name = "updated_time")
  @UpdateTimestamp
  private Timestamp updatedTime;

  @PrePersist
  void onCreate() {
    this.createdTime = new Timestamp(System.currentTimeMillis());
  }

  @PreUpdate
  void onUpdate() {
    this.updatedTime = new Timestamp(System.currentTimeMillis());
  }
}
