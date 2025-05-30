package aicar.simulation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;

import ai.djl.translate.TranslateException;
import aicar.dataRecording.DataRecordManager;
import aicar.model.ModelLoader;
import aicar.utils.ScreenSize;
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
    private static final String WORLD_MAP_FILEPATH = "/world/mediumMapMedRes.json",
        DEFAULT_MODEL_FOLDER_PATH = "/models", DEFAULT_MODEL_FILE_NAME = "modelcomplex";
    private static final String TOGGLE_CONTROL_KEY = "C";

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

    public Controller(ControlMode controlMode, Keyboard keyboard, Mouse mouse, int screenWidth, int screenHeight) {
        this.controlMode = controlMode;
        this.keyboard = keyboard;
        this.mouse = mouse;
        camera = new Camera(screenWidth, screenHeight, 200, 200);
        world = new World(WORLD_MAP_FILEPATH, camera);
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
        new Sprite("control mode", ScreenSize.getWidth() - 250, 0, 250, 70, "ui") {
            @Override
            public void drawSelf(Graphics2D g, int x, int y, int w, int h, double a) {
                g.setColor(UI.BG_COLOR);
                g.fillRect(x, y, w, h);
                g.fillRect(x, y + h + 10, w, 20);

                g.setColor(UI.TEXT_COLOR);
                g.drawString("Control Mode: " + Controller.this.controlMode, x + 5, y + 20);
                g.drawString("Toggle Control Mode: " + TOGGLE_CONTROL_KEY, x + 5, y + 40);
                g.drawString(Controller.this.modelLoader.isModelLoaded() ? "Model Successfully Loaded " : "Loading " + Controller.this.modelFileName + "...", x + 5, y + 60);
                g.drawString("type file name", x + 5, y + h + 25);
            }
        };

        newSpawnSprite = new TextSprite(ScreenSize.getWidth() / 2, 10, "Spawn set at (x, y)", "ui");
        newSpawnSprite.setVisible(false);

        modelTextInput = new TextInputSprite(keyboard, mouse, ScreenSize.getWidth() - 250, 110, 240, 20, "ui") {
            @Override
            public void onEnter() {
                modelFileName = getText().toLowerCase();
                System.out.println("ON ENTER, loading " + modelFileName);
                modelLoader.loadModelAsync(DEFAULT_MODEL_FOLDER_PATH, modelFileName);
            }
        };
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
        if (!modelTextInput.isSelected()) {
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
        if (modelTextInput.isSelected())
            return;
        double drivePower = 0, turnPower = 0;

        if (mouse.down()) {
            double angle = car.getAngle();
            Vec carToMouse = new Vec(camera.screenXToWorld(mouse.getX()) - car.getCenterX(), camera.screenYToWorld(mouse.getY()) - car.getCenterY());
            carToMouse = carToMouse.rotate(-angle);

            drivePower = carToMouse.x() / ScreenSize.getWidth() * 2;
            turnPower = carToMouse.y() / ScreenSize.getWidth() * 2;
        }

        car.recieveHumanInput(drivePower, turnPower);
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
        car.recieveAccelerations(acceleration[0], acceleration[1]);
    }

    private double[] createModelInputs(double[] distances, double[] velocity) {
        // merge distances and car velocity into 1 double[]
        double[] inputs = new double[distances.length + velocity.length];
        for (int i=0; i<inputs.length; i++)
            inputs[i] = i < distances.length ? distances[i] : velocity[i - distances.length];
        return inputs;
    }
}
