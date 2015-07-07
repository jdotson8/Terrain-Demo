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
        parser.useDelimiter(grammar.buildDelimiter());
        LinkedList<String> evaluable = new LinkedList<>();
        LinkedList<ASTNode> evaluated = new LinkedList<>();
        String popped = ""; 
        String next = "";
        String previous = "";
        main:
        while (parser.hasNext()) {
            next = parser.next();
            switch (next) {
                case "(":
                    evaluable.push(next);
                    break;
                case ",":
                case ")":
                    while (!evaluable.isEmpty()) {
                        if (next.equals(",") && evaluable.peek().equals("(")) {
                            continue main;
                        }
                        popped = evaluable.pop();
                        if (popped.equals("(")) {
                            continue main;
                        } else if (grammar.containsOperator(popped)) {
                            addOperatorNode(grammar.getOperator(popped), evaluated);
                        } else {
                            addFunctionNode(grammar.getFunction(popped), evaluated);
                        }
                    }
                    break;
                default:
                    if (grammar.containsOperator(next)) {
                        Operator o1 = grammar.getOperator(next);
                        Operator o2;
                        while (!evaluable.isEmpty()) {
                            if ((o2 = grammar.getOperator(evaluable.peek())) == null) {
                                break;
                            }
                            if ((!o1.isRightAssociative() && o1.comparePrecedence(o2) == 0) || o1.comparePrecedence(o2) < 0) {
                                evaluable.pop();
                                addOperatorNode(o2, evaluated);
                            } else {
                                break;
                            }
                        }
                        evaluable.push(next);
                    } else if (grammar.containsFunction(next)) {
                        evaluable.push(next);
                    } else if (grammar.containsVariable(next)) {
                        if (grammar.containsVariable(previous)) {
                            evaluable.push("*");
                        }
                        evaluated.push(new VariableNode(next, values));
                    } else if (next.matches("\\d+(\\.\\d+)?")) {
                        evaluated.push(new NumberNode(Double.parseDouble(next)));
                    } else {
                        throw new IllegalStateException("Error while parsing.");
                    }
                
            }
            previous = next;
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
        for (int i = function.getArgumentCount() - 1; i >= 0; i--) {
            arguments.set(i, nodes.pop());
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
