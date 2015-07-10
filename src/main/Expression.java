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
public class Expression {
    private ExpressionGrammar grammar;
    private ASTNode root;
    private HashMap<String, Double> values;
    
    public Expression(ExpressionGrammar grammar, String expression) {
        this.grammar = grammar;
        values = new HashMap<String, Double>();
        String exp = expression.replaceAll("\\s+","");
        Scanner parser = new Scanner(exp);
        String[] components = exp.split(grammar.buildDelimiter());
        LinkedList<String> evaluable = new LinkedList<>();
        LinkedList<ASTNode> evaluated = new LinkedList<>();
        String popped = ""; 
        String next = "";
        String previous = "";
        main:
        for (int i = 0; i < components.length; i++) {
            if (components[i].equals("(")) {
                evaluable.push(components[i]);
                break;
            } else if (components[i].equals(",") || components[i].equals(")")) {
                while (!evaluable.isEmpty()) {
                    if (components[i].equals(",") && evaluable.peek().equals("(")) {
                        continue main;
                    }
                    popped = evaluable.pop();
                    if (popped.equals("(")) {
                        if (grammar.containsFunction(evaluable.peek())) {
                            addFunctionNode(grammar.getFunction(evaluable.pop()), evaluated);
                        }
                        continue main;
                    } else if (grammar.containsOperator(popped)) {
                        addOperatorNode(grammar.getOperator(popped), evaluated);
                    }
                }
            } else if (grammar.containsOperator(components[i])) {
                Operator o1;
                Operator o2 = null;
                Operator unary = grammar.getUnaryOperator(components[i]);
                Operator binary = grammar.getBinaryOperator(components[i]);
                boolean unaryRight = unary.isRightAssociative();
                if (unary != null && binary != null) {
                    if (i < components.length - 1){
                        Operator unaryNext = grammar.getUnaryOperator(components[i+1]);
                        Operator binaryNext = grammar.getBinaryOperator(components[i+1]);
                        boolean unaryNextRight = unaryNext.isRightAssociative();
                        if (unaryNext != null && binaryNext != null) {
                            if (!unaryRight && unaryNextRight) {
                                if (unary.comparePrecedence(unaryNext) >= 0) {
                                    o1 = unary;
                                    o2 = binaryNext;
                                } else {
                                    o1 = binary;
                                    o2 = unaryNext;
                                }
                            } else if (unaryRight && unaryNextRight) {
                                o1 = binary;
                                o2 = unaryNext;
                            } else if (!unaryRight && !unaryNextRight) {
                                o1 = unary;
                                o2 = binaryNext;
                            } else {
                                throw new IllegalStateException("Syntax Error");
                            }
                        } else if (unaryNext != null && binaryNext == null) {
                            if (unaryNextRight) {
                                o1 = binary;
                                o2 = unaryNext;
                            } else {
                                throw new IllegalStateException("Syntax Error");
                            }
                        } else if (unaryNext == null && binaryNext != null) {
                            if (!unaryRight) {
                                o1 = unary;
                                o2 = binaryNext;
                            } else {
                                throw new IllegalStateException("Syntax Error");
                            }
                        } else {
                            if (i == 0 || (i > 0 && components[i-1].matches("[,(]"))) {
                                o1 = unary;
                            } else {
                                o1 = binary;
                            }
                        }
                    } else {
                        if (!unaryRight) {
                            o1 = unary;
                        } else {
                            throw new IllegalStateException("Syntax Error");
                        }
                    }
                } else if (unary != null) {
                    o1 = unary;
                } else {
                    o1 = binary;
                }
                Operator o3;
                while (!evaluable.isEmpty()) {
                    if ((o3 = grammar.getOperator(evaluable.peek())) == null) {
                        break;
                    }
                    if ((!o1.isRightAssociative() && o1.comparePrecedence(o3) == 0) || o1.comparePrecedence(o3) < 0) {
                        evaluable.pop();
                        addOperatorNode(o3, evaluated);
                    } else {
                        break;
                    }
                }
                if (!o1.isBinary()) {
                    evaluable.push("u_" + o1.getSymbol());
                } else {
                    evaluable.push("b_" + o1.getSymbol());
                }
                if (o2 != null) {
                    i++;
                    if (!o2.isBinary()) {
                        evaluable.push("u_" + o2.getSymbol());
                    } else {
                        evaluable.push("b_" + o2.getSymbol());
                    }
                }
            } else if (grammar.containsFunction(next)) {
                evaluable.push(next);
            } else if (grammar.containsVariable(next)) {
                evaluated.push(new VariableNode(next, values));
            } else if (next.matches("\\d+(\\.\\d+)?")) {
                evaluated.push(new NumberNode(Double.parseDouble(next)));
            } else {
                throw new IllegalStateException("Syntax Error");
            }
        }
        while (!evaluable.isEmpty()) {
            popped = evaluable.pop();
            if (grammar.containsOperator(popped)) {
                addOperatorNode(grammar.getOperator(popped), evaluated);
            } else if (grammar.containsFunction(popped)) {
                addFunctionNode(grammar.getFunction(popped), evaluated);
            } else {
                throw new IllegalStateException("Error while parsing.");
            }
        }
        if (evaluated.size() != 1) {
            throw new IllegalStateException("Error while parsing.");
        }
        root = evaluated.pop();
    }
    
    private void addOperatorNode(Operator operator, LinkedList<ASTNode> nodes) {
        ArrayList<ASTNode> operands = new ArrayList<>(2);
        operands.add(nodes.pop());
        if (operator.isBinary()) {
            operands.add(0, nodes.pop());
        }
        nodes.push(new OperatorNode(operator, operands));
    }
    
    private void addFunctionNode(Function function, LinkedList<ASTNode> nodes) {
        ArrayList<ASTNode> arguments = new ArrayList<>();
        for (int i = 0; i < function.getArgumentCount(); i++) {
            arguments.add(0, nodes.pop());
        }
        nodes.push(new FunctionNode(function, arguments));
    }
    
    public void setVariable(String name, double value) {
        if (grammar.containsVariable(name)) {
            values.put(name, value);
        } else {
            throw new IllegalArgumentException(name + " is not a variable in this expression.");
        }
    }
    
    public double evaluate() {
        return root.getValue();
    }
    
    public void print() {
        root.print();
    }
}
