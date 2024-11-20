package com.kmkbe.core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Date;
import java.util.Map;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "_redis_attack", schema = "public")
public class RedisAttack {
    @Id
    @Size(max = 255)
    @Column(name = "redis", nullable = false)
    private String redis;

    @Size(max = 255)
    @Column(name = "session")
    private String session;

    @Column(name = "modified_date")
    private Date modifiedDate;


    @Column(name = "count_attack")
    private int countAttack;



}
