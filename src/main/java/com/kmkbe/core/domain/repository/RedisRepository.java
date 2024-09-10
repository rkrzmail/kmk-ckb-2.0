package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.Redis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RedisRepository extends JpaRepository<Redis, String> {
}
