package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.RedisLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface RedisRepository extends JpaRepository<RedisLog, String> {

    List<RedisLog> findAllByRedis(String strings);
    Optional<RedisLog> findFirstByRedis(String strings);
    Optional<RedisLog> findFirstBySession(String session);
}
