package aicar.simulation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.text.DecimalFormat;

import aicar.utils.drawing.sprites.Sprite;
import aicar.utils.drawing.sprites.UI;
import aicar.utils.math.Vec;

public class Car extends Sprite {
    private static final DecimalFormat df = new DecimalFormat("#.###");
    private static final String CAR_IMAGE_PATH = "/textures/car.png";
    private static final double LINEAR_ACCEL_SCALAR = 0.15, PREV_LINEAR_ACCEL_SCALAR = 0.85, MAX_SPEED = 6, 
        ANGULAR_ACCEL_SCALAR = 0.002, PREV_ANGULAR_ACCEL_SCALAR = 0.8, MAX_ANGULAR_SPEED = 0.03;
    public static final int NUM_SENSORS = 5, MAX_SENSOR_DIST = 3000;
    private static final double SENSOR_ANGLE_SPREAD = Math.toRadians(25);
    private World world;
    private Vec pos;
    private double angle;
    private double linearVel, angularVel;
    private double linearAccel, angularAccel;
    private double frictionCoefficient, angularFrictionCoefficient;
    private DistanceSensor[] distanceSensors;
    private double distances[];
    private Sprite centerSprite;
    private Sprite distanceLinesSprite;

    public Car(World world, Vec pos, double heading) {
        super("carNew", CAR_IMAGE_PATH, (int) pos.x(), (int) pos.y(), 36, 36, "car");
        this.world = world;
        angle = heading;
        setAngle(heading);
        this.pos = pos;
        linearVel = 0;
        angularVel = 0;
        linearAccel = 0;
        angularAccel = 0;
        frictionCoefficient = .95;
        angularFrictionCoefficient = .8;
        
        centerSprite = new Sprite("car center", 0, 0, 4, 4, "car");
        centerSprite.setColor(Color.GREEN);
        
        distances = new double[NUM_SENSORS];
        distanceSensors = new DistanceSensor[NUM_SENSORS];
        for (int i=0; i<distanceSensors.length; i++)
            distanceSensors[i] = new DistanceSensor(world, MAX_SENSOR_DIST / world.getTileSize());
        
        new Sprite("car ui", "ui") {
            @Override
            public void drawSelf(Graphics2D g, int x, int y, int w, int h, double a) {
                g.setColor(UI.BG_COLOR);
                g.fillRect(0, 0, 120, 250);
                g.setColor(UI.TEXT_COLOR);
                int textX = 5;
                g.drawString("Car Properties", textX, 20);
                for (int i=0; i<distances.length; i++)
                    g.drawString("dist " + i + ": " + df.format(distances[i]), textX, 40 + i * 20);
                
                g.drawString("linear accel: " + df.format(linearAccel), textX, 160);
                g.drawString("linear vel: " + df.format(linearVel), textX, 180);

                g.drawString("angular accel: " + df.format(Math.toDegrees(angularAccel)), textX, 200);
                Vec velocity = Vec.createFromPolar(linearVel, angle);
                g.drawString("vel: " + df.format(velocity.x()) + ", " + df.format(velocity.y()), textX, 240);
                g.drawString("angular vel: " + df.format(angularVel), textX, 220);
            }
        };
        distanceLinesSprite = new Sprite("distance lines", "car") {
            @Override
            public void drawSelf(Graphics2D g, int x, int y, int w, int h, double a) {
                g.setColor(Color.RED);
                for (int i=0; i<distanceSensors.length; i++) {
                    double angle = getAngle() + (i - distanceSensors.length / 2) * SENSOR_ANGLE_SPREAD;
                    g.drawLine(x, y, x + (int) (Math.cos(angle) * distances[i]), y + (int) (Math.sin(angle) * distances[i]));
                }
            }
        };
    }

    public void setFrictionCoefficient(double friction) {
        frictionCoefficient = friction;
    }
    public void setAngularFrictionCoefficient(double friction) {
        angularFrictionCoefficient = friction;
    }

    public double[] getVelocity() {
        return new double[]{linearVel, angularVel};
    }
    public double[] getAcceleration() {
        return new double[]{linearAccel, angularAccel};
    }

    // i cannot directly control acceleration (no feasible way to with a keyboard)
    // i can only give quantized accelerations - model should recieve continuous spectrum of accelerations
    // drivePower and turnPower can only be {-1, 0, 1}
    public void recieveHumanInput(double drivePower, double turnPower) {
        // allow accerations of previous frames to affect current accelerations
        linearAccel = drivePower * LINEAR_ACCEL_SCALAR + linearAccel * PREV_LINEAR_ACCEL_SCALAR; 
        angularAccel = turnPower * ANGULAR_ACCEL_SCALAR + angularAccel * PREV_ANGULAR_ACCEL_SCALAR;

        recieveAccelerations(linearAccel, angularAccel);
    }

    // model can directly control acceleration
    public void recieveAccelerations(double linearAccel, double angularAccel) {
        this.linearAccel = linearAccel;
        linearVel += linearAccel;
        linearVel *= frictionCoefficient;
        linearVel = Math.max(Math.min(MAX_SPEED, linearVel), -MAX_SPEED);

        this.angularAccel = angularAccel;
        angularVel += angularAccel;
        angularVel *= angularFrictionCoefficient;
        angularVel = Math.max(Math.min(MAX_ANGULAR_SPEED, angularVel), -MAX_ANGULAR_SPEED);

        angle += angularVel;
        Vec velocity = Vec.createFromPolar(linearVel, angle);
        pos = pos.add(velocity);
    }

    public double[] getDistances() {
        for (int i=0; i<distanceSensors.length; i++) {
            double angle = getAngle() + (i - distanceSensors.length / 2) * SENSOR_ANGLE_SPREAD;
            distances[i] = distanceSensors[i].castRay(getCenterX(), getCenterY(), angle);
        }
        return distances;
    }

    // returns if car respawned or not
    public boolean updateCollisions() {
        if (getTile() == World.TileType.WALL || getTile() == World.TileType.OUT_OF_BOUNDS) {
            respawn(world.getSpawnPosition(), world.getSpawnAngle());
            updateSprites();
            return true;
        }
        return false;
    }

    private World.TileType getTile() {
        return world.getTileType((int) (pos.y() / world.getTileSize()), (int) (pos.x() / world.getTileSize()));
    }

    private void respawn(Vec respawnPosition, double respawnAngle) {
        pos = respawnPosition;
        angle = respawnAngle;
        setAngle(respawnAngle);
        linearVel = 0;
        angularVel = 0;
        linearAccel = 0;
        angularAccel = 0;
        angularVel = 0;
    }

    public void updateSprites() {
        setCenterX((int) pos.x());
        setCenterY((int) pos.y());
        setAngle(angle);
        centerSprite.setCenterPosition(getCenterX(), getCenterY());

        distanceLinesSprite.setAngle(getAngle());
        distanceLinesSprite.setX(getCenterX());
        distanceLinesSprite.setY(getCenterY());
    }
}
