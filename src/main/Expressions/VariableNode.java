/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.Expressions;

import java.util.HashMap;

/**
 *
 * @author Administrator
 */
public class VariableNode implements ASTNode {
    String name;
    HashMap<String, Double> values;

    public VariableNode(String name, HashMap<String, Double> values) {
        if (!name.matches("[a-zA-Z_]*+")) {
            throw new IllegalArgumentException("Invalid variable name.");
        } else {
            this.name = name;
        }
        this.values = values;
    }

    @Override
    public double getValue() {
        if (values.containsKey(name)) {
            return values.get(name);
        } else {
            throw new IllegalArgumentException("Variable " + name + " is undefined.");
        }
    }
    
    @Override
    public void print() {
        System.out.print(name + " ");
    }
}
