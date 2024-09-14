package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.Redis;
import org.apache.el.stream.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface RedisRepository extends JpaRepository<Redis, String> {

    List<Redis> findAllByRedis(String strings);
}
