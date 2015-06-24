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
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

/**
 * FXML Controller class
 *
 * @author Administrator
 */
public class TerrainController extends AnimationTimer implements Initializable {
    
    @FXML
    private SubScene terrainView;
    
    private HashMap<KeyCode, Boolean> inputMap;
    private PerspectiveCamera camera;
    private DoubleProperty pitch;
    private DoubleProperty yaw;
    private DoubleProperty forward;
    private DoubleProperty strafe;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        registerKey(KeyCode.W);
        registerKey(KeyCode.A);
        registerKey(KeyCode.S);
        registerKey(KeyCode.D);
        initInputMap(terrainView);
        initView();
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
        
        Group root = new Group();
        TransformLayer topTransform = new TransformLayer();
        TransformLayer bottomTransform = new TransformLayer();
        
        root.getChildren().add(topTransform);
        topTransform.getChildren().add(bottomTransform);
        bottomTransform.getChildren().add(camera);
    }

    @Override
    public void handle(long now) {
        // TODO
    }
}
