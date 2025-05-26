package aicar.utils.drawing.sprites;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;

public class TextSprite extends Sprite {
    
    private String text;
    private Color bgColor, textColor;
    private Insets padding;
    private boolean first = true;

    public TextSprite(int x, int y, String text, String layer) {
        super(text, x, y, 1, 1, layer);
        this.text = text;
        bgColor = UI.BG_COLOR;
        textColor = UI.TEXT_COLOR;
        padding = new Insets(5, 5, 5, 5);
    }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public void setColors(Color bgColor, Color textColor) {
        this.bgColor = bgColor;
        this.textColor = textColor;
    }

    @Override
    public void drawSelf(Graphics2D g, int x, int y, int w, int h, double a) {
        if (first) {
            first = false;
            setWidth(g.getFontMetrics().stringWidth(text) + padding.left + padding.right);
            setHeight(g.getFontMetrics().getHeight() + padding.top + padding.bottom);
            setX(getX() - getWidth() / 2);
        }
        else {
            g.setColor(bgColor);
            g.fillRect(x, y, w, h);
            g.setColor(textColor);
            g.drawString(text, x + padding.left, y + h - padding.bottom);
        }
    }
}
