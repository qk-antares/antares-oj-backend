package com.antares.oj.unsafe;

/**
 * 无限睡眠（阻塞程序执行）
 */
public class SleepError {
    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(60 * 1000);
    }
}
