package aicar.dataRecording;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;

import aicar.model.InputTranslator;
import aicar.simulation.Controller;
import aicar.utils.ScreenSize;
import aicar.utils.drawing.sprites.Sprite;
import aicar.utils.drawing.sprites.TextSprite;
import aicar.utils.drawing.sprites.UI;
import aicar.utils.input.Keyboard;
import aicar.utils.tween.Timer;

import java.awt.Graphics2D;

public class DataRecordManager {
    private static final String HUMAN_FILEPATH = "app/output/humanResponse/mediumMap/rightTurn.csv", 
        MODEL_FILEPATH = "app/output/modelResponse/mediumMap/rightTurn.csv";

    private static final int FRAME_RATE = 20; // how many times to record data per second
    private static final int RECORD_DELAY = 1000 / FRAME_RATE; // # of ms between each recording

    private static final String TOGGLE_RECORDING_KEY = "R", SAVE_RECORDING_KEY = "S", DELETE_RECORDING_KEY = "Backspace";

    enum RecordMode {
        RECORDING,
        SAVE,
        NOT_RECORDING
    }
    private Keyboard keyInput;
    private InputTranslator translator;
    private RecordMode recordMode;
    private DataRecorder dataRecorder;
    private long lastRecordedTime; // time of last recorded data (ms)
    private TextSprite saveSprite;

    public DataRecordManager(Keyboard keyInput, InputTranslator translator) {
        this.keyInput = keyInput;
        this.translator = translator;
        dataRecorder = new DataRecorder(10, new DecimalFormat("#.#####"));
        recordMode = RecordMode.NOT_RECORDING;

        new Sprite("data record UI", ScreenSize.getWidth() - 250, 140, 300, 105, "ui") {
            @Override
            public void drawSelf(Graphics2D g, int x, int y, int w, int h, double a) {
                g.setColor(UI.BG_COLOR);
                g.fillRect(x, y, w, h);
                g.setColor(UI.TEXT_COLOR);
                g.drawString("Record Mode: " + recordMode, x + 5, y + 15);
                g.drawString("toggle recording: " + TOGGLE_RECORDING_KEY, x + 5, y + 35);
                g.drawString("save recording: " + SAVE_RECORDING_KEY, x + 5, y + 55);
                g.drawString("current timestamp: " + getTimestamp(), x + 5, y + 75);
            }
        };
        saveSprite = new TextSprite(ScreenSize.getWidth() - 300, 200, "Saved # lines to filepath", "ui");
        saveSprite.setVisible(false);

        lastRecordedTime = 0;
    }

    public void updateRecording(Controller.ControlMode controlMode, double[] inputs, double[] outputs) {

        // update recording state
        if (keyInput.keyClicked(TOGGLE_RECORDING_KEY)) {
            if (recordMode == RecordMode.NOT_RECORDING)
                recordMode = RecordMode.RECORDING;
            else
                recordMode = RecordMode.NOT_RECORDING;
        }
        
        if (keyInput.keyClicked(SAVE_RECORDING_KEY))
            recordMode = RecordMode.SAVE;
        
        if (keyInput.keyClicked(DELETE_RECORDING_KEY)) {
            dataRecorder.clearDataPoints();
            recordMode = RecordMode.NOT_RECORDING;
        }
        
        // take actions based off current recording state
        switch (recordMode) {
            case NOT_RECORDING: 
                lastRecordedTime = 0;
                break;
            case RECORDING: 
                if (System.currentTimeMillis() - lastRecordedTime > RECORD_DELAY) {
                    String timestamp = getTimestamp();
                    dataRecorder.addDataPoint(timestamp, inputs, outputs); 
                    lastRecordedTime = System.currentTimeMillis();
                }
                break;
            case SAVE: 
                String path = getDataFilePath(controlMode);

                saveSprite.setText("Saved " + dataRecorder.getNumEntries() + " entries to " + path);
                saveSprite.setVisible(true);
                Timer.createSetTimer("hide save sprite", saveSprite, 3, "visible", false);

                dataRecorder.saveToFile(path);
                translator.saveData();
                recordMode = RecordMode.NOT_RECORDING;
                break;
        }
    }
    private String getDataFilePath(Controller.ControlMode controlMode) {
        switch (controlMode) {
            case HUMAN: return HUMAN_FILEPATH;
            case MODEL: return MODEL_FILEPATH;
            default: return "";
        }
    }

    public String getTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }
}
