package com.example.demo;

public class SingleTonClass2 {
    private static class SingleConnectionFactory{
         public static SingleTonClass2 instance;
    }

    public static SingleTonClass2 getInstance() {
        return SingleConnectionFactory.instance;
    }
}
