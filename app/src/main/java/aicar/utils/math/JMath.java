package aicar.utils.math;

import java.awt.Color;

public class JMath {
    public static final double THRESHOLD = 0.000001;
    public static double roundToThreshold(double num) {
        return Math.round(num / THRESHOLD) * THRESHOLD;
    }
    public static boolean areTheSame(double n1, double n2) {
        return Math.abs(n1 - n2) <= THRESHOLD;
    }
    public static int clamp(int x, int max, int min) {
        return Math.max(min, Math.min(max, x));
    }
    public static double clamp(double x, double max, double min) {
        return Math.max(min, Math.min(max, x));
    }

    // only works if x is less than y, but more performant than lerp
    public static double simpleLerp(double x, double y, double a) {
        return x + (y - x) * a;
    }

    // works for any values of x and y
    public static double lerp(double x, double y, double a) {
        // prevent rounding errors
        if (a < THRESHOLD)
            return x;
        if (a > 1 - THRESHOLD) {
            return y;
        }

        // actually calculate lerp
        return x * (1 - a) + y * a;
    }

    // creates a new color based on the starting and ending colors
    public static Color lerpColor(Color x, Color y, double a) {
        return new Color(
            (int) lerp(x.getRed(), y.getRed(), a),
            (int) lerp(x.getGreen(), y.getGreen(), a),
            (int) lerp(x.getBlue(), y.getBlue(), a)
        );
    }

    // normalizes a number between 0 and 1 given a min and max
    public static double normalize(double min, double max, double num) {
        return (num - min) / (max - min);
    }

    // uses modulo to find corresponding number within a certain period
    public static double modNumberInPeriod(double num, double period) {
        return (num % period + period) % period;
    }
    public static int modNumberInPeriod(int num, int period) {
        return (num % period + period) % period;
    }

    // forces angle to be between 0 and 2PI and then rounds it
    public static double simplifyAngle(double angle) {
        return roundToThreshold(modNumberInPeriod(angle, Math.PI * 2));
    }
    
    // rotates point by radians radains
    public static Vec rotateAroundPoint(Vec point, Vec origin, double radians) {
        if (radians == 0)
            return point.clone();
        radians = simplifyAngle(radians);
        Vec relative = point.sub(origin);
        double relativeMag = relative.getMag();
        double theta = Math.atan2(relative.y(), relative.x()) + radians;
        double newX = origin.x() + Math.cos(theta) * relativeMag;
        double newY = origin.y() + Math.sin(theta) * relativeMag;
        return new Vec(newX, newY);
    }
    public static double[] rotateAroundPoint(double x, double y, double originX, double originY, double radians) {
        Vec v = rotateAroundPoint(new Vec(x, y), new Vec(originX, originY), radians);
        return new double[]{ v.x(), v.y() };
    }
    public static Vec rotateOrthogonalAroundPoint(Vec point, Vec origin, int numQuadrants) {
        Vec relative = point.sub(origin);
        numQuadrants = modNumberInPeriod(numQuadrants, 4);
        switch (numQuadrants) {
            case 0: return point.clone();
            case 1: return new Vec(relative.y(), -relative.x());
            case 2: return new Vec(-relative.x(), -relative.y());
            case 3: return new Vec(-relative.y(), relative.x());
            default: return point.clone();
        }
    }
    public static double[] rotateOrthogonalAroundPoint(double x, double y, double originX, double originY, int numQuadrants) {
        Vec v = rotateOrthogonalAroundPoint(new Vec(x, y), new Vec(originX, originY), numQuadrants);
        return new double[] { v.x(), v.y() };
    }

    public static double sigmoid(double x) {
        return 1 / (1 + Math.pow(Math.E, -x));
    }
}
