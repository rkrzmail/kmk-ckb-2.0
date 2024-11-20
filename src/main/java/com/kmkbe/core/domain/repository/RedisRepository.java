package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.RedisLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface RedisRepository extends JpaRepository<RedisLog, String> {

    List<RedisLog> findAllByRedis(String strings);
}
