package aicar.simulation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;

import ai.djl.translate.TranslateException;
import aicar.dataRecording.DataRecordManager;
import aicar.model.ModelLoader;
import aicar.utils.misc.ScreenSize;
import aicar.utils.drawing.sprites.Camera;
import aicar.utils.drawing.sprites.Sprite;
import aicar.utils.drawing.sprites.TextInputSprite;
import aicar.utils.drawing.sprites.TextSprite;
import aicar.utils.drawing.sprites.UI;
import aicar.utils.input.Keyboard;
import aicar.utils.input.Mouse;
import aicar.utils.math.Vec;
import aicar.utils.tween.Timer;

public class Controller {
    private static final String WORLD_FOLDER_PATH = "/world/", DEFAULT_WORLD_MAP_FILEPATH = "mediummap.json",
        DEFAULT_MODEL_FOLDER_PATH = "/models", DEFAULT_MODEL_FILE_NAME = "modelcomplex";
    private static final String TOGGLE_CONTROL_KEY = "C", TOGGLE_DISTANCE_SENSOR_VISIBLE_KEY = "H";

    public enum ControlMode {
        MODEL,
        HUMAN
    }

    private ControlMode controlMode;
    private Keyboard keyboard;
    private Mouse mouse;
    private Camera camera;
    private World world;
    private Car car;
    private ModelLoader modelLoader;
    private DataRecordManager dataRecordManager;
    private TextSprite newSpawnSprite;
    private String modelFileName;
    private TextInputSprite modelTextInput;
    private String worldMapFileName;
    private TextInputSprite worldMapTextInput;

    public Controller(ControlMode controlMode, Keyboard keyboard, Mouse mouse, int screenWidth, int screenHeight) {
        this.controlMode = controlMode;
        this.keyboard = keyboard;
        this.mouse = mouse;
        camera = new Camera(screenWidth, screenHeight, 200, 200);
        worldMapFileName = DEFAULT_WORLD_MAP_FILEPATH;
        world = new World(WORLD_FOLDER_PATH + worldMapFileName, camera);
        car = new Car(world, world.getSpawnPosition(), world.getSpawnAngle());
        modelLoader = new ModelLoader();
        modelFileName = DEFAULT_MODEL_FILE_NAME;
        modelLoader.loadModelAsync(DEFAULT_MODEL_FOLDER_PATH, modelFileName);
        camera.setWorldBounds(world.getWorldWidth(), world.getWorldHeight());
        dataRecordManager = new DataRecordManager(keyboard, modelLoader.getTranslator());

        new Sprite("mouse", "ui") {
            @Override
            public void drawSelf(Graphics2D g, int x, int y, int w, int h, double a) {
                g.setColor(Color.BLACK);
                int radius = 2;
                g.fillRect(mouse.getX() - radius, mouse.getY() - radius, radius * 2, radius * 2);
            }
        };
        new Sprite("control mode", ScreenSize.getWidth() - 250, 0, 250, 110, "ui") {
            @Override
            public void drawSelf(Graphics2D g, int x, int y, int w, int h, double a) {
                g.setColor(UI.BG_COLOR);
                g.fillRect(x, y, w, h);
                g.fillRect(x, 120, w, 20);
                g.fillRect(x, 160, w, 20);

                g.setColor(UI.TEXT_COLOR);
                g.drawString("Control Mode: " + Controller.this.controlMode, x + 5, y + 20);
                g.drawString("Toggle Control Mode: " + TOGGLE_CONTROL_KEY, x + 5, y + 40);
                g.drawString(Controller.this.modelLoader.isModelLoaded() ? "Model Successfully Loaded " : "Loading " + Controller.this.modelFileName + "...", x + 5, y + 60);
                g.drawString("Current model: " + Controller.this.modelFileName, x + 5, y + 80);
                g.drawString("Current map: " + Controller.this.worldMapFileName, x + 5, y + 100);

                g.drawString("type model file name", x + 5, 135);
                g.drawString("type map file name", x+ 5, 175);
            }
        };

        modelTextInput = new TextInputSprite(keyboard, mouse, ScreenSize.getWidth() - 250, 140, 240, 20, "ui") {
            @Override
            public void onEnter() {
                modelFileName = getText().toLowerCase();
                modelLoader.loadModelAsync(DEFAULT_MODEL_FOLDER_PATH, modelFileName);
            }
        };
        worldMapTextInput = new TextInputSprite(keyboard, mouse, ScreenSize.getWidth() - 250, 180, 240, 20, "ui") {
            @Override
            public void onEnter() {
                worldMapFileName = getText().toLowerCase();
                world.loadMap(WORLD_FOLDER_PATH + worldMapFileName + ".json");
            }
        };

        newSpawnSprite = new TextSprite(ScreenSize.getWidth() / 2, 10, "Spawn set at (x, y)", "ui");
        newSpawnSprite.setVisible(false);
    }

