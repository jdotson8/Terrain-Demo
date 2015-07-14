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
public class NumberNode implements ASTNode {
    private double value;
    
    public NumberNode(double value) {
        this.value = value;
    }

    @Override
    public double getValue() {
        return value;
    }
    
    @Override
    public void print() {
        System.out.print(value + " ");
    }
}
