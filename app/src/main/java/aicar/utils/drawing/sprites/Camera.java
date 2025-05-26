package aicar.utils.drawing.sprites;

import aicar.utils.math.JMath;
import aicar.utils.math.Vec;

public class Camera {
    private static final double FOLLOW_TIGHTNESS = 1;

    private int screenWidth, screenHeight;
    private int worldWidth, worldHeight;
    private double x, y;

    public Camera(int screenWidth, int screenHeight, double x, double y) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.x = x - screenWidth * 0.5;
        this.y = y - screenHeight * 0.5;
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
    public int getScreenWidth() {
        return screenWidth;
    }
    public int getScreenHeight() {
        return screenHeight;
    }

    public void setWorldBounds(int worldWidth, int worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    public void follow(double x, double y) {
        this.x = JMath.lerp(this.x, x - screenWidth * 0.5, FOLLOW_TIGHTNESS);
        this.y = JMath.lerp(this.y, y - screenHeight * 0.5, FOLLOW_TIGHTNESS);

        this.x = Math.max(0, Math.min(worldWidth - screenWidth, this.x));
        this.y = Math.max(0, Math.min(worldHeight - screenHeight, this.y));
    }

    public boolean onScreen(int worldX, int worldY, int width, int height) {
        Rect screenRect = getScreenRect(worldX, worldY, width, height);
        return screenRect.x() > -width && screenRect.x() < screenWidth + width && screenRect.y() > -height && screenRect.y() < screenHeight + height; // the width and height are buffered on both sides so it won't matter where (0, 0) is on the sprite
    }
    public Rect getScreenRect(int worldX, int worldY, int width, int height) {
        double screenX = worldX - x;
        double screenY = worldY - y;
        return new Rect((int) screenX, (int) screenY, (int) width, (int) height);
    }
    public double screenXToWorld(double screenX) {
        return screenX + x;
    }
    public double screenYToWorld(double screenY) {
        return screenY + y;
    }
}
