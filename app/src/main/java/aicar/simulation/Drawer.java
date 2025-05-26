package aicar.simulation;

import javax.swing.JPanel;

import aicar.utils.drawing.sprites.Camera;
import aicar.utils.drawing.sprites.Sprites;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
public class Drawer extends JPanel {

    // layers that move with the camera
    private static final ArrayList<String> WORLD_LAYERS = new ArrayList<>(Arrays.asList("world", "car"));

    private Camera camera;

    public void setupLayers() {
        Sprites.addLayer("world", 0);
        Sprites.addLayer("car", 1);
        Sprites.addLayer("ui", 2);
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.clearRect(0, 0, getWidth(), getHeight());

        if (camera == null)
            Sprites.drawSprites(g2);
        else
            Sprites.drawSprites(g2, camera, WORLD_LAYERS);
        
        g2.dispose();
    }
}
