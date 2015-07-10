/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
        
        /*ExpressionGrammar grammar = new ExpressionGrammar();
        grammar.addVariable("x");
        grammar.addVariable("six");
        Expression exp = new Expression(grammar, "sixlog10( sqrt(x^x * x))");
        exp.setVariable("six", 2);
        exp.setVariable("x", 2);
        //exp.print();
        System.out.println(exp.evaluate());*/
        ExpressionGrammar grammar = new ExpressionGrammar();
        grammar.addVariable("sin");
        /*System.out.println(del);
        while (test.hasNext()) {
            System.out.println(test.next());
        }*/
        Expression exp = new Expression(grammar, "sin*sin(50)");
        exp.setVariable("sin", 5);
        System.out.println(exp.evaluate());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
