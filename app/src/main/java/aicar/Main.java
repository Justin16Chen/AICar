package aicar;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;

import aicar.simulation.Controller;
import aicar.simulation.Drawer;
import aicar.utils.misc.ScreenSize;
import aicar.utils.drawing.sprites.Sprite;
import aicar.utils.drawing.sprites.UI;
import aicar.utils.input.Keyboard;
import aicar.utils.input.Mouse;
import aicar.utils.tween.Updatables;

public class Main extends JFrame {

    private static final int WIDTH = 900, HEIGHT = 900;

    private Controller controller;
    private Keyboard keyboard;
    private Mouse mouse;
    private Drawer drawer;
    private double dt;

    public void openWindow() {
        // update screen size
        setPreferredSize(new Dimension(900, 900));
        ScreenSize.setScreenSize(WIDTH, HEIGHT);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                ScreenSize.setScreenSize(WIDTH, HEIGHT);
            }
        });
        // setup drawing
        drawer = new Drawer();
        drawer.setupLayers();

        // add inputs
        keyboard = new Keyboard();
        mouse = new Mouse(new Insets(25, 0, 0, 0));
        addKeyListener(keyboard);
        addMouseMotionListener(mouse);
        addMouseListener(mouse);

        // setup controller
        controller = new Controller(Controller.ControlMode.HUMAN, keyboard, mouse, WIDTH, HEIGHT);
        drawer.setCamera(controller.getCamera());

        // load different models at runtime based on text input
        // JTextField modelTextField = new JTextField(20);
        // modelTextField.setAlignmentX(0.8f);
        // modelTextField.setAlignmentY(0.2f);
        // modelTextField.setBackground(UI.BG_COLOR);
        // modelTextField.setText("MODEL TEXT FIELD");
        // modelTextField.addActionListener(new ActionListener() {
        //     @Override
        //     public void actionPerformed(ActionEvent e) {
        //         String[] text = modelTextField.getText().replace(" ", "").split(",");
        //         controller.loadModel(text[0], text[1]);
        //         modelTextField.setText("");
        //         Sprite sprite = new TextSprite(WIDTH / 2, 5, "Loading " + text[0] + "/" + text[1], "ui");
        //         Timer.createCallTimer("delete load model sprite", sprite, 1.5, "delete");
        //     }
        // });
        // add(modelTextField);

        // setup window layout
        setTitle("Driving Simulation");
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        add(drawer);
        pack();
    }

    public void startThread() {
        new Thread() {
            @Override
            public void run() {
                int waitTime = 1000 / 60;
                long lastTime = System.currentTimeMillis();
                long currentTime = System.currentTimeMillis();
                dt = 0;
                new Sprite("delta time ui", "ui") {
                    @Override
                    public void drawSelf(Graphics2D g, int x, int y, int w, int h, double a) {
                        int top = ScreenSize.getHeight() - 100;
                        g.setColor(UI.BG_COLOR);
                        g.fillRect(0, top, 100, 30);
                        g.setColor(UI.TEXT_COLOR);
                        g.drawString("dt: " + UI.DF.format(dt), 5, top + 20);
                    }
                };
                while (true) {

                    lastTime = currentTime;
                    currentTime = System.currentTimeMillis();
                    dt = (currentTime - lastTime) / 1000.;

                    Updatables.updateUpdatables(dt);
                    mouse.update();
                    keyboard.update();

                    controller.update();
                    drawer.repaint();

                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }.run();
    }

    public static void main(String[] args) throws Exception {
       Main main = new Main();
       main.openWindow();
       main.startThread();
    }
}
