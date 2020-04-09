package com.redis.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentTest {
    //请求次数
    private int requestQty =100000;
    //倒计时器，当发送requestQty次请求后继续执行主线程
    private CountDownLatch latch = new CountDownLatch(requestQty);

    //记录请求落在数据库上的次数
    private AtomicInteger dbSelectCount = new AtomicInteger();
    //记录请求落在缓存中的次数
    private AtomicInteger cacheSelectCount = new AtomicInteger();
    //用HashMap模拟缓存储存
    private Map<String, String> cache = new HashMap<>();

    public static void main(String[] args) {
        new ConcurrentTest().go();
    }
    private void go() {
        //同时创建一万个线程获取
        for (int i = 0; i < requestQty; i++) {
            new Thread(() -> {
                this.getGoodsDetail("商品id");
                latch.countDown();
            }).start();
        }

        //计数器大于0时，await()方法会阻塞程序继续执行
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("数据库查询次数："+dbSelectCount.get());
        System.out.println("缓存库查询次数："+cacheSelectCount.get());
    }

    /**
     * @Description: 获取商品数据
     * @Param: [key 商品id]
     * @return: java.lang.String
     * @Author: YaoHuan
     * @Date: 2020/4/3
    */
    public String getGoodsDetail(String key) {
        //从缓存查询，存在则直接返回
        String data = this.selectCache(key);
        if (data != null) {
            cacheSelectCount.addAndGet(1);//记录次数
            return data;
        }

        //不存在则从数据库查询且将数据放入缓存
        data = this.selectDB(key);
        cache.put(key, data);
        return data;
    }

    /**
     * 从缓存中获取数据
     *
     * @param key
     * @return
     */
    public String selectCache(String key) {

        System.out.println(Thread.currentThread().getId() + " 从cache获取数据====");
        return cache.get(key);
    }

    /**
     * 从数据库中获取数据
     *
     * @param key
     * @return
     */
    public String selectDB(String key) {
//        sleep(100);//模拟查询数据库花费100ms
        dbSelectCount.addAndGet(1);//记录次数

        System.out.println(Thread.currentThread().getId() + " 从db获取数据====");
        return "数据中的数据";
    }

    private static void sleep(long m) {
        try {
            Thread.sleep(m);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
