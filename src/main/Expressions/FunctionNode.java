/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.Expressions;

import java.util.ArrayList;
import java.util.HashMap;

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
    public double getValue() {
        return function.evaluate(arguments);
    }
    
    @Override
    public void print() {
        System.out.println(function.getName());
        for(ASTNode args : arguments) {
            args.print();
        }
    }
}
