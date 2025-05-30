package aicar.utils.drawing.sprites;

import java.awt.Graphics2D;

import aicar.utils.input.Keyboard;
import aicar.utils.input.Mouse;

public abstract class TextInputSprite extends TextSprite {

    private Keyboard keyboard;
    private Mouse mouse;
    private int cursorIndex;
    private boolean selected;
    
    public TextInputSprite(Keyboard keyboard, Mouse mouse, int x, int y, int w, int h, String layer) {
        super(x, y, "", layer);
        setWidth(w);
        setHeight(h);
        this.keyboard = keyboard;
        this.mouse = mouse;
        cursorIndex = 0;
        selected = false;
    }

    public abstract void onEnter();

    private String getTextToAdd() {
        String textToAdd = "";
        for (String key : keyboard.getAlphaNumericKeysClicked())
            textToAdd += key.toLowerCase();
        return textToAdd;
    }

    private void updateSelected() {
        if (mouse.clicked()) 
            selected = mouse.isOver(this);
    }

    public boolean isSelected() {
        return selected;
    }

    private void updateText() {
         if (keyboard.keyClicked("backspace")) {
            setText(getText().substring(0, getText().length() - 1));
            cursorIndex--;
        }

        cursorIndex += keyboard.keyClickedInt("right") - keyboard.keyClickedInt("left");
        cursorIndex = Math.max(0, Math.min(getText().length(), cursorIndex));

        String textToAdd = getTextToAdd();
        if (!textToAdd.isEmpty()) {
            setText(getText().substring(0, cursorIndex) + textToAdd + getText().substring(cursorIndex));
            cursorIndex += textToAdd.length();
        }
        
        if (keyboard.keyClicked("enter"))
            onEnter();
    }

    @Override
    public void drawSelf(Graphics2D g, int x, int y, int w, int h, double a) {
        setUpdateWidth(false);
        updateSelected();
        if (selected)
            updateText();

        super.drawSelf(g, x, y, w, h, a);

        // only draw cursor if selected
        if (selected) {
            g.setColor(textColor);
            int textWToCursor = g.getFontMetrics().stringWidth(getText().substring(0, cursorIndex));
            int cursorX = x + padding.left + textWToCursor;
            g.drawLine(cursorX, y + padding.top, cursorX, y + h - padding.bottom);
        }
    }
}
