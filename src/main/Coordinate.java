/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

/**
 *
 * @author Administrator
 */
public class Coordinate {
    private float x;
    private float y;
    
    public Coordinate(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
    
    @Override
    public int hashCode() {
        int bits = Float.floatToIntBits(x);
        bits = bits * 15 ^ Float.floatToIntBits(y);
        return ((bits >> 16) ^ 1);
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof Coordinate))
            return false;
        Coordinate c = (Coordinate)o;
        return x == c.x && y == c.y;
    }
}
