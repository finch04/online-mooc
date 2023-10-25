package com.itheima;


import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorTest {
    public static void main(String[] args) {
//        Executors.newFixedThreadPool(3);//创建固定线程数的线程池
//        Executors.newSingleThreadExecutor();//创建单线程的线程池
//        Executors.newCachedThreadPool();//创建缓存线程池
//        Executors.newScheduledThreadPool();//创建可以延迟执行线程池

        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(3,
                5,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(60));
        //建议1：如果任务属于cpu运算任务，推荐核心线程为cpu的核数
        //建议2：如果任务属于io型的，推荐核心线程为cpu核数的2倍

        poolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                //任务
            }
        });

    }
}
