/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Point3D;
import main.expressions.Expression;
import main.expressions.ExpressionGrammar;

/**
 *
 * @author Administrator
 */
public class Noise2D {
    private static final ExpressionGrammar NOISE_GRAMMAR = new ExpressionGrammar();
    
    private static final double F2 = 0.5*(Math.sqrt(3.0)-1.0);
    private static final double G2 = (3.0-Math.sqrt(3.0))/6.0;
    private static final double CONTRIBUTION_FACTOR = 0.5;
    private static final double NORMALIZE_FACTOR = 70.0;
    private static final Point3D[] GTAB = {
        new Point3D(1, 1, 0), new Point3D(-1, 1, 0), new Point3D(1, -1, 0),
        new Point3D(-1, -1, 0), new Point3D(1, 0, 1), new Point3D(-1, 0, 1),
        new Point3D(1, 0, -1), new Point3D(-1, 0, -1), new Point3D(0, 1, 1),
        new Point3D(0, -1, 1), new Point3D(0, 1, -1), new Point3D(0, -1, -1)};
    private static final int SUPPLY_LENGTH = 256;
    private static final short[] SUPPLY = new short[SUPPLY_LENGTH];
    static {
        for (int i = 0; i < SUPPLY_LENGTH; i++) {
            SUPPLY[i] = (short) i;
        }
        NOISE_GRAMMAR.addVariable("x");
    }

    private final short[] perm;
    private final short[] permMod12 = new short[2 * SUPPLY_LENGTH];
    private ArrayList<NoiseLayer> noiseLayers;
    
    public Noise2D(long seed) {
        Random r = new Random(seed);
        perm = Arrays.copyOf(SUPPLY, 2 * SUPPLY_LENGTH);
        for (int i = 0; i < SUPPLY_LENGTH; i++) {
            int j = r.nextInt(SUPPLY_LENGTH);
            short temp = perm[i];
            perm[i] = perm[j];
            perm[j] = temp;
        }
        for (int i = 0; i < 2 * SUPPLY_LENGTH; i++) {
            perm[i] = perm[i & (SUPPLY_LENGTH - 1)];
            permMod12[i] = (short) (perm[i] % GTAB.length);
        }
    }
    
    private static int fastfloor(final double x) {
        int floorX = (int) x;
        if (x < floorX) {
            return floorX - 1;
        } else {
            return floorX;
        }
    }
    
    public double sample(double xin, double yin) {
        double n0, n1, n2;
        double s = (xin + yin) * F2;
        int i = fastfloor(xin + s);
        int j = fastfloor(yin + s);
        double t = (i + j) * G2;
        double X0 = i - t;
        double Y0 = j - t;
        double x0 = xin - X0;
        double y0 = yin - Y0;
        int i1, j1;
        if(x0 > y0) {
            i1 = 1;
            j1 = 0;
        } else {
            i1 = 0;
            j1 = 1;
        }
        double x1 = x0 - i1 + G2;
        double y1 = y0 - j1 + G2;
        double x2 = x0 - 1.0 + 2.0 * G2;
        double y2 = y0 - 1.0 + 2.0 * G2;
        int ii = i & (SUPPLY_LENGTH - 1);
        int jj = j & (SUPPLY_LENGTH - 1);
        int gi0 = permMod12[perm[ii + perm[jj]]];
        int gi1 = permMod12[perm[ii + i1 + perm[jj + j1]]];
        int gi2 = permMod12[perm[ii + 1 + perm[jj + 1]]];
        double t0 = CONTRIBUTION_FACTOR - x0 * x0 - y0 * y0;
        if(t0 < 0) {
            n0 = 0.0;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * GTAB[gi0].dotProduct(x0, y0, 0);
        }
        double t1 = CONTRIBUTION_FACTOR - x1 * x1 - y1 * y1;
        if(t1 < 0) {
            n1 = 0.0;
        } else {
            t1 *= t1;
            n1 = t1 * t1 * GTAB[gi1].dotProduct(x1, y1, 0);
        }
        double t2 = CONTRIBUTION_FACTOR - x2 * x2 - y2 * y2;
        if(t2 < 0) {
            n2 = 0.0;
        } else {
            t2 *= t2;
            n2 = t2 * t2 * GTAB[gi2].dotProduct(x2, y2, 0);
        }
        return NORMALIZE_FACTOR * (n0 + n1 + n2);
    }
    
    public void addNoiseLayer(double amplitude, double frequency, String expression) {
        noiseLayers.add(new NoiseLayer());
    }
    
    public void setAmplitude(int index, double amplitude) {
        noiseLayers.get(index).setAmplitude(amplitude);
    }
    
    public void setFrequency(int index, double frequency) {
        noiseLayers.get(index).setFrequency(frequency);
    }
    
    public void setExpression(int index, String expString) {
        noiseLayers.get(index).changeExpression(expString);
    }
    
    public double getValue(double x, double y) {
        double value = 0;
        for (NoiseLayer layer : noiseLayers) {
            value += layer.getValue(x, y);
        }
        return value;
    }
    
    

    
    private class NoiseLayer {
        double amplitude;
        double frequency;
        Expression expression;

        public NoiseLayer() {
            amplitude = 0;
            frequency = 0;
            this.expression = new Expression(NOISE_GRAMMAR);
        }
        
        public void setAmplitude(double amplitude) {
            this.amplitude = amplitude;
        }
        
        public void setFrequency(double frequency) {
            this.frequency = frequency;
        }
        
        public void changeExpression(String expString) {
            expression.buildExpression(expString);
        }

        public double getValue(double x, double y) {
            expression.setVariable("x", amplitude * sample(frequency * x, frequency * y));
            return expression.evaluate();
        }
    }
}
