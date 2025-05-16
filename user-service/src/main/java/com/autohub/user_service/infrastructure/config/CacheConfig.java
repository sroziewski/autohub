package com.autohub.user_service.infrastructure.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for caching using Redis.
 * Defines cache settings and TTL for different cache regions.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Cache manager configuration with Redis.
     * Configures different TTLs for different cache regions.
     *
     * @param connectionFactory Redis connection factory
     * @return RedisCacheManager
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // Default TTL: 30 minutes
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
                )
                .disableCachingNullValues();

        // Configure TTL for specific caches
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // User cache: 15 minutes TTL
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // User by ID cache: 15 minutes TTL
        cacheConfigurations.put("userById", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // User by email cache: 15 minutes TTL
        cacheConfigurations.put("userByEmail", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // All users cache (paginated): 5 minutes TTL
        cacheConfigurations.put("allUsers", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // Build and return the cache manager
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
