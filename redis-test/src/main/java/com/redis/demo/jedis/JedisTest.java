package com.redis.demo.jedis;

import org.junit.Test;
import redis.clients.jedis.Jedis;

/**
 * @author YaoHuan
 * @version V1.0
 * @ClassName JedisTest
 * @Description Jedis测试类
 * @Date 2020/4/7 10:46
 */
public class JedisTest {

    @Test
    public void test(){
        Jedis jedis = new Jedis("127.0.0.1",6379);
        jedis.set("myvalue", "123456");
        String myvalue = jedis.get("myvalue");
        System.out.println(myvalue);
        jedis.close();
    }

    public void sdds(){

    }

}
