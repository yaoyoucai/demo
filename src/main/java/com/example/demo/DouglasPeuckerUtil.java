package com.example.demo;

import java.util.ArrayList;
import java.util.List;

public class DouglasPeuckerUtil {

    public static void main(String[] args) {

        System.out.print("原始坐标：");

        List<Point> points = new ArrayList<>();
        List<Point> result = new ArrayList<>();

        points.add(Point.instance(1, 1));
        points.add(Point.instance(2, 2));
        points.add(Point.instance(3, 4));
        points.add(Point.instance(4, 1));
        points.add(Point.instance(5, 0));
        points.add(Point.instance(6, 3));
        points.add(Point.instance(7, 5));
        points.add(Point.instance(8, 2));
        points.add(Point.instance(9, 1));
        points.add(Point.instance(10, 6));

        System.out.println("");
        System.out
                .println("=====================================================================");
        System.out.print("抽稀坐标：");

        result = DouglasPeucker(points, 1);

        for (Point p : result) {
            System.out.print("(" + p.x + "," + p.y + ") ");
        }
    }

    public static List<Point> DouglasPeucker(List<Point> points, int epsilon) {
        // 找到最大阈值点，即操作（1）
        double maxH = 0;
        int index = 0;
        int end = points.size();
        for (int i = 1; i < end - 1; i++) {
            double h = H(points.get(i), points.get(0), points.get(end - 1));
            if (h > maxH) {
                maxH = h;
                index = i;
            }
        }

        // 如果存在最大阈值点，就进行递归遍历出所有最大阈值点
        List<Point> result = new ArrayList<>();
        if (maxH > epsilon) {
            List<Point> leftPoints = new ArrayList<>();// 左曲线
            List<Point> rightPoints = new ArrayList<>();// 右曲线
            // 分别提取出左曲线和右曲线的坐标点
            for (int i = 0; i < end; i++) {
                if (i <= index) {
                    leftPoints.add(points.get(i));
                    if (i == index)
                        rightPoints.add(points.get(i));
                } else {
                    rightPoints.add(points.get(i));
                }
            }

            // 分别保存两边遍历的结果
            List<Point> leftResult = new ArrayList<>();
            List<Point> rightResult = new ArrayList<>();
            leftResult = DouglasPeucker(leftPoints, epsilon);
            rightResult = DouglasPeucker(rightPoints, epsilon);

            // 将两边的结果整合
            rightResult.remove(0);//移除重复点
            leftResult.addAll(rightResult);
            result = leftResult;
        } else {// 如果不存在最大阈值点则返回当前遍历的子曲线的起始点
            result.add(points.get(0));
            result.add(points.get(end - 1));
        }
        return result;
    }

    /**
     * 计算点到直线的距离
     *
     * @param p
     * @param s
     * @param e
     * @return
     */
    public static double H(Point p, Point s, Point e) {
        double AB = distance(s, e);
        double CB = distance(p, s);
        double CA = distance(p, e);

        double S = helen(CB, CA, AB);
        double H = 2 * S / AB;

        return H;
    }

    /**
     * 计算两点之间的距离
     *
     * @param p1
     * @param p2
     * @return
     */
    public static double distance(Point p1, Point p2) {
        double x1 = p1.x;
        double y1 = p1.y;

        double x2 = p2.x;
        double y2 = p2.y;

        double xy = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        return xy;
    }

    /**
     * 海伦公式，已知三边求三角形面积
     *
     * @return 面积
     */
    public static double helen(double CB, double CA, double AB) {
        double p = (CB + CA + AB) / 2;
        double S = Math.sqrt(p * (p - CB) * (p - CA) * (p - AB));
        return S;
    }
}