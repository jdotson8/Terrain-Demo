/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Administrator
 */
public class FunctionNode implements ASTNode {
    private Function function;
    private ArrayList<ASTNode> arguments;
    
    public FunctionNode(Function function, ArrayList<ASTNode> arguments) {
        this.function = function;
        if (arguments.size() == function.getArgumentCount()) {
            this.arguments = arguments;
        } else {
            throw new IllegalArgumentException("Wrong Number of Arguments");
        }
    }

    @Override
    public double getValue(Map<String, Double> values) {
        return function.evaluate(arguments, values);
    }
    
    @Override
    public void print() {
        System.out.println(function.getName());
        for(ASTNode args : arguments) {
            args.print();
        }
    }
}
