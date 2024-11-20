package com.kmkbe.core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "_redis", schema = "public")
public class RedisLog {
    @Id
    @Size(max = 255)
    @Column(name = "redis", nullable = false)
    private String redis;

    @Column(name = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> json;

    @Size(max = 255)
    @Column(name = "session")
    private String session;

}
