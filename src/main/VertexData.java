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
public class VertexData {
    private float height;
    private float error;
    private boolean enabled;
    
    VertexData(float height, float error) {
        this.height = height;
        this.error = error;
        enabled = false;
    }
    
    public float getHeight() {
        return height;
    }
    
    public float getError() {
        return error;
    }
    
    public void setError(float error) {
        this.error = error;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
}
