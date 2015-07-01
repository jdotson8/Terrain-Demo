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
public class ASTEvaluableNode extends ASTNode {
    private Evaluable evaluable;
    private ArrayList<ASTNode> arguments;
    
    public ASTEvaluableNode(Evaluable pEvaluable, ArrayList<ASTNode> pArguments) {
        evaluable = pEvaluable;
        arguments = pArguments;
    }

    @Override
    public double getValue(HashMap<String, Double> variables) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
