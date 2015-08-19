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
                
    }
    
    public void removeNormal(Vec3D pNormal, float weight) {
        normal.sub(pNormal);
        normalizer -= weight;
    }
    
    public Vec3D getNormal() {
        return Vec3D.div(normal, normalizer).normalize();
    }
    
//    public float[] getNormal() {
//        float[] normalized = new float[3];
//        normalized[0] = normal[0] / normalizer;
//        normalized[1] = normal[1] / normalizer;
//        normalized[2] = normal[2] / normalizer;
//        float mag = (float) Math.sqrt(normalized[0] * normalized[0] + normalized[1] * normalized[1] + normalized[2] * normalized[2]);
//        System.out.println(mag);
//        normalized[0] /= mag;
//        normalized[1] /= mag;
//        normalized[2] /= mag;
//        mag = (float) Math.sqrt(normalized[0] * normalized[0] + normalized[1] * normalized[1] + normalized[2] * normalized[2]);
//        System.out.println(mag);
//        return normalized;
//    }
    
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
