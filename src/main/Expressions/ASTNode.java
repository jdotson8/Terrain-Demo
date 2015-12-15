/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.expressions;

import java.util.Map;

/**
 *
 * @author Administrator
 */
public interface ASTNode {
    public abstract double getValue(Map<String, Double> values);
    public abstract void print();
}
