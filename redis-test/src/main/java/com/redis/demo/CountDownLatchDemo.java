package com.redis.demo;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author YaoHuan
 * @version V1.0
 * @ClassName CountDownLatchDemo
 * @Description 倒计时示例：火箭发射
 * @Date 2020/4/3 17:29
 */
public class CountDownLatchDemo implements Runnable {
    static final CountDownLatch latch = new CountDownLatch(15);
    static final CountDownLatchDemo demo = new CountDownLatchDemo();

    @Override
    public void run() {
        try {
            Thread.sleep(new Random().nextInt(10) * 1000);
            System.out.println("check complete");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            latch.countDown();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ExecutorService exec= Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            exec.submit(demo);
        }

        // 等待检查
        latch.await();

        System.out.println("Fire");
        exec.shutdown();
    }
}
