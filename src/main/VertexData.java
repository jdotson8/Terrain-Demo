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
    private float[] normal = new float[3];
    private float normalizer;
    private float error;
    private boolean enabled;
    private int dependencyCount;
    
    VertexData(float height, float error) {
        this.height = height;
        this.error = error;
        enabled = false;
    }
    
    public float getHeight() {
        return height;
    }
    
    public void addNormal(float nx, float ny, float nz, float weight) {
        normal[0] += nx;
        normal[1] += ny;
        normal[2] += nz;
        normalizer += weight;
    }
    
    public void removeNormal(float nx, float ny, float nz, float weight) {
        normal[0] -= nx;
        normal[1] -= ny;
        normal[2] -= nz;
        normalizer -= weight;
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
    
    public int getDependencyCount() {
        return dependencyCount;
    }
    
    public void incDependencyCount() {
        dependencyCount++;
    }
    
    public void decDependencyCount() {
        dependencyCount--;
    }
}
