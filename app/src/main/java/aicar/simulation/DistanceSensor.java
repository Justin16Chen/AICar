package aicar.simulation;

import aicar.utils.math.JMath;
import aicar.utils.math.Vec;

public class DistanceSensor {
    private World world;
    private int maxSteps;

    public DistanceSensor(World world, int maxSteps) {
        this.world = world;
        this.maxSteps = maxSteps;
    }

    public double castRay(double x, double y, double angle) {
        angle = JMath.simplifyAngle(angle);
        Vec pos = new Vec(x, y);
        Vec tilePos = pos.intDiv(world.getTileSize()).mult(world.getTileSize());

        int startRow = (int) pos.y() / world.getTileSize();
        int startCol = (int) pos.x() / world.getTileSize();
        int rowsTraveled = 0;
        int colsTraveled = 0;
        int rayXDir = (int) Math.signum(Math.cos(angle));
        int rayYDir = (int) Math.signum(Math.sin(angle));

        // calculate line equation of raycast
        // y = tan(theta)x + yintercept
        // yintercept = y - slope * x
        double raySlope = Math.tan(angle);
        double rayYIntercept = pos.y() - raySlope * pos.x();
        Vec horizontalIntersection, verticalIntersection;

        // hideSprites();

        // 2 equations of lines: x = tilePos.x, y = tilePos.y
        // horizontal intersection is intersection of ray line with horizontal line
        // vertical intersection is intersection of ray line with vertical line
        // need to find closest intersection between raycast and either of these lines
        double verticalDistSquared = 0, horizontalDistSquared = 0;
        int steps = 0;
        while (world.getTileType(startRow + rowsTraveled, startCol + colsTraveled) == World.TileType.ROAD && steps < maxSteps) {
            if (raySlope == 0) {
                horizontalIntersection = null;
                verticalDistSquared = Double.MAX_VALUE;
            }
            else {
                double intersectY = angle < Math.PI ? tilePos.y() + world.getTileSize() : tilePos.y();
                //intersectY = tilePos.y();
                intersectY += world.getTileSize() * rowsTraveled;
                horizontalIntersection = new Vec((intersectY - rayYIntercept) / raySlope, intersectY);
                verticalDistSquared = horizontalIntersection.distSquared(pos);
            }
            double intersectX = angle > 1.5 * Math.PI || angle < 0.5 * Math.PI ? tilePos.x() + world.getTileSize() : tilePos.x();
            //intersectX = tilePos.x();
            intersectX += world.getTileSize() * colsTraveled;
            verticalIntersection = new Vec(intersectX, raySlope * intersectX + rayYIntercept);
            horizontalDistSquared = verticalIntersection.distSquared(pos);

            if (verticalDistSquared < horizontalDistSquared) {
                rowsTraveled += rayYDir;
            }
            else {
                colsTraveled += rayXDir;
            }

            steps++;   
        }
        return Math.sqrt(Math.min(verticalDistSquared, horizontalDistSquared));
    }
}
