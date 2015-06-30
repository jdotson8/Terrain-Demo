/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.Scanner;

/**
 *
 * @author Administrator
 */
public abstract class ASTNode {
    public static ASTNode parseExpression(String expression) {
        String exp = expression.replaceAll("\\s+","");
        Scanner parser = new Scanner(expression);
        StringBuilder delimiter = new StringBuilder("((?<=\\()|(?=\\())|((?<=\\))|(?=\\)))");
        for (String str : Operator.OPERATORS.keySet()) {
            delimiter.append(String.format("((?<=\\%1$s)|(?=\\1$s))", str));
        }
        parser.useDelimiter(delimiter.toString());
        
        return new ASTVariableNode();
    }
    
    public abstract double getValue(double x);
}
