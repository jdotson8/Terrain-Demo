/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.awt.geom.Point2D;
import main.expressions.ASTNode;
import main.expressions.ExpressionGrammar;
import main.expressions.Operator;
import main.expressions.Expression;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.shape.TriangleMesh;
import javafx.stage.Stage;

/**
 *
 * @author Administrator
 */
public class TerrainDemo extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("Terrain.fxml"));
        
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        Coordinate a = new Coordinate(6f, 5f);
        Coordinate b = new Coordinate(6f, 5f);
        Coordinate c = new Coordinate(5f, 6f);
        Coordinate d = new Coordinate(5f, 5f);
        
        ConcurrentHashMap<Coordinate, VertexData> test = new ConcurrentHashMap<>();
        ExecutorService ex = Executors.newFixedThreadPool(5, new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable runnable) {
                Thread thread = Executors.defaultThreadFactory()
                        .newThread(runnable);
                thread.setDaemon(true);
                return thread;
            }
        });
        for (int i = 0; i < 100; i++) {
            test.put(new Coordinate(i,i), new VertexData(5f));
        }
        Task<Boolean> task1 = new Task<Boolean>() {

            @Override
            protected Boolean call() throws Exception {
                System.out.println("Starting task 1");
                test.computeIfPresent(new Coordinate(5,5), (key, value) -> {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        System.out.println("Interrupted");
                    }
                    value.setError(10f);
                    return value;
                });
                System.out.println("Finishing 1");
                return true;
            }
        };
        Task<Boolean> task2 = new Task<Boolean>() {

            @Override
            protected Boolean call() throws Exception {
                System.out.println("Starting task 2");
                //Thread.sleep(5000);
                System.out.println("Error: " + test.get(new Coordinate(5,5)).getError());
                System.out.println("Finishing 2");
                return true;
            }
            
        };
        ex.submit(task1);
        ex.submit(task2);
        
        
        /*System.out.println(a.hashCode() == b.hashCode());
        System.out.println(a.hashCode() == c.hashCode());
        System.out.println(a.hashCode() == d.hashCode());
        /*for (int squareindex = 0; squareindex < 4; squareindex++) {
            for (int vertindex = 0; vertindex < 4; vertindex++) {
                int test1 = (squareindex ^ 1) ^ ((vertindex & 1) << 1);
                boolean test2 = (((vertindex - squareindex) & 2) > 0) ? true : false;
                System.out.println("squareindex: " + squareindex + " vertindex: " + vertindex);
                System.out.println(test1);
                System.out.println(test2);
            }
        }*/
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
