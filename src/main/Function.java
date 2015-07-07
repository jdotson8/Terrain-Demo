/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Administrator
 */
public abstract class Function {
    public static HashMap<String, Function> DEFAULT_FUNCTIONS = new HashMap<>();
    static {
        DEFAULT_FUNCTIONS.put("sin", new Function("sin", 1) {

            @Override
            public double evaluate(ArrayList<ASTNode> arguments) {
                return Math.sin(arguments.get(0).getValue());
            }
        });
    }
    
    private String name;
    private int argumentCount;
    
    protected Function(String name, int argumentCount) {
        if (!name.matches("([_]*+\\w*+)*+")) {
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
    
    public abstract double evaluate(ArrayList<ASTNode> arguments);
}
