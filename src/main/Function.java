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
public abstract class Function implements Evaluable {
    public static final HashMap<String, Function> FUNCTIONS = new HashMap<>();
    public static final Function SQUARE_ROOT = new Function("sqrt", 1) {

        @Override
        public double evaluate(ArrayList<ASTNode> arguments, HashMap<String, Double> variables) throws IllegalArgumentException {
            if (arguments.size() != getArgumentCount()) {
                throw new IllegalArgumentException("Wrong number of operands.");
            } else {
                return Math.sqrt(arguments.get(0).getValue(variables));
            }
        }
    };
    public static final Function SINE = new Function("sin", 1) {

        @Override
        public double evaluate(ArrayList<ASTNode> arguments, HashMap<String, Double> variables) throws IllegalArgumentException {
            if (arguments.size() != getArgumentCount()) {
                throw new IllegalArgumentException("Wrong number of operands.");
            } else {
                return Math.sin(arguments.get(0).getValue(variables));
            }
        }
    };
    public static final Function COSINE = new Function("cos", 1) {

        @Override
        public double evaluate(ArrayList<ASTNode> arguments, HashMap<String, Double> variables) throws IllegalArgumentException {
            if (arguments.size() != getArgumentCount()) {
                throw new IllegalArgumentException("Wrong number of operands.");
            } else {
                return Math.cos(arguments.get(0).getValue(variables));
            }
        }
    };
    public static final Function TANGENT = new Function("tan", 1) {

        @Override
        public double evaluate(ArrayList<ASTNode> arguments, HashMap<String, Double> variables) throws IllegalArgumentException {
            if (arguments.size() != getArgumentCount()) {
                throw new IllegalArgumentException("Wrong number of operands.");
            } else {
                return Math.tan(arguments.get(0).getValue(variables));
            }
        }
    };
    public static final Function ABSOLUTE_VALUE = new Function("abs", 1) {

        @Override
        public double evaluate(ArrayList<ASTNode> arguments, HashMap<String, Double> variables) throws IllegalArgumentException {
            if (arguments.size() != getArgumentCount()) {
                throw new IllegalArgumentException("Wrong number of operands.");
            } else {
                return Math.abs(arguments.get(0).getValue(variables));
            }
        }
    };
    
    private int argumentCount;
    
    public Function(String pSymbol, int pArgumentCount) {
        argumentCount = pArgumentCount;
        FUNCTIONS.put(pSymbol, this);
    }
    
    public int getArgumentCount() {
        return argumentCount;
    }
    
    @Override
    public abstract double evaluate(ArrayList<ASTNode> arguments, HashMap<String, Double> variables);
}
