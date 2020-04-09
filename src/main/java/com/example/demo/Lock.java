package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 */
public class Lock {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired(required = false)
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    public final String LOCK_PREFIX = "REDIS_LOCK";

    private final Long LOCK_EXPIRE = 30 * 1000L;

    private final Long OVER_TIME = 10L;

    private Map<String, ScheduledFuture<?>> futureMap = new ConcurrentHashMap<>();

    private RedisProperties.Jedis jedis;

    public Lock() {
    }

    private ReentrantLock reentrantLock;

    /**
     * 给线程枷锁
     *
     * @param key
     */
    public void lock(String key) {
        //自旋获取锁
        while (true) {
            if (setLock(key)) {//拿锁成功
                //获取锁后开启任务
                threadPoolTaskScheduler.schedule(()->{
                    Set<String> keys = scan(LOCK_PREFIX);
                    Iterator<String> iterator = keys.iterator();
                    //遍历所有的key 延长key的时间
                    while (iterator.hasNext()) {
                        log.info("执行动态定时任务: " + LocalDateTime.now().toLocalTime());
                        redisUtils.expire(key, Long.valueOf(OVER_TIME), TimeUnit.SECONDS);//延长时间（秒）
                    }
                },new Trigger(){
                    @Override
                    public Date nextExecutionTime(TriggerContext triggerContext){
                        return new CronTrigger("0/10 * * * * ?").nextExecutionTime(triggerContext);
                    }
                });
                return;
            }
        }
    }

    /**
     * setnx
     *
     * @param key
     * @return
     */
    public boolean setLock(String key) {
        String lock = LOCK_PREFIX + key;
        return (Boolean) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                long expireAt = System.currentTimeMillis() + LOCK_EXPIRE + 1;
                Boolean acquire = redisConnection.setNX(lock.getBytes(), String.valueOf(expireAt).getBytes());
                if (acquire) {
                    return true;
                } else {
                    byte[] value = redisConnection.get(lock.getBytes());
                    if (Objects.nonNull(value) && value.length > 0) {
                        long expireTime = Long.parseLong(new String(value));
                        if (expireTime < System.currentTimeMillis()) {
                            // 如果锁已经过期
                            byte[] oldValue = redisConnection.getSet(lock.getBytes(), String.valueOf(System.currentTimeMillis() + LOCK_EXPIRE + 1).getBytes());
                            // 防止死锁
                            return Long.parseLong(new String(oldValue)) < System.currentTimeMillis();
                        }
                    }
                }
                return false;
            }
        });
    }

    /**
     * 删除锁
     *
     * @param key
     */
    public void unlock(String key) {
        String lock = LOCK_PREFIX + key;
        synchronized (this) {
            futureMap.get(lock).cancel(true);//停止任务
            redisTemplate.delete(lock);
        }
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Set<String> scan(String key) {
        return (Set<String>) redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = Sets.newHashSet();

            JedisCommands commands = (JedisCommands) connection.getNativeConnection();
            MultiKeyCommands multiKeyCommands = (MultiKeyCommands) commands;

            ScanParams scanParams = new ScanParams();
            scanParams.match("*" + key + "*");
            scanParams.count(1000);
            ScanResult<String> scan = multiKeyCommands.scan("0", scanParams);
            while (null != scan.getStringCursor()) {
                keys.addAll(scan.getResult());
                if (!StringUtils.equals("0", scan.getStringCursor())) {
                    scan = multiKeyCommands.scan(scan.getStringCursor(), scanParams);
                    continue;
                } else {
                    break;
                }
            }

            return keys;
        });
    }

}
