package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.RedisAttack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface RedisAttackRepository extends JpaRepository<RedisAttack, String> {

    Optional<RedisAttack> findTopByRedis(String strings);


    void deleteByRedis(String redis);
}
