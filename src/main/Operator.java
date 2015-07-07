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
public abstract class Operator {
    public static HashMap<String, Operator> DEFAULT_OPERATORS = new HashMap<>();
    static {
        DEFAULT_OPERATORS.put("+", new Operator("+", true, false, 1){

            @Override
            public double operate(ArrayList<ASTNode> operands) {
                return operands.get(0).getValue() + operands.get(1).getValue();
            }
        });
        DEFAULT_OPERATORS.put("-", new Operator("-", true, false, 1){

            @Override
            public double operate(ArrayList<ASTNode> operands) {
                return operands.get(0).getValue() - operands.get(1).getValue();
            }
        });
        DEFAULT_OPERATORS.put("*", new Operator("*", true, false, 2){

            @Override
            public double operate(ArrayList<ASTNode> operands) {
                return operands.get(0).getValue() * operands.get(1).getValue();
            }
        });
        DEFAULT_OPERATORS.put("/", new Operator("/", true, false, 2){

            @Override
            public double operate(ArrayList<ASTNode> operands) {
                return operands.get(0).getValue() / operands.get(1).getValue();
            }
        });
        DEFAULT_OPERATORS.put("^", new Operator("^", true, true, 3){

            @Override
            public double operate(ArrayList<ASTNode> operands) {
                return Math.pow(operands.get(0).getValue(), operands.get(1).getValue());
            }
        });
    }
    
    private String symbol;
    private boolean isBinary;
    private boolean isRightAssociative;
    private int precedence;
            
    protected Operator(String symbol, boolean isBinary, boolean isRightAssociative, int precedence) {
        if (!symbol.matches("[\\-+/*^!@#$%&:;',.<>?=_|~]*+")) {
            throw new IllegalArgumentException("Invalid Operator Symbol");
        } else {
            this.symbol = symbol;
        }
        this.isBinary = isBinary;
        this.isRightAssociative = isRightAssociative;
        this.precedence = precedence;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public boolean isBinary() {
        return isBinary;
    }
    
    public boolean isRightAssociative() {
        return isRightAssociative;
    }
    
    public int comparePrecedence(Operator other) {
        return precedence > other.precedence ? 1 : other.precedence == precedence ? 0 : -1;
    }
    
    public abstract double operate(ArrayList<ASTNode> operands);
}
