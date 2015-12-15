/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.expressions;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Administrator
 */
public class VariableNode implements ASTNode {
    String name;

    public VariableNode(String name) {
        if (!name.matches("[a-zA-Z_]*+")) {
            throw new IllegalArgumentException("Invalid variable name.");
        } else {
            this.name = name;
        }
    }

    @Override
    public double getValue(Map<String, Double> values) {
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
