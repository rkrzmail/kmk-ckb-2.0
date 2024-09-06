package com.kmkbe.core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "rabbitmq_log")
public class RabbitmqLog {
    @Id
    @ColumnDefault("nextval('rabbitmq_log_rabbitmq_log_id_seq'::regclass)")
    @Column(name = "rabbitmq_log_id", nullable = false)
    private Long rabbitMqLogId;

    @Size(max = 100)
    @NotNull
    @Column(name = "exchange_name", nullable = false, length = 100)
    private String exchangeName;

    @Size(max = 100)
    @NotNull
    @Column(name = "exchange_type", nullable = false, length = 100)
    private String exchangeType;

    @Size(max = 100)
    @NotNull
    @Column(name = "last_exchange", nullable = false, length = 100)
    private String lastExchange;

    @Size(max = 100)
    @NotNull
    @Column(name = "queue_name", nullable = false, length = 100)
    private String queueName;

    @Size(max = 100)
    @NotNull
    @Column(name = "route_key", nullable = false, length = 100)
    private String routeKey;

    @Size(max = 1000)
    @NotNull
    @Column(name = "payload", nullable = false, length = 1000)
    private String payload;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "is_ack", nullable = false)
    private Boolean isAck = false;

    @Size(max = 50)
    @NotNull
    @Column(name = "dtm_crt", nullable = false, length = 50)
    private String dtmCrt;

    @Column(name = "dtm_upd")
    private Instant dtmUpd;

}
