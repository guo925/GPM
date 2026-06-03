package com.gjx.gpms.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis 统一缓存服务。
 * 业务代码通过本服务访问缓存，避免 RedisTemplate 分散在各业务类中。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheService {

    private static final String NULL_VALUE = "__GPMS_NULL_VALUE__";

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 查询相关逻辑。
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 查询相关逻辑。
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = get(key);
        if (value == null || NULL_VALUE.equals(value)) {
            return null;
        }
        return (T) value;
    }

    /**
     * 处理set相关逻辑。
     */
    public void set(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    /**
     * 删除相关逻辑。
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 处理expire相关逻辑。
     */
    public Boolean expire(String key, Duration ttl) {
        return redisTemplate.expire(key, ttl);
    }

    /**
     * 处理increment相关逻辑。
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 设置WithRandomTtl。
     */
    public void setWithRandomTtl(String key, Object value, Duration baseTtl, int randomSeconds) {
        int jitter = randomSeconds <= 0 ? 0 : ThreadLocalRandom.current().nextInt(randomSeconds + 1);
        set(key, value, baseTtl.plusSeconds(jitter));
    }

    /**
     * 处理cacheNullValue相关逻辑。
     */
    public void cacheNullValue(String key, Duration ttl) {
        set(key, NULL_VALUE, ttl);
    }

    /**
     * 处理tryLock相关逻辑。
     */
    public Boolean tryLock(String key, Duration ttl) {
        return redisTemplate.opsForValue().setIfAbsent(key, "1", ttl);
    }

    /**
     * 设置IfAbsent。
     */
    public Boolean setIfAbsent(String key, Object value, Duration ttl) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, ttl);
    }

    /**
     * 处理isNullValue相关逻辑。
     */
    public boolean isNullValue(Object value) {
        return NULL_VALUE.equals(value);
    }

    /**
     * 获取OrLoad。
     */
    public <T> T getOrLoad(String key, Class<T> type, Duration ttl, int randomSeconds, Supplier<T> loader) {
        Object cached = get(key);
        if (cached != null) {
            return isNullValue(cached) ? null : type.cast(cached);
        }

        String lockKey = "lock:" + key;
        boolean locked = Boolean.TRUE.equals(tryLock(lockKey, Duration.ofSeconds(5)));
        try {
            if (locked) {
                Object secondCheck = get(key);
                if (secondCheck != null) {
                    return isNullValue(secondCheck) ? null : type.cast(secondCheck);
                }
                T loaded = loader.get();
                if (Objects.isNull(loaded)) {
                    cacheNullValue(key, Duration.ofMinutes(1));
                    return null;
                }
                setWithRandomTtl(key, loaded, ttl, randomSeconds);
                return loaded;
            }
            TimeUnit.MILLISECONDS.sleep(80);
            Object retry = get(key);
            return retry == null || isNullValue(retry) ? null : type.cast(retry);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("等待缓存锁被中断，key={}", key);
            return loader.get();
        } finally {
            if (locked) {
                delete(lockKey);
            }
        }
    }

}
