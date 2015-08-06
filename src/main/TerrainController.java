/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.net.URL;
import java.util.HashMap;
import java.util.Random;
import java.util.ResourceBundle;
import javafx.animation.AnimationTimer;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import main.TransformLayer.RotateOrder;

/**
 * FXML Controller class
 *
 * @author Administrator
 */
public class TerrainController extends AnimationTimer implements Initializable {
    private static final double CAMERA_TRANSLATE_SPEED = 1;
    private static final double CAMERA_ROTATE_SPEED = 0.01;

    @FXML
    private Pane mainPane;
    @FXML
    private Label fpsLabel;
    
    private HashMap<KeyCode, Boolean> inputMap = new HashMap<>();
    private SubScene terrainView;
    private TransformLayer root = new TransformLayer();
    private PerspectiveCamera camera;
    private double mousePosX;
    private double mousePosY;
    private DoubleProperty pitch;
    private DoubleProperty yaw;
    private DoubleProperty cameraX;
    private DoubleProperty cameraY;
    private DoubleProperty cameraZ;
    
    private QuadSquare test;
    private Group terrain = new Group();
    
    private final long[] frameTimes = new long[100];
    private int frameTimeIndex = 0 ;
    private boolean arrayFilled = false ;
    public static boolean update = true;
    
    TriangleMesh mesh;
    Random r = new Random();
    
