package aicar.utils.drawing.sprites;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;

public class Sprites {

    private static HashMap<Integer, String> layers = new HashMap<>();
    private static HashMap<String, ArrayList<Sprite>> sprites = new HashMap<>();

    
    public static ArrayList<Sprite> getSprites() {
        ArrayList<Sprite> allSprites = new ArrayList<>();
        for (String layerName : layers.values()) 
            allSprites.addAll(sprites.get(layerName));
        return allSprites;
    }

    public static ArrayList<Sprite> getSprites(ArrayList<String> layersToInclude) {
        ArrayList<Sprite> allSprites = new ArrayList<>();
        for (String layerName : layers.values()) 
            if (layersToInclude.contains(layerName))
                allSprites.addAll(sprites.get(layerName));
        return allSprites;
    }

    public static void addLayer(String layerName, int layerNumber) {
        if (layers.containsKey(layerNumber)) 
            throw new IllegalArgumentException("Layer " + layerName + "on layer " + layerNumber + " already exists");
        
        layers.put(layerNumber, layerName);
        sprites.put(layerName, new ArrayList<Sprite>());
    }
    protected static void addSprite(Sprite sprite, String layerName) {
        if (!layers.values().contains(layerName)) 
            throw new IllegalArgumentException("Layer " + layerName + " does not exist");
        sprites.get(layerName).add(sprite);
    }

    public static void deleteSprite(Sprite spriteToDelete) {
        for (String layerName : layers.values()) 
            for (Sprite sprite : sprites.get(layerName)) 
                if (sprite.equals(spriteToDelete)) {
                    sprites.get(layerName).remove(sprite);
                    return;
                }
    }

    public static void deleteSprites(ArrayList<Sprite> spritesToDelete) {
        for (Sprite sprite : spritesToDelete) {
            boolean foundSprite = false;
            for (String layerName : layers.values()) {
                for (int i = 0; i < sprites.get(layerName).size(); i++) {
                    if (sprites.get(layerName).get(i).equals(sprite)) {
                        sprites.get(layerName).remove(i);
                        foundSprite = true;
                        break;
                    }
                }
                if (foundSprite)
                    break;
            }
        }
    }

    public static void deleteAllSprites() {
        for (String layerName : layers.values())
            sprites.get(layerName).clear();
    }

    // removes all updatables except the one with a matching name
    public static void deleteExceptNames(String[] exceptions) {
        for (String layerName : layers.values()) {
            ArrayList<Sprite> spritesOnLayer = sprites.get(layerName);
            for (int i=0; i<spritesOnLayer.size(); i++) { 
                Sprite sprite = spritesOnLayer.get(i);
                for (String exception : exceptions)
                    if (!exception.equals(sprite.getName())) {
                        sprites.get(layerName).remove(i);
                        i--;
                }
            }
        }
    }
    // deletes updatable unless they have ONE of the provided tags
    public static void deleteExceptTags(String[] tags) {
        for (String layerName : layers.values()) {
            ArrayList<Sprite> spritesOnLayer = sprites.get(layerName);
            for (int i=0; i<spritesOnLayer.size(); i++) { 
                Sprite sprite = spritesOnLayer.get(i);
                for (String tag : tags)
                    if (!sprite.hasTag(tag)) {
                        sprites.get(layerName).remove(i);
                        i--;
                    }
                    else
                        System.out.println(sprite + "\nhas [" + tag + "] tag");
            }
        }
    }

    public static void drawSprites(Graphics2D g) {
        for (String layerName : layers.values()) 
            for (Sprite sprite : sprites.get(layerName))  {
                if (sprite.isVisible())
                    sprite.draw(g);
            }
    }

    // draws all sprites on given layers according to camera's reference frame (sprites xy coords used as world coords, not screen coords)
    public static void drawSprites(Graphics2D g, Camera camera, ArrayList<String> layersToInclude) {
        for (String layerName : layers.values()) {
            if (layersToInclude.contains(layerName))
                for (Sprite sprite : sprites.get(layerName))  {
                    if (sprite.isVisible())
                        sprite.draw(g, camera);
                }
            else
                for (Sprite sprite : sprites.get(layerName)) {
                    if (sprite.isVisible())
                        sprite.draw(g);
                }
        }
        
    }

    public static String getLayersToString() {
        return layers.toString();
    }
    public static String getSpritesToString() {
        return sprites.toString();
    }
}
