package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/test")
public class TestController {
    protected static Logger logger = LoggerFactory.getLogger(TestController.class);

    private static final String LOCK = "lock";
    @Autowired
    private StringRedisTemplate redisTemplate;

    @RequestMapping(value = "/lock",method = RequestMethod.GET)
    public String testLock() {
        //防止服务器重启或者崩溃出现的死锁
        String value = UUID.randomUUID().toString();
        Boolean hasLock = redisTemplate.opsForValue().setIfAbsent(LOCK, value,30, TimeUnit.SECONDS);
        try {
            if (!hasLock) {
                return "";
            }
            int stock = Integer.parseInt(redisTemplate.opsForValue().get("stock"));
            if (stock <= 0) {
                logger.info("库存不足");
            } else {
                stock-=1;
                redisTemplate.opsForValue().set("stock",stock+"");
                logger.info("执行减库存操作，剩余库存为:"+stock);
            }
        }finally {
            //防止由于锁失效导致误删其他线程的锁
            if (redisTemplate.opsForValue().get(LOCK).equals(value)) {
                redisTemplate.delete(LOCK);
            }
        }

        return "End";
    }
}
