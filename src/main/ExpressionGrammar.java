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
public class ExpressionGrammar {
    private HashMap<String, Operator> unaryOperators;
    private HashMap<String, Operator> binaryOperators;
    private HashMap<String, Function> functions;
    private ArrayList<String> variables;
    
    public ExpressionGrammar() {
        for (Operator operator : Operator.DEFAULT_OPERATORS) {
            if (operator.isBinary()) {
                binaryOperators.put(operator.getSymbol(), operator);
            } else {
                unaryOperators.put(operator.getSymbol(), operator);
            }
        }
        for (Function function : Function.DEFAULT_FUNCTIONS) {
            functions.put(function.getName(), function);
        }
        variables = new ArrayList<>();
    }
    
    public boolean containsOperator(String symbol) {
        if (symbol.startsWith("u_")) {
            return unaryOperators.containsKey(symbol);
        } else if (symbol.startsWith("b_")) {
            return binaryOperators.containsKey(symbol);
        } else {
            return unaryOperators.containsKey(symbol) || binaryOperators.containsKey(symbol);
        }
    }
    
    public boolean containsUnaryOperator(String symbol) {
        return unaryOperators.containsKey(symbol);
    }
    
    public boolean containsBinaryOperator(String symbol) {
        return binaryOperators.containsKey(symbol);
    }
    
    public boolean containsFunction(String name) {
        return functions.containsKey(name);
    }
    
    public boolean containsVariable(String name) {
        return variables.contains(name);
    }
    
    public Operator getOperator(String symbol) {
        if (symbol.startsWith("u_")) {
            return getUnaryOperator(symbol.substring(2));
        } else if (symbol.startsWith("b_")) {
            return getBinaryOperator(symbol.substring(1));
        } else if (unaryOperators.containsKey(symbol)) {
            return getUnaryOperator(symbol);
        } else {
            return getBinaryOperator(symbol);
        }
    }
    
    public Operator getUnaryOperator(String symbol) {
        return unaryOperators.get(symbol);
    }
    
    public Operator getBinaryOperator(String symbol) {
        return binaryOperators.get(symbol);
    }
    
    public Function getFunction(String name) {
        return functions.get(name);
    }
    
    public void addUnaryOperator(Operator operator) {
        if (!operator.isBinary()) {
            unaryOperators.put(operator.getSymbol(), operator);
        } else {
            throw new IllegalArgumentException("Operator is not unary.");
        }
    }
    
    public void addBinaryOperator(Operator operator) {
        if (operator.isBinary()) {
            binaryOperators.put(null, operator);
        } else {
            throw new IllegalArgumentException("Operator is not unary.");
        }
    }
    
    public void addFunction(Function function) {
        functions.put(function.getName(), function);
    }
    
    public void addVariable(String variable) {
        variables.add(variable);
    }
    
    public String buildDelimiter() {
        String operatorGroup = "";
        String functionGroup = "";
        String variableGroup = "";
        for (String str : unaryOperators.keySet()) {
            if (!operatorGroup.equals("")) {
                operatorGroup += "|";
            }
            str = " " + str;
            String[] escapes = str.split("(?=[*|^+?$<>!=])");
            if (!escapes[0].equals(" ")) {
                operatorGroup += escapes[0].trim();
            }
            for (int i = 1; i < escapes.length; i++) {
                operatorGroup += "\\" + escapes[i];
            }
        }
        for (String str : binaryOperators.keySet()) {
            if (!operatorGroup.equals("")) {
                operatorGroup += "|";
            }
            str = " " + str;
            String[] escapes = str.split("(?=[*|^+?$<>!=])");
            if (!escapes[0].equals(" ")) {
                operatorGroup += escapes[0].trim();
            }
            for (int i = 1; i < escapes.length; i++) {
                operatorGroup += "\\" + escapes[i];
            }
        }
        for (String str : functions.keySet()) {
            if (!functionGroup.equals("")) {
                functionGroup += "|";
            }
            functionGroup += str;
        }
        for (String str : variables) {
            if (!variableGroup.equals("")) {
                variableGroup += "|";
            }
            variableGroup += str;
        }
        return String.format("((?<=%1$s)(?=%2$s|%3$s|\\d|\\())|((?<=%2$s)(?=\\())|((?<=%3$s|\\d|\\))(?=%1$s|\\)|,))|((?<=\\(|,)(?=%1$s|%2$s|%3$s|\\d|\\())", operatorGroup, functionGroup, variableGroup);
    }
}
