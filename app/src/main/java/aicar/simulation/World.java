package aicar.simulation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import aicar.utils.drawing.sprites.Camera;
import aicar.utils.drawing.sprites.Sprite;
import aicar.utils.math.Vec;

public class World {

    private static final Color ROAD_COLOR = new Color(30, 30, 40), WALL_COLOR_LIGHT = new Color(80, 125, 55), WALL_COLOR_DARK = new Color(62, 107, 47);
    private static final String ROAD_STRING = "-", WALL_STRING = "0";
    public enum TileType {
        ROAD, WALL, OUT_OF_BOUNDS
    }

    private MapFile mapFile;

    public World(String mapFilePath, Camera camera) {
        mapFile = parseToMapFile(mapFilePath);

        int worldWidth = mapFile.cols * mapFile.tileSize;
        int worldHeight = mapFile.rows *  mapFile.tileSize;
        new Sprite("world", 0, 0, worldWidth, worldHeight, "world") {
            @Override
            public void drawSelf(Graphics2D g, int x, int y, int width, int height, double angle) {
                int startRow = (int) camera.getY() /  mapFile.tileSize;
                int startCol = (int) camera.getX() /  mapFile.tileSize;
                int cols = camera.getScreenWidth() /  mapFile.tileSize + 2;
                int rows = camera.getScreenHeight() /  mapFile.tileSize + 2;

                int startDrawx = -((int) camera.getX() %  mapFile.tileSize);
                int startDrawy = -((int) camera.getY() %  mapFile.tileSize);
                
                for (int i=0; i<rows; i++) {
                    if (i + startRow >= mapFile.rows)
                        break;
                    
                    for (int j=0; j<cols; j++) {
                        if (j + startCol >= mapFile.cols)
                            break;
                        
                        int drawx = startDrawx + j *  mapFile.tileSize;
                        int drawy = startDrawy + i *  mapFile.tileSize;
                        Color color = Color.WHITE;
                        int worldRow = startRow + i;
                        int worldCol = startCol + j;
                        switch (mapFile.map[worldRow][worldCol]) {
                            case TileType.ROAD: 
                                color = ROAD_COLOR; 
                                break;
                            case TileType.WALL: 
                                // add tiling pattern so you can always see the world moving
                                if ((worldRow / mapFile.patternTileSize + worldCol / mapFile.patternTileSize) % 2 == 0)
                                    color = WALL_COLOR_LIGHT; 
                                else
                                    color = WALL_COLOR_DARK;
                                break;
                            case TileType.OUT_OF_BOUNDS: 
                                color = Color.WHITE; 
                                break;
                        }
                        g.setColor(color);
                        g.fillRect(drawx, drawy, mapFile.tileSize, mapFile.tileSize);
                    }
                }
            }
        };
    }

    public int getWorldWidth() {
        return mapFile.cols *  mapFile.tileSize;
    }
    public int getWorldHeight() {
        return mapFile.rows *  mapFile.tileSize;
    }
    public int getTileSize() {
        return  mapFile.tileSize;
    }
    public Vec getSpawnPosition() {
        return mapFile.spawnPos;
    }
    public double getSpawnAngle() {
        return mapFile.spawnAngle;
    }
    
    public boolean inBounds(int row, int col) {
        return row >= 0 && col >= 0 && row < mapFile.map.length && col < mapFile.map[0].length;
    }
    public TileType getTileType(int row, int col) {
        if (!inBounds(row, col))
            return TileType.OUT_OF_BOUNDS;
        return mapFile.map[row][col];
    }

    public void setSpawnPos(Vec pos, double angle) {
        mapFile = new MapFile(mapFile.rows, mapFile.cols, mapFile.tileSize, mapFile.patternTileSize, mapFile.map, pos, angle);
    }

    // tileSize = tile size of tiles in world
    private record MapFile(int rows, int cols, int tileSize, int patternTileSize, TileType[][] map, Vec spawnPos, double spawnAngle){}

    private MapFile parseToMapFile(String mapFilePath) {
        try {
            InputStream is = getClass().getResourceAsStream(mapFilePath);
            JSONObject data = new JSONObject(new String(is.readAllBytes(), StandardCharsets.UTF_8));

            String type = data.getString("type");

            int tileSize = data.getInt("tileSize");
            int patternTileSize = data.getInt("patternTileSize");
            Vec spawnPos = new Vec(data.getDouble("spawnCol") * tileSize, data.getDouble("spawnRow") * tileSize);
            double spawnAngle = Math.toRadians(data.getDouble("spawnAngle"));

            TileType[][] mapTiles = null;
            if (type.equals("JSON")) 
                mapTiles = getJsonMapTiles(data.getJSONArray("map"));
            else if (type.equals("PNG"))
                mapTiles = parsePngToMapFile(data, data.getString("map"));
            else
                throw new JSONException("type must be either JSON or PNG, cannot be " + type);
            
            return new MapFile(mapTiles.length, mapTiles[0].length, tileSize, patternTileSize, mapTiles, spawnPos, spawnAngle);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private TileType[][] getJsonMapTiles(JSONArray map) {

        TileType[][] mapTiles = new TileType[map.length()][map.getJSONArray(0).length()];
        for (int i=0; i<map.length(); i++) {
            String row = map.getString(i).replace(" ", "");
            
            for (int j=0; j<row.length(); j++)
                switch (row.substring(j, j + 1)) {
                    case ROAD_STRING: mapTiles[i][j] = TileType.ROAD; break;
                    case WALL_STRING: mapTiles[i][j] = TileType.WALL; break;
                }
        }
        return mapTiles;
    }

    private TileType[][] parsePngToMapFile(JSONObject data, String mapFilePath) {
        try {
            BufferedImage image = ImageIO.read(getClass().getResourceAsStream(mapFilePath));
            TileType[][] mapTiles = new TileType[image.getHeight()][image.getWidth()];

            int roadThreshold = data.getInt("roadThreshold");
            for (int y=0; y<image.getHeight(); y++) {
                for (int x=0; x<image.getWidth(); x++) {
                    Color color = new Color(image.getRGB(x, y), true);
                    int total = color.getRed() + color.getBlue() + color.getGreen();
                    if (total > roadThreshold)
                        mapTiles[y][x] = TileType.ROAD;
                    else
                        mapTiles[y][x] = TileType.WALL;
                }
            }

            return mapTiles;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
