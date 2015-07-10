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
public class OperatorNode extends ASTNode{
    private Operator operator;
    private ArrayList<ASTNode> operands;
    
    public OperatorNode(Operator operator, ArrayList<ASTNode> operands) {
        this.operator = operator;
        if ((operator.isBinary() && operands.size() == 2) || (!operator.isBinary() && operands.size() == 1)) {
            this.operands = operands;
        } else {
            throw new IllegalArgumentException("Wrong number of operands");
        }
    }

    @Override
    public double getValue() {
        return operator.operate(operands);
    }

    @Override
    public void print() {
        System.out.println(operator.getSymbol() + " " + operands.size());
    }
}
