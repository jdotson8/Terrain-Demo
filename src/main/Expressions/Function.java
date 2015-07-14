/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.Expressions;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrator
 */
public abstract class Function {
    public static ArrayList<Function> DEFAULT_FUNCTIONS = new ArrayList<>();
    static {
        DEFAULT_FUNCTIONS.add(new Function("sin", 1) {

            @Override
            public double evaluate(List<ASTNode> arguments) {
                return Math.sin(arguments.get(0).getValue());
            }
        });
        DEFAULT_FUNCTIONS.add(new Function("cos", 1) {

            @Override
            public double evaluate(List<ASTNode> arguments) {
                return Math.cos(arguments.get(0).getValue());
            }
        });
        DEFAULT_FUNCTIONS.add(new Function("tan", 1) {

            @Override
            public double evaluate(List<ASTNode> arguments) {
                return Math.sin(arguments.get(0).getValue());
            }
        });
        DEFAULT_FUNCTIONS.add(new Function("log", 1) {

            @Override
            public double evaluate(List<ASTNode> arguments) {
                return Math.log(arguments.get(0).getValue());
            }
        });
        DEFAULT_FUNCTIONS.add(new Function("log10", 1) {

            @Override
            public double evaluate(List<ASTNode> arguments) {
                return Math.log10(arguments.get(0).getValue());
            }
        });
        DEFAULT_FUNCTIONS.add(new Function("logb", 2) {

            @Override
            public double evaluate(List<ASTNode> arguments) {
                return Math.log(arguments.get(0).getValue()) / Math.log(arguments.get(1).getValue());
            }
        });
        DEFAULT_FUNCTIONS.add(new Function("abs", 1) {

            @Override
            public double evaluate(List<ASTNode> arguments) {
                return Math.abs(arguments.get(0).getValue());
            }
        });
        DEFAULT_FUNCTIONS.add(new Function("sqrt", 1) {

            @Override
            public double evaluate(List<ASTNode> arguments) {
                return Math.sqrt(arguments.get(0).getValue());
            }
        });
    }
    
    private String name;
    private int argumentCount;
    
    public Function(String name, int argumentCount) {
        if (!name.matches("[a-zA-Z_]*+[\\w]*+")) {
            throw new IllegalArgumentException("Invalid Function Name");
        } else {
            this.name = name;
        }
        this.argumentCount = argumentCount;
    }
    
    public String getName() {
        return name;
    }
    
    public int getArgumentCount() {
        return argumentCount;
    }
    
    public abstract double evaluate(List<ASTNode> arguments);
}
