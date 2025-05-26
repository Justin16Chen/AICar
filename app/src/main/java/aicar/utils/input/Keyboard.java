package aicar.utils.input;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Keyboard extends KeyAdapter {

    public static final Keyboard NOTHING_INPUT = new Keyboard(Type.NOTHING);

    private enum Type {
        NOTHING,
        ACTIVE
    }

    private ArrayList<String> keyList = new ArrayList<String>(Arrays.asList(
        "Back Quote","1","2","3","4","5","6","7","8","9","0","Minus","Equals","Backspace",
        "Q","W","E","R","T","Y","U","I","O","P","Open Bracket","Close Bracket","Back Slash",
        "Caps Lock","A","S","D","F","G","H","J","K","L","Semicolon","Quote","Enter",
        "Shift","Z","X","C","V","B","N","M","Comma","Period","Slash",
        "Ctrl","Windows","Alt","Space","Page Up","Page Down","Up","Left","Down","Right"
    ));

    private HashMap<String, Key> keyMap = new HashMap<String, Key>();
    private Type type;
    
    public Keyboard() {
        setupKeyMap();
        type = Type.ACTIVE;
    }
    public Keyboard(Type type) {
        setupKeyMap();
        this.type = type;
    }

    // setup key map from key list
    private void setupKeyMap() {
        for (String key : keyList) {
            keyMap.put(parseKeyName(key), new Key(key));
        }
    }
    
    // update all keys
    public void update() {
        if (type == Type.NOTHING)
            return;

        for (String keyName : keyList) {
            keyMap.get(parseKeyName(keyName)).update();
        }
    }

    private String parseKeyName(String keyName) {
        return keyName.toLowerCase();
    }

    // key input getters
    public HashMap<String, Key> getKeyMap() {
        return keyMap;
    }
    public boolean keyDown(String keyName) {
        return keyMap.get(parseKeyName(keyName)).down();
    }
    public int keyDownInt(String keyName) {
        return keyDown(parseKeyName(keyName)) ? 1 : 0;
    }
    public boolean keyClicked(String keyName) {
        return keyMap.get(parseKeyName(keyName)).clicked();
    }
    public int keyClickedInt(String keyName) {
        return keyClicked(parseKeyName(keyName)) ? 1 : 0;
    }
    public boolean keyReleased(String keyName) {
        return keyMap.get(parseKeyName(keyName)).released();
    }
    public int keyReleasedInt(String keyName) {
        return keyReleased(parseKeyName(keyName)) ? 1 : 0;
    }
    public ArrayList<String> getAllKeys(InputBase.State keyState) {
        ArrayList<String> keyNames = new ArrayList<>();

        for (String keyName : keyList) {
            Key key = keyMap.get(keyName);

            if (key.getState() == keyState) keyNames.add(keyName);
        }
        return keyNames;
    }

    public String parseKeyEvent(KeyEvent e) {
        // convert ascii value to character
        return parseKeyName(KeyEvent.getKeyText(e.getKeyCode()));
    }

    // recieve key input
    @Override
    public void keyPressed(KeyEvent e) {
        if (type == Type.NOTHING)
            return;
        String keyName = parseKeyEvent(e);
        if (keyMap.getOrDefault(keyName, null) == null)
            return;
        keyMap.get(keyName).setDown(true);
    }
    @Override
    public void keyReleased(KeyEvent e) {
        if (type == Type.NOTHING)
            return;
        String keyName = parseKeyEvent(e);
        if (keyMap.getOrDefault(keyName, null) == null)
            return;
        keyMap.get(keyName).setDown(false);
    }
}
