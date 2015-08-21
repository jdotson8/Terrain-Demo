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
    private Vec3D normal;
    private float normalizer;
    private float error;
    private boolean enabled;
    private int dependencyCount;
    public int normalCount;
    
    VertexData(float pHeight, float pError) {
        height = pHeight;
        error = pError;
        normal = new Vec3D();
        enabled = false;
    }
    
    public float getHeight() {
        return height;
    }
    
    public void addNormal(Vec3D pNormal, float weight) {
        normal.add(pNormal);
        normalizer += weight;
        normalCount++;
    }
    
    public void removeNormal(Vec3D pNormal, float weight) {
        normal.sub(pNormal);
        normalizer -= weight;
        normalCount--;
    }
    
    public Vec3D getNormal() {
        return Vec3D.div(normal, normalizer).normalize();
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
