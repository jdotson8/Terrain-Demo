/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

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
        
        /*String expression = "1 + sin(3 * 4 / 2)";
        String exp = expression.replaceAll("\\s+","");
        Scanner parser = new Scanner(exp);
        StringBuilder delimiter = new StringBuilder("((?<=\\()|(?=\\())|((?<=\\))|(?=\\)))");
        for (String str : Operator.OPERATORS.keySet()) {
            if (str.length() == 1) {
                delimiter.append(String.format("|((?<=\\%1$s)|(?=\\%1$s))", str));
            } else {
                delimiter.append(String.format("|((?<=%1$s)|(?=%1$s))", str));
            }
        }
        parser.useDelimiter(delimiter.toString());
        System.out.println(delimiter);
        System.out.println(parser.next());
        System.out.println(parser.next());
        System.out.println(parser.next());
        System.out.println(parser.next());
        System.out.println(parser.next());
        System.out.println(parser.next());
        System.out.println(parser.next());
        System.out.println(parser.next());
        System.out.println(parser.next());*/
        String expression = "1 + 0.3453*x  * sinsinsinsinsin(2+ x/  7.0)";
        String exp = expression.replaceAll("\\s+","");
        Scanner parser = new Scanner(exp);
        parser.useDelimiter(buildDelimiter());
        System.out.println(parser.delimiter());
        while(parser.hasNext()) {
            System.out.println(parser.next());
        }
    }
    
    private String buildDelimiter() {
        String characterClass = "";
        String words = "";
        for (String str : Operator.OPERATORS.keySet()) {
            if (str.length() == 1) {
                characterClass += str.matches("[\\[|\\]\\-\\^]") ? "\\" + str : str;
            } else {
                words += "|" + str;
            }
        }
        for (String str : Function.FUNCTIONS.keySet()) {
            if (str.length() == 1) {
                characterClass += str.matches("[\\[|\\]\\-\\^]") ? "\\" + str : str;
            } else {
                words += "|" + str;
            }
        }
        return String.format("((?<=([%1$s(),]%2$s))|(?=([%1$s(),]%2$s)))", characterClass, words);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
