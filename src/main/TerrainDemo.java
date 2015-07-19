/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import main.expressions.ASTNode;
import main.expressions.ExpressionGrammar;
import main.expressions.Operator;
import main.expressions.Expression;
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
        grammar.addOperator(new Operator("!", false, false, 0) {

            @Override
            public double operate(List<ASTNode> operands) {
                return operands.get(0).getValue()*2;
            }
            
        });
        grammar.addOperator(new Operator("!", true, false, 1) {

            @Override
            public double operate(List<ASTNode> operands) {
                return operands.get(0).getValue()*operands.get(1).getValue();
            }
            
        });
        Expression exp = new Expression(grammar, "-sin!-2");
        exp.setVariable("sin", 5);
        exp.print();
        System.out.println(exp.evaluate());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
