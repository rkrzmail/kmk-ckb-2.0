package com.kmkbe.scheduled;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "scheduler_logs")
public class SchedulerLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String jobName;
  private LocalDateTime startTime;
  private LocalDateTime endTime;

  @Column(length = 20)
  private String status;

  @Column(columnDefinition = "TEXT")
  private String errorMessage;
}
