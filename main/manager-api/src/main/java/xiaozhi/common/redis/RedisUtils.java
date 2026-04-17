package xiaozhi.common.redis;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import xiaozhi.common.utils.ResourcesUtils;

/**
 * Redistoolclass
 * Copyright (c) Renren Opensource All rights reserved.
 * Website: https://www.renren.io
 */
@Component
public class RedisUtils {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ResourcesUtils resourceUtils;

    /**
     * default期when长as24smallwhen，unit：seconds
     */
    public final static long DEFAULT_EXPIRE = 60 * 60 * 24L;
    /**
     * 期when长as1smallwhen，unit：seconds
     */
    public final static long HOUR_ONE_EXPIRE = (long) 60 * 60;
    /**
     * 期when长as6smallwhen，unit：seconds
     */
    public final static long HOUR_SIX_EXPIRE = 60 * 60 * 6L;
    /**
     * not set期when长
     */
    public final static long NOT_EXPIRE = -1L;

    public Long increment(String key, long expire) {
        Long increment = redisTemplate.opsForValue().increment(key, 1L);
        if (expire != NOT_EXPIRE) {
            expire(key, expire);
        }
        return increment;
    }

    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key, 1L);
    }

    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key, 1L);
    }



    public void set(String key, Object value, long expire) {
        redisTemplate.opsForValue().set(key, value);
        if (expire != NOT_EXPIRE) {
            expire(key, expire);
        }
    }

    public void set(String key, Object value) {
        set(key, value, DEFAULT_EXPIRE);
    }

    public Object get(String key, long expire) {
        Object value = redisTemplate.opsForValue().get(key);
        if (expire != NOT_EXPIRE) {
            expire(key, expire);
        }
        return value;
    }

    public Object get(String key) {
        return get(key, NOT_EXPIRE);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void delete(Collection<String> keys) {
        redisTemplate.delete(keys);
    }

    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    public Map<String, Object> hGetAll(String key) {
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        return hashOperations.entries(key);
    }

    public void hMSet(String key, Map<String, Object> map) {
        hMSet(key, map, DEFAULT_EXPIRE);
    }

    public void hMSet(String key, Map<String, Object> map, long expire) {
        redisTemplate.opsForHash().putAll(key, map);

        if (expire != NOT_EXPIRE) {
            expire(key, expire);
        }
    }

    public void hSet(String key, String field, Object value) {
        hSet(key, field, value, DEFAULT_EXPIRE);
    }

    public void hSet(String key, String field, Object value, long expire) {
        redisTemplate.opsForHash().put(key, field, value);

        if (expire != NOT_EXPIRE) {
            expire(key, expire);
        }
    }

    public void expire(String key, long expire) {
        redisTemplate.expire(key, expire, TimeUnit.SECONDS);
    }

    public void hDel(String key, Object... fields) {
        redisTemplate.opsForHash().delete(key, fields);
    }

    public void leftPush(String key, Object value) {
        leftPush(key, value, DEFAULT_EXPIRE);
    }

    public void leftPush(String key, Object value, long expire) {
        redisTemplate.opsForList().leftPush(key, value);

        if (expire != NOT_EXPIRE) {
            expire(key, expire);
        }
    }

    public Object rightPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }


    /**
     * 清emptyall Redis datalibrary allkey
     */
    public void emptyAll() {
        // Lua script FLUSHALLYesredis清emptyalllibrary command
        String luaScript =resourceUtils.loadString("lua/emptyAll.lua");

        // create DefaultRedisScript object
        DefaultRedisScript<Void> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(luaScript); // set Lua scriptcontent
        redisScript.setResultType(Void.class); // setreturnvaluetype

        // execute Lua script
        List<String> keys = Collections.emptyList(); // ifscriptnot 依赖 key，可to传入emptylist
        redisTemplate.execute(redisScript, keys);

    }

    /**
     * getinredisspecifiedkey value，ifvalueasempty，setkey defaultvalue
     * @param key redis key
     * @param defaultValue defaultvalue
     * @param expiresInSecond Expiration time
     * @return returnkey value
     */
    public String getKeyOrCreate(String key, String defaultValue,Long expiresInSecond) {
        // Lua script
        String luaScript = resourceUtils.loadString("lua/getKeyOrCreate.lua");

        DefaultRedisScript<String> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(luaScript);
        redisScript.setResultType(String.class);

        // execute Lua script
        List<String> keys = Collections.singletonList(key);
        return redisTemplate.execute(redisScript, keys, defaultValue,expiresInSecond);
    }



}