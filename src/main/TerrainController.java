/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import javafx.animation.AnimationTimer;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Sphere;

/**
 * FXML Controller class
 *
 * @author Administrator
 */
public class TerrainController extends AnimationTimer implements Initializable {

    @FXML
    private AnchorPane mainPane;
    
    private HashMap<KeyCode, Boolean> inputMap = new HashMap<>();
    private SubScene terrainView;
    private Group root = new Group();
    private PerspectiveCamera camera;
    private DoubleProperty pitch;
    private DoubleProperty yaw;
    private DoubleProperty cameraX;
    private DoubleProperty cameraY;
    private DoubleProperty cameraZ;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        terrainView = new SubScene(root, 640, 480, true, SceneAntialiasing.BALANCED);
        mainPane.getChildren().add(terrainView);
        registerKey(KeyCode.W);
        registerKey(KeyCode.A);
        registerKey(KeyCode.S);
        registerKey(KeyCode.D);
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
    }
    
    public void initView() {
        terrainView.setFill(Color.BLACK);
        camera = new PerspectiveCamera(true);
        camera.setFieldOfView(45);
        camera.setNearClip(1);
        camera.setFarClip(1000);
        terrainView.setCamera(camera);
        
        pitch = new SimpleDoubleProperty(0);
        yaw = new SimpleDoubleProperty(0);
        cameraX = new SimpleDoubleProperty(0);
        cameraY = new SimpleDoubleProperty(0);
        cameraZ = new SimpleDoubleProperty(0);
        
        TransformLayer cameraTransform = new TransformLayer();
        cameraTransform.rxProperty().bind(new DoubleBinding() {
            {super.bind(pitch);}

            @Override
            protected double computeValue() {
                return Math.toDegrees(pitch.get());
            }
            
        });
        cameraTransform.ryProperty().bind(new DoubleBinding() {
            {super.bind(yaw);}

            @Override
            protected double computeValue() {
                return Math.toDegrees(yaw.get());
            }
            
        });
        cameraTransform.txProperty().bind(cameraX);
        cameraTransform.tyProperty().bind(cameraY);
        cameraTransform.tzProperty().bind(cameraZ);
        
        root.getChildren().add(cameraTransform);
        cameraTransform.getChildren().add(camera);
    }
    
    public void buildTest() {
        for (int i = -50; i < 50; i += 2) {
            for (int j = -50; j < 50; j += 2) {
                Sphere sphere = new Sphere(0.5, 10);
                sphere.setTranslateX(i);
                sphere.setTranslateY(j);
                sphere.setTranslateZ(10);
                root.getChildren().add(sphere);
            }
        }
    }

    @Override
    public void handle(long now) {
        if (inputMap.get(KeyCode.W)) {
            cameraX.set(cameraX.get() + Math.cos(pitch.get()) * Math.sin(yaw.get()));
            cameraY.set(cameraY.get() + Math.sin(pitch.get()));
            cameraZ.set(cameraZ.get() + Math.cos(pitch.get()) * Math.cos(yaw.get()));
        }
        if (inputMap.get(KeyCode.S)) {
            cameraX.set(cameraX.get() - Math.cos(pitch.get()) * Math.sin(yaw.get()));
            cameraY.set(cameraY.get() - Math.sin(pitch.get()));
            cameraZ.set(cameraZ.get() - Math.cos(pitch.get()) * Math.cos(yaw.get()));
        }
        if (inputMap.get(KeyCode.A)) {
            yaw.set(yaw.get() - 0.02);
        }
        if (inputMap.get(KeyCode.D)) {
            yaw.set(yaw.get() + 0.02);
        }
    }
}
