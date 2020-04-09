package com.example.demo;

public class SingleTonClass1 {
    private static volatile  SingleTonClass1 instance;
    public  SingleTonClass1 getInstance(){
        if (instance == null) {
            synchronized (this) {
                if (instance==null) {
                    instance = new SingleTonClass1();
                }
            }
        }

        return instance;
    }

}
