package aicar.utils.math;

import java.text.DecimalFormat;

public class Vec {
    public static Vec createFromPolar(double radius, double theta) {
        return new Vec(radius * Math.cos(theta), radius * Math.sin(theta));
    }
    
    private static DecimalFormat df = new DecimalFormat("0.000");

    private double x, y;

    public Vec(double x, double y) {
        this.x = x;
        this.y = y;
    }
    public Vec(double n) {
        this.x = n;
        this.y = n;
    }

    public double x() {
        return x;
    }
    public double y() {
        return y;
    }
    public void setX(double x) {
        this.x = x;
    }
    public void setY(double y) {
        this.y = y;
    }
    public double getMag() {
        return Math.sqrt(x * x + y * y);
    }
    public Vec setMag(double mag) {
        Vec unitVec = normalize();
        return new Vec(unitVec.x() * mag, unitVec.y() * mag);
    }
    public double dist(Vec v) {
        return Math.sqrt(distSquared(v));
    }
    public double distSquared(Vec v) {
        return Math.pow(x - v.x, 2) + Math.pow(y - v.y, 2);
    }
    public Vec normalize() {
        return this.div(getMag());
    }
    public Vec add(Vec v) {
        return new Vec(x + v.x(), y + v.y());
    }
    public Vec add(double n) {
        return new Vec(x + n, y + n);
    }
    public Vec sub(Vec v) {
        return new Vec(x - v.x(), y - v.y());
    }
    public Vec sub(double n) {
        return new Vec(x - n, y - n);
    }
    public Vec mult(Vec v) {
        return new Vec(x * v.x(), y * v.y());
    }
    public Vec mult(double n) {
        return new Vec(x * n, y * n);
    }
    public Vec div(Vec v) {
        return new Vec(x / v.x(), y / v.y());
    }
    public Vec div(double n) {
        return new Vec(x / n, y / n);
    }
    public Vec intDiv(double n) {
        return new Vec(Math.floor(x / n), Math.floor(y / n));
    }
    public Vec rotate(double angleRadians) {
        double cos = Math.cos(angleRadians);
        double sin = Math.sin(angleRadians);
        double newX = x * cos - y * sin;
        double newY = x * sin + y * cos;
        return new Vec(newX, newY);
    }
    public double getAngle() {
        return Math.atan2(y, x);
    }

    @Override
    public String toString() {
        return df.format(x) + ", " + df.format(y);
    }

    @Override
    public Vec clone() {
        return new Vec(x, y);
    }
}
