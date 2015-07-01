/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

/**
 *
 * @author Administrator
 */
public abstract class ASTNode {
    public static ASTNode parseExpression(String expression) {
        String exp = expression.replaceAll("\\s+","");
        Scanner parser = new Scanner(exp);
        parser.useDelimiter(buildDelimiter());
        LinkedList<String> evaluableStack = new LinkedList<>();
        LinkedList<ASTNode> evaluatedStack = new LinkedList<>();
        main:
        while (parser.hasNext()) {
            String next = parser.next();
            String popped;
            switch (next) {
                case "(":
                    evaluableStack.addFirst(next);
                    break;
                case ")":
                    while (!evaluableStack.isEmpty()) {
                        popped = evaluableStack.remove();
                        if ("(".equals(popped)) {
                            continue main;
                        } else if (Operator.OPERATORS.containsKey(popped)) {
                            ArrayList<ASTNode> arguments = new ArrayList<>();
                            Operator op = Operator.OPERATORS.get(popped);
                            arguments.add(evaluatedStack.remove());
                            if (op.isBinary()) {
                                arguments.add(evaluatedStack.remove());
                            }
                            evaluatedStack.addFirst(new ASTEvaluableNode(op, arguments));
                        } else if (Function.FUNCTIONS.containsKey(popped)) {
                            ArrayList<ASTNode> arguments = new ArrayList<>();
                            Function func = Function.FUNCTIONS.get(popped);
                            for (int i = 0; i < func.getArgumentCount(); i++) {
                                arguments.add(evaluatedStack.remove());
                            }
                        } else {
                            throw new IllegalStateException("Error while parsing.");
                        }
                    }
            }
        }
        return new ASTVariableNode();
    }
    
    private static String buildDelimiter() {
        String characterClass = "";
        String words = "";
        for (String str : Operator.OPERATORS.keySet()) {
            if (str.length() == 1) {
                characterClass += str.matches("[\\[|\\]\\-\\^]") ? "\\" + str : str;
            } else {
                words += "|" + str;
            }
        }
        for (String str : Function.FUNCTIONS.keySet()) {
            if (str.length() == 1) {
                characterClass += str.matches("[\\[|\\]\\-\\^]") ? "\\" + str : str;
            } else {
                words += "|" + str;
            }
        }
        return String.format("((?<=([%1$s(),]%2$s))|(?=([%1$s(),]%2$s)))", characterClass, words);
    }
    
    public abstract double getValue(HashMap<String, Double> variables);
}
