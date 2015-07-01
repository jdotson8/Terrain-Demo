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
public abstract class Operator implements Evaluable {
    public static final HashMap<String, Operator> OPERATORS = new HashMap<>();
    public static final Operator PLUS = new Operator("+", true, 1, false) {

        @Override
        public double evaluate(ArrayList<ASTNode> arguments, HashMap<String, Double> variables) throws IllegalArgumentException {
            if ((isBinary() && arguments.size() != 2) || (!isBinary() && arguments.size() != 1)) {
                throw new IllegalArgumentException("Wrong number of arguments.");
            } else {
                return arguments.get(0).getValue(variables) + arguments.get(1).getValue(variables);
            }
        }
    };
    public static final Operator MINUS = new Operator("-", true, 1, false) {

        @Override
        public double evaluate(ArrayList<ASTNode> arguments, HashMap<String, Double> variables) throws IllegalArgumentException {
            if ((isBinary() && arguments.size() != 2) || (!isBinary() && arguments.size() != 1)) {
                throw new IllegalArgumentException("Wrong number of arguments.");
            } else {
                return arguments.get(0).getValue(variables) - arguments.get(1).getValue(variables);
            }
        }
    };
    public static final Operator MULTIPLY = new Operator("*", true, 2, false) {

        @Override
        public double evaluate(ArrayList<ASTNode> arguments, HashMap<String, Double> variables) throws IllegalArgumentException {
            if ((isBinary() && arguments.size() != 2) || (!isBinary() && arguments.size() != 1)) {
                throw new IllegalArgumentException("Wrong number of arguments.");
            } else {
                return arguments.get(0).getValue(variables) * arguments.get(1).getValue(variables);
            }
        }
    };
    public static final Operator DIVIDE = new Operator("/", true, 2, false) {

        @Override
        public double evaluate(ArrayList<ASTNode> arguments, HashMap<String, Double> variables) throws IllegalArgumentException {
            if ((isBinary() && arguments.size() != 2) || (!isBinary() && arguments.size() != 1)) {
                throw new IllegalArgumentException("Wrong number of arguments.");
            } else {
                return arguments.get(0).getValue(variables) / arguments.get(1).getValue(variables);
            }
        }
    };
    public static final Operator POWER = new Operator("^", true, 3, true) {

        @Override
        public double evaluate(ArrayList<ASTNode> arguments, HashMap<String, Double> variables) throws IllegalArgumentException {
            if ((isBinary() && arguments.size() != 2) || (!isBinary() && arguments.size() != 1)) {
                throw new IllegalArgumentException("Wrong number of arguments.");
            } else {
                return Math.pow(arguments.get(0).getValue(variables), arguments.get(1).getValue(variables));
            }
        }
    };

    private boolean isBinary;
    private int precedence;
    private boolean isRightAssociative;
            
    public Operator(String pSymbol, boolean pIsBinary, int pPrecedence, boolean pIsRightAssociative) {
        isBinary = pIsBinary;
        precedence = pPrecedence;
        isRightAssociative = pIsRightAssociative;
        OPERATORS.put(pSymbol, this);
    }
    
    public boolean isBinary() {
        return isBinary;
    }
    
    public int comparePrecedence(Operator other) {
        return precedence > other.precedence ? 1 : other.precedence == precedence ? 0 : -1;
    }
    
    public boolean getIsRightAssociative() {
        return isRightAssociative;
    }
    
    public abstract double evaluate(ArrayList<ASTNode> arguments, HashMap<String, Double> variables) throws IllegalArgumentException;
}