    Service service;


    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        terrainView = new SubScene(root, 640, 480, true, SceneAntialiasing.BALANCED);
        mainPane.getChildren().add(terrainView);
        inputMap.put(KeyCode.W, false);
        inputMap.put(KeyCode.A, false);
        inputMap.put(KeyCode.S, false);
        inputMap.put(KeyCode.D, false);
        inputMap.put(KeyCode.R, false);
        inputMap.put(KeyCode.SHIFT, false);
        inputMap.put(KeyCode.SPACE, false);
        initInputMap(terrainView);
        initView();
        buildTest();
        start();
    }
    
    public void registerKey(KeyCode key) {
        inputMap.put(key, false);
    }
    
    public void initInputMap(Node target) {
        target.setFocusTraversable(true);
        target.requestFocus();
        target.setOnKeyPressed((KeyEvent event) -> {
            KeyCode keyCode = event.getCode();
            if (inputMap.containsKey(keyCode)) {
                inputMap.put(keyCode, true);
            }
        });
        target.setOnKeyReleased((KeyEvent event) -> {
            KeyCode keyCode = event.getCode();
            if (inputMap.containsKey(keyCode)) {
                inputMap.put(keyCode, false);
            }
        });
        target.setOnMousePressed((final MouseEvent event) -> {
            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();
        });
        target.setOnMouseDragged((final MouseEvent event) -> {
            double oldMousePosX = mousePosX;
            double oldMousePosY = mousePosY;
            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();
            yaw.set(yaw.get() - CAMERA_ROTATE_SPEED * (mousePosX - oldMousePosX));
            pitch.set(pitch.get() - CAMERA_ROTATE_SPEED * (mousePosY - oldMousePosY));
        });
    }
    
    public void initView() {
        terrainView.setFill(Color.BLACK);
        camera = new PerspectiveCamera(true);
        camera.setFieldOfView(45);
        camera.setNearClip(1);
        camera.setFarClip(10000);
        terrainView.setCamera(camera);
        
        pitch = new SimpleDoubleProperty(Math.toRadians(-90));
        yaw = new SimpleDoubleProperty(0);
        cameraX = new SimpleDoubleProperty(0);
        cameraY = new SimpleDoubleProperty(0);
        cameraZ = new SimpleDoubleProperty(300);
        
        TransformLayer cameraTransform = new TransformLayer();
        cameraTransform.rxProperty().bind(new DoubleBinding() {
            {super.bind(pitch);}

            @Override
            protected double computeValue() {
                return 270 + Math.toDegrees(pitch.get());
            }
            
        });
        cameraTransform.rzProperty().bind(new DoubleBinding() {
            {super.bind(yaw);}

            @Override
            protected double computeValue() {
                return Math.toDegrees(yaw.get()) - 90;
            }
            
        });
        cameraTransform.txProperty().bind(cameraX);
        cameraTransform.tyProperty().bind(cameraY);
        cameraTransform.tzProperty().bind(cameraZ);
        
        root.getChildren().add(cameraTransform);
        cameraTransform.getChildren().add(camera);
    }
    
    public void buildTest() {
        Random r = new Random();
        long seed = r.nextLong();
        System.out.println("Seed: " + seed);
        Noise2D noise = new Noise2D(-4402001516981054855L);
        Noise2D noise2 = new Noise2D(seed);
        noise.addNoiseLayer(30, 0.005, "x");
        noise.addNoiseLayer(2, 0.03, "x");
        //noise.addNoiseLayer(5, 0.023, "x");
        //noise.addNoiseLayer(2, 0.3, "x");
        //noise.addNoiseLayer(1, 0.4, "x");
        noise.addNoiseLayer(1, 0.05, "x");
        //noise.addNoiseLayer(1, 0.1, "x");
        test = new QuadSquare(512, noise);
        terrain.getChildren().add(test.getMeshGroup());
        mesh = new TriangleMesh();
        mesh.getTexCoords().addAll(0f, 0f);
        for (int i = -50; i < 50; i++) {
            for (int j = -50; j < 50; j++) {
                mesh.getPoints().addAll(i, j, (float)noise.getValue(i, j));
            }
        }
        
        for (int i = 0; i < 99; i++) {
            for (int j = 0; j < 99; j++) {
                mesh.getFaces().addAll(j*100+i, 0, (j+1)*100+i, 0, (j+1)*100+(i+1), 0);
                mesh.getFaces().addAll(j*100+i, 0, (j+1)*100+(i+1), 0, j*100+(i+1), 0);
            }
        }
        
        root.getChildren().add(terrain);
        MeshView brute = new MeshView(mesh);
        brute.setMaterial(new PhongMaterial(Color.RED));
        //brute.setDrawMode(DrawMode.LINE);
        //root.getChildren().add(brute);
//        for (int i = 0; i < 50; i += 2) {
//            for (int j = 0; j < 20; j += 2) {
//                Sphere sphere = new Sphere(0.5, 10);
//                sphere.setTranslateX(i);
//                sphere.setTranslateY(j);
//                sphere.setTranslateZ(-10);
//                root.getChildren().add(sphere);
//            }
//        }
        service = new Service() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Object call() throws Exception {
                        test.update((float)cameraX.get(), (float)cameraY.get(), (float)cameraZ.get());
                        return null;
                    }
                };
            }
        };
    }

    @Override
    public void handle(long now) {
        if (inputMap.get(KeyCode.W)) {
            cameraX.set(cameraX.get() + CAMERA_TRANSLATE_SPEED * (Math.cos(pitch.get()) * Math.cos(yaw.get())));
            cameraY.set(cameraY.get() + CAMERA_TRANSLATE_SPEED * (Math.cos(pitch.get()) * Math.sin(yaw.get())));
            cameraZ.set(cameraZ.get() + CAMERA_TRANSLATE_SPEED * (Math.sin(pitch.get())));
        }
        if (inputMap.get(KeyCode.S)) {
            cameraX.set(cameraX.get() - CAMERA_TRANSLATE_SPEED * (Math.cos(pitch.get()) * Math.cos(yaw.get())));
            cameraY.set(cameraY.get() - CAMERA_TRANSLATE_SPEED * (Math.cos(pitch.get()) * Math.sin(yaw.get())));
            cameraZ.set(cameraZ.get() - CAMERA_TRANSLATE_SPEED * (Math.sin(pitch.get())));
        }
        if (inputMap.get(KeyCode.A)) {
            cameraX.set(cameraX.get() - CAMERA_TRANSLATE_SPEED * (Math.sin(yaw.get())));
            cameraY.set(cameraY.get() + CAMERA_TRANSLATE_SPEED * (Math.cos(yaw.get())));
        }
        if (inputMap.get(KeyCode.D)) {
            cameraX.set(cameraX.get() + CAMERA_TRANSLATE_SPEED * (Math.sin(yaw.get())));
            cameraY.set(cameraY.get() - CAMERA_TRANSLATE_SPEED * (Math.cos(yaw.get())));
        }
        if (inputMap.get(KeyCode.SHIFT)) {
            cameraZ.set(cameraZ.get() - CAMERA_TRANSLATE_SPEED);
        }
        if (inputMap.get(KeyCode.SPACE)) {
            System.out.println(cameraX.getValue() + " " + cameraY.getValue());
            //cameraZ.set(cameraZ.get() + CAMERA_TRANSLATE_SPEED);
            inputMap.put(KeyCode.SPACE, false);
        }
        if (inputMap.get(KeyCode.R)) {
            //test.update((float)cameraX.get(), (float)cameraY.get(), (float)cameraZ.get());
            test.render();
            //test.test();
            //System.out.println(QuadSquare.squareCount);
            //update = !update;
            inputMap.put(KeyCode.R, false);
        }
        if (service.getState() != Worker.State.RUNNING) {
            //System.out.println(service.getState());
            test.render();
            service.restart();
        } else {
            //System.out.println(service.getState());
        }
//        for (int i = 0; i < mesh.getPoints().size(); i ++) {
//            mesh.getPoints().set(i, 50 * r.nextFloat());
//        }
        long oldFrameTime = frameTimes[frameTimeIndex] ;
        frameTimes[frameTimeIndex] = now ;
        frameTimeIndex = (frameTimeIndex + 1) % frameTimes.length ;
        if (frameTimeIndex == 0) {
            arrayFilled = true ;
        }
        if (arrayFilled) {
            long elapsedNanos = now - oldFrameTime ;
            long elapsedNanosPerFrame = elapsedNanos / frameTimes.length ;
            double frameRate = 1_000_000_000.0 / elapsedNanosPerFrame ;
            fpsLabel.setText(String.format("FPS: %.3f", frameRate));
        }
    }
}
