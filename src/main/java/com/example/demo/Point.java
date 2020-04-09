package com.example.demo;

public class Point {
    double x;
    double y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
        System.out.print("(" + x + "," + y + ") ");
    }

    public static Point instance(int x, int y) {
        return new Point(x, y);
    }
}
