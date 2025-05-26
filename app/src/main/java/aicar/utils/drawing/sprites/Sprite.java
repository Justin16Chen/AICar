package aicar.utils.drawing.sprites;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import aicar.utils.Print;
import aicar.utils.math.JMath;

public class Sprite extends TaggableChild<Sprite> {

    private String name, layerName;
    private BufferedImage image;
    private int x, y, width, height;
    private double angle; //angle is in radians
    private boolean visible;
    private Color color;

    public Sprite(String name, String layerName) {
        this.name = name;
        this.layerName = layerName;
        
        x = 0;
        y = 0;
        width = 1;
        height = 1;
        angle = 0;
        image = null;
        visible = true;
        color = Color.BLACK;
        Sprites.addSprite(this, layerName);
    }

    public Sprite(String name, String imagePath, String layerName) {
        this.name = name;
        setImagePath(imagePath);
        this.layerName = layerName;

        x = 0;
        y = 0;
        width = 1;
        height = 1;
        angle = 0;
        visible = true;
        color = Color.BLACK;
        Sprites.addSprite(this, layerName);
    }
    public Sprite(String name, int x, int y, int width, int height, String layerName) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        angle = 0;
        this.layerName = layerName;

        image = null;
        visible = true;
        color = Color.BLACK;
        Sprites.addSprite(this, layerName);
    }
    public Sprite(String name, String imagePath, int x, int y, int width, int height, String layerName) {
        this.name = name;
        setImagePath(imagePath);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        angle = 0;
        this.layerName = layerName;

        visible = true;
        color = Color.BLACK;
        Sprites.addSprite(this, layerName);
    }


    public String getName() { return name; }
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public void setPosition(int x, int y) { this.x = x; this.y = y; }
    public int getCenterX() { return x + (int) (width * 0.5);}
    public void setCenterX(int cx) { x = cx - (int) (width * 0.5); }
    public int getCenterY() { return y + (int) (height * 0.5);}
    public void setCenterY(int cy) { y = cy - (int) (height * 0.5); }
    public void setCenterPosition(int cx, int cy) { setCenterX(cx); setCenterY(cy); }
    public int getRight() { return x + width; }
    public void setRight(int right) { x = right - width; }
    public int getBottom() { return y + height; }
    public void setBottom(int bottom) { y = bottom - height; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    public void setDimensions(int width, int height) { this.width = width; this.height = height; }
    public double getAngle() { return angle; }
    public void setAngle(double angle) { this.angle = angle; }
    public boolean isVisible() { return visible; }

    public void setVisible(boolean visible) { 
        this.visible = visible; 
    }
    public String getLayerName() { return layerName; }
    
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }
    public BufferedImage getImage() { return image; }
    public void setImagePath(String imagePath) { 
        try {
            image = ImageIO.read(getClass().getResourceAsStream(imagePath));
        } catch (IOException e) {
            e.printStackTrace();
            Print.println("Failed to load image " + imagePath, Print.RED);
        }
    }
    public void setImage(BufferedImage image) {
        this.image = image;
    }

    // only moves this to layerName
    public void moveToLayer(String layerName) {
        Sprites.addSprite(this, layerName);
        Sprites.deleteSprite(this);
    }
    // sets the visibility of this and all of its children
    public void setAllChildrenVisible(boolean visible) { 
        setVisible(visible);
        for (Sprite child : getDirectChildren())
            child.setAllChildrenVisible(visible);
    }
    // sets the visibility of this and all of its children with matching tag
    public void setAllChildrenVisible(boolean visible, String tag) {
        if (hasTag(tag))
            setVisible(visible);
        for (Sprite child : getDirectChildren())
            child.setAllChildrenVisible(visible, tag);
    }
    // moves this and all children to layerName
    public void moveAllChildrenToLayer(String layerName) {
        moveToLayer(layerName);
        for (Sprite child : getDirectChildren())
            child.moveAllChildrenToLayer(layerName);
    }
    
    // assumes x and y are coordinates to draw to screen
    public final void draw(Graphics2D g) {
        if (isVisible() && width > 0 && height > 0) {
            angle = JMath.simplifyAngle(angle);
            drawSelf(g, x, y, width, height, angle);
        }
    }

    // assumes x and y are world coordinates - screen coordinates are found based on camera position
    public final void draw(Graphics2D g, Camera camera) {
        if (isVisible() && width > 0 && height > 0 && camera.onScreen(x, y, Math.max(width, height), Math.max(width, height))) { // I do Math.max(width, height) to account for any possible rotation (the extra pixels do not matter much)
            Rect drawRect = camera.getScreenRect(x, y, width, height);
            angle = JMath.simplifyAngle(angle);
            drawSelf(g, drawRect.x(), drawRect.y(), drawRect.width(), drawRect.height(), angle);
        }
    }

    public void drawSelf(Graphics2D g, int x, int y, int width, int height, double angle) {
        if (angle == 0) {
            if (image == null) {
                g.setColor(color);
                g.fillRect(x, y, width, height);
            }
            else {
                g.drawImage(image, x, y, width, height, null);
            }
        }
        else {
            AffineTransform old = g.getTransform();
            AffineTransform transform = new AffineTransform();
            transform.translate(x + width / 2, y + height / 2);
            transform.rotate(angle);
            transform.translate(-width / 2., -height / 2.);
            
            if (image == null) {
                g.setTransform(transform);
                g.setColor(color);
                g.fillRect(x, y, width, height);
            }
            else {
                transform.scale(width * 1. / image.getWidth(), height * 1. / image.getHeight());
                g.setTransform(transform);
                g.drawImage(image, 0, 0, null);
            }
            g.setTransform(old);
        }
        // if (image == null) {
        //     g.setColor(color);
        //     g.fillRect(x, y, width, height);
        // }
        // else if (angle == 0) {
        //     g.drawImage(image, x, y, width, height, null);
        // }
        // else {
        //     AffineTransform transform = new AffineTransform();
        //     transform.translate(x + width / 2, y + height / 2);
        //     transform.rotate(angle);
        //     transform.translate(-width / 2., -height / 2.);
        //     transform.scale(width * 1. / image.getWidth(), height * 1. / image.getHeight());
        //     g.drawImage(image, transform, null);
        // }
    }

    @Override
    public boolean equals(Object obj) {
        Sprite sprite = (Sprite) obj;
        return name.equals(sprite.getName()) && x == sprite.x && y == sprite.y && width == sprite.width && height == sprite.height && layerName.equals(sprite.getLayerName());
    }
    @Override
    public String toString() {
        String visibleString = visible ? "shown" : "hidden"; 
        return "Sprite(" + name + " (" +  x + ", " + y + ") | " + width + "x" + height + " | " + layerName + " layer | " + visibleString + ")";
    }
}