    public ControlMode getControlMode() {
        return controlMode;
    }
    public void setControlMode(ControlMode mode) {
        controlMode = mode;
    }

    public Camera getCamera() {
        return camera;
    }

    public void update() {
        boolean uiSelected = modelTextInput.isSelected() || worldMapTextInput.isSelected();
        if (!uiSelected) {
            if (keyboard.keyClicked(TOGGLE_CONTROL_KEY)) {
                controlMode = controlMode == ControlMode.HUMAN ? ControlMode.MODEL : ControlMode.HUMAN;
                if (controlMode == ControlMode.MODEL && !modelLoader.isModelLoaded()) {
                    controlMode = ControlMode.HUMAN;
                    TextSprite modelLoadSprite = new TextSprite(ScreenSize.getWidth() / 2, 10, "MODEL IS NOT LOADED", "ui");
                    Timer.createSetTimer("hide model load sprite", modelLoadSprite, 3, "visible", false);
                }
                else if (controlMode == ControlMode.HUMAN)
                    car.stop();
            }

            if (mouse.clicked() && keyboard.keyDown("Ctrl")) {
                int x = (int) camera.screenXToWorld(mouse.getX());
                int y = (int) camera.screenYToWorld(mouse.getY());
                newSpawnSprite.setText("Spawn set at (" + x + ", " + y + ")");
                newSpawnSprite.setVisible(true);
                Timer.createSetTimer("hide new spawn sprite", newSpawnSprite, 1.5, "visible", false);
                world.setSpawnPos(new Vec(x, y), Math.atan2(car.getCenterY() - world.getWorldHeight() * 0.5, car.getCenterX() - world.getWorldWidth() * 0.5) - Math.PI * 0.5);
            }

            if (keyboard.keyClicked(TOGGLE_DISTANCE_SENSOR_VISIBLE_KEY))
                car.toggleDistanceSensorVisible();
        }
        
        
        double[] modelInputs = createModelInputs(car.getDistances(), car.getVelocity());
        switch (controlMode) {
            case HUMAN:
                updateHuman();
                break;
            case MODEL:
                updateModel(modelInputs);
                break;
        }
        
        dataRecordManager.updateRecording(controlMode, modelInputs, car.getAcceleration());
            
        car.updateCollisions();
        car.updateSprites();
        camera.follow(car.getCenterX(), car.getCenterY());
    }

    private void updateHuman() {
        boolean uiSelected = modelTextInput.isSelected() || worldMapTextInput.isSelected();
        if (uiSelected)
            return;
        double drivePower = 0, turnPower = 0;

        if (mouse.down()) {
            double angle = car.getAngle();
            Vec carToMouse = new Vec(camera.screenXToWorld(mouse.getX()) - car.getCenterX(), camera.screenYToWorld(mouse.getY()) - car.getCenterY());
            carToMouse = carToMouse.rotate(-angle);

            drivePower = carToMouse.x() / ScreenSize.getWidth() * 2;
            turnPower = carToMouse.y() / ScreenSize.getWidth() * 2;
        }

        car.receiveHumanInput(drivePower, turnPower);
    }

    private void updateModel(double[] modelInputs) {
        
        // calculate model outputs
        double[] acceleration;
        try {
            acceleration = modelLoader.getPredictor().predict(modelInputs);
        } catch (TranslateException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("the model inputs: " + Arrays.toString(modelInputs) + " are not formatted correctly");
        }
        car.receiveAccelerations(acceleration[0], acceleration[1]);
    }

    private double[] createModelInputs(double[] distances, double[] velocity) {
        // merge distances and car velocity into 1 double[]
        double[] inputs = new double[distances.length + velocity.length];
        for (int i=0; i<inputs.length; i++)
            inputs[i] = i < distances.length ? distances[i] : velocity[i - distances.length];
        return inputs;
    }
}
