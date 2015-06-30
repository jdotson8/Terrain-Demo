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
    public static final HashMap<String, Operator> OPERATORS = new HashMap<>();
    public static final Operator PLUS = new Operator("+", 2, 1, false) {

        @Override
        public double operate(ArrayList<ASTNode> operands, double x) throws IllegalArgumentException {
            if (operands.size() != getOperandCount()) {
                throw new IllegalArgumentException("Wrong number of operands.");
            } else {
                return operands.get(0).getValue(x) + operands.get(1).getValue(x);
            }
        }
    };
    public static final Operator MINUS = new Operator("-", 2, 1, false) {

        @Override
        public double operate(ArrayList<ASTNode> operands, double x) throws IllegalArgumentException {
            if (operands.size() != getOperandCount()) {
                throw new IllegalArgumentException("Wrong number of operands.");
            } else {
                return operands.get(0).getValue(x) - operands.get(1).getValue(x);
            }
        }
    };
    public static final Operator MULTIPLY = new Operator("*", 2, 1, false) {

        @Override
        public double operate(ArrayList<ASTNode> operands, double x) throws IllegalArgumentException {
            if (operands.size() != getOperandCount()) {
                throw new IllegalArgumentException("Wrong number of operands.");
            } else {
                return operands.get(0).getValue(x) * operands.get(1).getValue(x);
            }
        }
    };
    public static final Operator DIVIDE = new Operator("/", 2, 1, false) {

        @Override
        public double operate(ArrayList<ASTNode> operands, double x) throws IllegalArgumentException {
            if (operands.size() != getOperandCount()) {
                throw new IllegalArgumentException("Wrong number of operands.");
            } else {
                return operands.get(0).getValue(x) / operands.get(1).getValue(x);
            }
        }
    };
    public static final Operator POWER = new Operator("^", 2, 1, true) {

        @Override
        public double operate(ArrayList<ASTNode> operands, double x) throws IllegalArgumentException {
            if (operands.size() != getOperandCount()) {
                throw new IllegalArgumentException("Wrong number of operands.");
            } else {
                return Math.pow(operands.get(0).getValue(x), operands.get(1).getValue(x));
            }
        }
    };
    public static final Operator SQUARE_ROOT = new Operator("sqrt", 2, 1, true) {

        @Override
        public double operate(ArrayList<ASTNode> operands, double x) throws IllegalArgumentException {
            if (operands.size() != getOperandCount()) {
                throw new IllegalArgumentException("Wrong number of operands.");
            } else {
                return Math.sqrt(operands.get(0).getValue(x));
            }
        }
    };
    public static final Operator SINE = new Operator("sin", 2, 1, true) {

        @Override
        public double operate(ArrayList<ASTNode> operands, double x) throws IllegalArgumentException {
            if (operands.size() != getOperandCount()) {
                throw new IllegalArgumentException("Wrong number of operands.");
            } else {
                return Math.sin(operands.get(0).getValue(x));
            }
        }
    };
    public static final Operator COSINE = new Operator("cos", 2, 1, true) {

        @Override
        public double operate(ArrayList<ASTNode> operands, double x) throws IllegalArgumentException {
            if (operands.size() != getOperandCount()) {
                throw new IllegalArgumentException("Wrong number of operands.");
            } else {
                return Math.cos(operands.get(0).getValue(x));
            }
        }
    };
    public static final Operator TANGENT = new Operator("tan", 2, 1, true) {

        @Override
        public double operate(ArrayList<ASTNode> operands, double x) throws IllegalArgumentException {
            if (operands.size() != getOperandCount()) {
                throw new IllegalArgumentException("Wrong number of operands.");
            } else {
                return Math.tan(operands.get(0).getValue(x));
            }
        }
    };
    public static final Operator ABSOLUTE_VALUE = new Operator("abs", 2, 1, true) {

        @Override
        public double operate(ArrayList<ASTNode> operands, double x) throws IllegalArgumentException {
            if (operands.size() != getOperandCount()) {
                throw new IllegalArgumentException("Wrong number of operands.");
            } else {
                return Math.abs(operands.get(0).getValue(x));
            }
        }
    };

    private int operandCount;
    private int precedence;
    private boolean rightAssociative;
            
    public Operator(String pSymbol, int pOperandCount, int pPrecedence, boolean pRightAssociative) {
        operandCount = pOperandCount;
        precedence = pPrecedence;
        rightAssociative = pRightAssociative;
        OPERATORS.put(pSymbol, this);
    }
    
    public int getOperandCount() {
        return operandCount;
    }
    
    public int comparePrecedence(Operator other) {
        return precedence > other.precedence ? 1 : other.precedence == precedence ? 0 : -1;
    }
    
    public boolean isRightAssociative() {
        return rightAssociative;
    }
    
    public abstract double operate(ArrayList<ASTNode> operands, double x) throws IllegalArgumentException;
}
