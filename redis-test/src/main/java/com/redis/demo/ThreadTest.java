package com.redis.demo;

import org.junit.Test;

/**
 * @author YaoHuan
 * @Description TODO
 * @Date 2020/4/9 17:23
 */
public class ThreadTest {
    private static final long COUNT=100000L;

    public static void main(String[] args) throws InterruptedException {
        concurrency();
        serial();

    }
    public static void concurrency() throws InterruptedException {
        long start = System.currentTimeMillis();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int a = 0;
                for (long i = 0; i < COUNT; i++) {
                    a += 5;
                }
            }
        });
        thread.start();

        int b=0;
        for (int i = 0; i < COUNT; i++) {
            b--;
        }
        long time = System.currentTimeMillis() - start;
        thread.join();
        System.out.println("concurrency :" + time + "ms, b" + b);
    }

    public static void serial(){
        long start = System.currentTimeMillis();

        int a = 0;
        for (long i = 0; i < COUNT; i++) {
            a += 5;
        }
        int b=0;
        for (int i = 0; i < COUNT; i++) {
            b--;
        }

        long time = System.currentTimeMillis() - start;
        System.out.println("serial :" + time + "ms, b" + b);

    }
}
