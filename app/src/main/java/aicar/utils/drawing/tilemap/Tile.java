package aicar.utils.drawing.tilemap;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Tile {

    public enum Type {
        FILLED, EMPTY, ANYTHING
    }
    private int tileSize;
    private int tilemapDrawX, tilemapDrawy;
    private Type[][] rules; // cols must be same as rows

    public Tile(int tileSize, int tilemapDrawX, int tilemapDrawY, String rules, int ruleWidth, int ruleHeight) {
        this.tileSize = tileSize;
        this.tilemapDrawX = tilemapDrawX;
        this.tilemapDrawy = tilemapDrawY;

        this.rules = loadRules(rules, ruleWidth, ruleHeight);
    }

    private Type[][] loadRules(String rulesString, int ruleWidth, int ruleHeight) {
        if (rulesString.length() != (ruleWidth + 1) * ruleHeight - 1 && rulesString.length() != (ruleWidth + 1) * ruleHeight)
            throw new IllegalArgumentException(rulesString + " is not correct length");
        Type[][] rules = new Type[ruleHeight][ruleWidth];

        rulesString = rulesString.replaceAll(" ", "");
        for (int i = 0; i <  rulesString.length(); i++) {
            Type type = null;
            switch (rulesString.charAt(i)) {
                case 'y': type = Type.FILLED; break;
                case 'n': type = Type.EMPTY; break;
                case 'a': type = Type.ANYTHING; break;
            }
            if (type == null) 
                throw new IllegalArgumentException("rule string " + rulesString + " is invalid at row " + (i / ruleHeight) + " and column " + (i % ruleWidth));
            
            rules[i / ruleHeight][i % ruleWidth] = type;
        }
        return rules;
    }

    public boolean rulesMatch(boolean[][] rules) {
        for (int y=0; y<rules.length; y++)
            for (int x=0; x<rules[0].length; x++) {
                if (x == rules.length / 2 && y == rules.length / 2)
                    continue;
                Type requiredType = this.rules[y][x];
                if (requiredType == Type.ANYTHING)
                    continue;
                if (requiredType == Type.FILLED && !rules[y][x] || requiredType == Type.EMPTY && rules[y][x])
                    return false;
            }
        return true;
    }

    public void setRule(Type ruleType, int y, int x) {
        if (x == 1 && y == 1)
            return; // middle is self - should always be anything b/c it is meaningless
        rules[y][x] = ruleType;
    }

    public void drawTile(Graphics2D g, BufferedImage image, int x, int y, int w, int h) {
        g.drawImage(image, x, y, x + w, y + h, tilemapDrawX, tilemapDrawy, tilemapDrawX + tileSize, tilemapDrawy + tileSize, null);
    }
    public void drawRules(Graphics2D g, Color filledColor, Color anythingColor, int x, int y, int w, int h) {
        int ruleSize = w / rules.length;
        for (int i = 0; i < rules.length; i++)
            for (int j = 0; j < rules[0].length; j++) {
                if (rules[i][j] == Type.EMPTY)
                    continue;
                g.setColor(rules[i][j] == Type.ANYTHING ? anythingColor : filledColor);
                int drawX = x + j * ruleSize;
                int drawY = y + i * ruleSize;
                g.fillRect(drawX, drawY, ruleSize, ruleSize);
            }
    }

    @Override
    public String toString() {
        String str = "";
        for (Type[] row : rules) {
            for (Type type : row)
                str += type.toString().charAt(0) + " ";
            str += "\n";
        }
        return str;
    }

    public String getRulesString() {
        System.out.println(this);
        String str = "";
        for (Type[] row : rules) {
            for (Type rule : row)
                switch (rule) {
                    case ANYTHING: str += "a"; break;
                    case FILLED: str += "y"; break;
                    case EMPTY: str += "n"; break;
                }
            str += " ";
        }
        return str;
    }
}
