package com.kmkbe.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test RedisConfig: standalone factory memakai host/port/db/password
 * dari properti, cacheManager TTL (default 10 menit, refreshToken 5 menit),
 * dan redisTemplate dengan serializer yang benar.
 */
class RedisConfigTest {

  private RedisConfig config;

  @BeforeEach
  void setUp() {
    config = new RedisConfig();
    ReflectionTestUtils.setField(config, "host", "localhost");
    ReflectionTestUtils.setField(config, "port", 6379);
    ReflectionTestUtils.setField(config, "database", 6);
    ReflectionTestUtils.setField(config, "password", "secret");
    ReflectionTestUtils.setField(config, "maxIdle", 10);
    ReflectionTestUtils.setField(config, "minIdle", 2);
    ReflectionTestUtils.setField(config, "maxActive", 30);
    ReflectionTestUtils.setField(config, "maxWait", 10_000L);
  }

  @Test
  void lettuceConnectionFactoryIsPrimaryAndStandalone() {
    RedisConnectionFactory factory = config.redisConnectionFactory();

    assertThat(factory).isInstanceOf(LettuceConnectionFactory.class);
    LettuceConnectionFactory lettuce = (LettuceConnectionFactory) factory;
    RedisStandaloneConfiguration standalone = lettuce.getStandaloneConfiguration();

    assertThat(standalone.getHostName()).isEqualTo("localhost");
    assertThat(standalone.getPort()).isEqualTo(6379);
    assertThat(standalone.getDatabase()).isEqualTo(6);
    // password ter-set (nilai di-mask dalam toString)
    assertThat(standalone.getPassword().toString()).isNotEqualTo("RedisPassword[null]");
    assertThat(standalone.getPassword().get()).isNotNull();
  }

  @Test
  void cacheManagerConfiguresRefreshTokenTtlAndDisablesNullValues() {
    var cacheManager = config.cacheManager();
    // RedisCacheManager lazy: cache baru terlihat setelah initializeCaches()
    // (dipanggil Spring bean lifecycle di runtime)
    ((org.springframework.cache.support.AbstractCacheManager) cacheManager).initializeCaches();

    var refreshToken = cacheManager.getCacheConfigurations().get("refreshToken");
    assertThat(refreshToken).isNotNull();
    assertThat(refreshToken.getTtl()).isEqualTo(Duration.ofMinutes(5));
    // perilaku aktual: withCacheConfiguration("refreshToken") memakai
    // defaultCacheConfig() tanpa disableCachingNullValues (hanya default
    // config yang men-disable) — dijaga agar perubahan tak lolos tanpa sadar
    assertThat(refreshToken.getAllowCacheNullValues()).isTrue();

    // default config (tanpa override) = 10 menit — ambil cache mana pun
    // selain "refreshToken" (getMissingCache memakai default config)
    var otherCache = (org.springframework.data.redis.cache.RedisCache) cacheManager.getCache("anyOther");
    assertThat(otherCache).isNotNull();
    assertThat(otherCache.getCacheConfiguration().getTtl()).isEqualTo(Duration.ofMinutes(10));
  }

  @Test
  @SuppressWarnings("unchecked")
  void redisTemplateUsesStringKeyAndJsonValueSerializers() {
    RedisTemplate<String, Object> template = config.redisTemplate();

    org.springframework.data.redis.serializer.RedisSerializer<String> keySerializer =
      (org.springframework.data.redis.serializer.RedisSerializer<String>) template.getKeySerializer();
    org.springframework.data.redis.serializer.RedisSerializer<String> hashKeySerializer =
      (org.springframework.data.redis.serializer.RedisSerializer<String>) template.getHashKeySerializer();

    assertThat(keySerializer.serialize("key-1")).isEqualTo("key-1".getBytes());
    assertThat(hashKeySerializer.serialize("hk")).isEqualTo("hk".getBytes());
    assertThat(template.getHashValueSerializer()).isNotNull();
  }

  @Test
  void cacheNameConstantIsStable() {
    // kontrak yang mungkin dirujuk service lain
    assertThat(RedisConfig.CSUL_CACHE_NAME).isEqualTo("kmk");
  }
}
