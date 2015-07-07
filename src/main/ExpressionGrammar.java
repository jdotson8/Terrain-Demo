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
    private HashMap<String, Operator> operators;
    private HashMap<String, Function> functions;
    private ArrayList<String> variables;
    
    public ExpressionGrammar() {
        operators = new HashMap<>(Operator.DEFAULT_OPERATORS);
        functions = new HashMap<>(Function.DEFAULT_FUNCTIONS);
        variables = new ArrayList<>();
    }
    
    public boolean containsOperator(String symbol) {
        return operators.containsKey(symbol);
    }
    
    public boolean containsFunction(String name) {
        return functions.containsKey(name);
    }
    
    public boolean containsVariable(String name) {
        return variables.contains(name);
    }
    
    public Operator getOperator(String symbol) {
        return operators.get(symbol);
    }
    
    public Function getFunction(String name) {
        return functions.get(name);
    }
    
    public void addOperator(Operator operator) {
        operators.put(operator.getSymbol(), operator);
    }
    
    public void addFunction(Function function) {
        functions.put(function.getName(), function);
    }
    
    public void addVariable(String variable) {
        variables.add(variable);
    }
    
    public String buildDelimiter() {
        String characterClass = "";
        String words = "";
        for (String str : operators.keySet()) {
            if (str.length() == 1) {
                characterClass += str.matches("[|\\-\\^]") ? "\\" + str : str;
            } else {
                words += "|" + str;
            }
        }
        for (String str : functions.keySet()) {
            words += "|" + str;
        }
        for (String str : variables) {
            words += "|" + str;
        }
        return String.format("((?<=([%1$s(),]%2$s))|(?=([%1$s(),]%2$s)))", characterClass, words);
    }
}
