package aicar.dataRecording;

import java.text.DecimalFormat;

import aicar.simulation.Controller;
import aicar.utils.ScreenSize;
import aicar.utils.drawing.sprites.Sprite;
import aicar.utils.drawing.sprites.UI;
import aicar.utils.input.Keyboard;
import aicar.utils.tween.Timer;

import java.awt.Graphics2D;

public class DataRecordManager {
    private static final DecimalFormat DATE_FORMAT = new DecimalFormat("00");
    private static final String DATA_FOLDER_FILEPATH = "app/output/humanResponse/mediumMap", HUMAN_DATA_FILEPATH = "thirdAttemptFixed.csv", MODEL_DATA_FILEPATH = "modelResponse.csv";
    private static final int FRAME_RATE = 20; // how many times to record data per second
    private static final int RECORD_DELAY = 1000 / FRAME_RATE; // # of ms between each recording

    private static final String TOGGLE_RECORDING_KEY = "R", SAVE_RECORDING_KEY = "S";

    enum RecordMode {
        RECORDING,
        SAVE,
        NOT_RECORDING
    }
    private Keyboard keyInput;
    private RecordMode recordMode;
    private DataRecorder dataRecorder;
    private String dataFilePath;
    private long lastRecordedTime; // time of last recorded data (ms)
    private Sprite saveSprite;
    private long startTimeMillis;

    public DataRecordManager(Keyboard keyInput, Controller.ControlMode controlMode) {
        this.keyInput = keyInput;
        dataRecorder = new DataRecorder(10, new DecimalFormat("#.#####"));
        recordMode = RecordMode.NOT_RECORDING;
        dataFilePath = updateDataFilePath(controlMode);
        startTimeMillis = System.currentTimeMillis();

        new Sprite("data record UI", ScreenSize.getWidth() - 250, 80, 300, 85, "ui") {
            @Override
            public void drawSelf(Graphics2D g, int x, int y, int w, int h, double a) {
                g.setColor(UI.BG_COLOR);
                g.fillRect(x, y, w, h);
                g.setColor(UI.TEXT_COLOR);
                g.drawString("Record Mode: " + recordMode, x + 5, y + 15);
                g.drawString("toggle recording: " + TOGGLE_RECORDING_KEY, x + 5, y + 35);
                g.drawString("save recording: " + SAVE_RECORDING_KEY, x + 5, y + 55);
                g.drawString("current timestamp: " + convertMsToTimestamp(System.currentTimeMillis() - startTimeMillis), x + 5, y + 75);

            }
        };
        saveSprite = new Sprite("save record", ScreenSize.getWidth() - 300, 200, 300, 20, "ui") {
            @Override
            public void drawSelf(Graphics2D g, int x, int y, int w, int h, double a) {
                g.setColor(UI.BG_COLOR);
                g.fillRect(x, y, w, h);
                g.setColor(UI.TEXT_COLOR);
                g.drawString("Saved to " + dataFilePath, x + 5, getBottom() - 5);
            }
        };
        saveSprite.setVisible(false);

        lastRecordedTime = 0;
    }

    public void stopRecording() {
        recordMode = RecordMode.NOT_RECORDING;
    }
    public void clearRecording() {
        dataRecorder.clearDataPoints();
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
        
        // take actions based off current recording state
        switch (recordMode) {
            case NOT_RECORDING: 
                lastRecordedTime = 0;
                break;
            case RECORDING: 
                if (System.currentTimeMillis() - lastRecordedTime > RECORD_DELAY) {
                    String timestamp = convertMsToTimestamp(System.currentTimeMillis() - startTimeMillis);
                    dataRecorder.addDataPoint(timestamp, inputs, outputs); 
                    lastRecordedTime = System.currentTimeMillis();
                }
                break;
            case SAVE: 
                dataFilePath = updateDataFilePath(controlMode);
                dataRecorder.saveToFile(DATA_FOLDER_FILEPATH, dataFilePath);
                recordMode = RecordMode.NOT_RECORDING; // only want to save once
                saveSprite.setVisible(true);
                Timer.createSetTimer("hide save sprite", saveSprite, 1.5, "visible", false);
                break;
        }
    }
    private String updateDataFilePath(Controller.ControlMode controlMode) {
        String filePath = "";
        switch (controlMode) {
            case HUMAN: filePath = HUMAN_DATA_FILEPATH; break;
            case MODEL: filePath = MODEL_DATA_FILEPATH; break;
        }
        return filePath;
    }

    public String convertMsToTimestamp(long millis) {
        int seconds = (int) (millis / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        return DATE_FORMAT.format(hours) + ":" + DATE_FORMAT.format(minutes % 60) + ":" + DATE_FORMAT.format(seconds % 60);
    }
}
