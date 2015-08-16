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
public class Vec3D {
    private float x;
    private float y;
    private float z;
    
    public Vec3D(float pX, float pY, float pZ) {
        x = pX;
        y = pY;
        z = pZ;
    }
    
    public Vec3D add(Vec3D vec) {
        x += vec.x;
        y += vec.y;
        z += vec.z;
        return this;
    }
    
    public Vec3D add(float num) {
        x += num;
        y += num;
        z += num;
        return this;
    }
    
    public static Vec3D add(Vec3D vec1, Vec3D vec2) {
        return new Vec3D(vec1.x + vec2.x, vec1.y + vec2.y, vec1.z + vec2.z);
    }
    
    public static Vec3D add(Vec3D vec, float num) {
        return new Vec3D(vec.x + num, vec.y + num, vec.z + num);
    }
    
    public Vec3D sub(Vec3D vec) {
        x -= vec.x;
        y -= vec.y;
        z -= vec.z;
        return this;
    }
    
    public Vec3D sub(float num) {
        x -= num;
        y -= num;
        z -= num;
        return this;
    }
    
    public static Vec3D sub(Vec3D vec1, Vec3D vec2) {
        return new Vec3D(vec1.x - vec2.x, vec1.y - vec2.y, vec1.z - vec2.z);
    }
    
    public static Vec3D sub(Vec3D vec, float num) {
        return new Vec3D(vec.x - num, vec.y - num, vec.z - num);
    }
    
    public Vec3D mult(Vec3D vec) {
        x *= vec.x;
        y *= vec.y;
        z *= vec.z;
        return this;
    }
    
    public Vec3D mult(float num) {
        x *= num;
        y *= num;
        z *= num;
        return this;
    }
    
    public static Vec3D mult(Vec3D vec1, Vec3D vec2) {
        return new Vec3D(vec1.x * vec2.x, vec1.y * vec2.y, vec1.z * vec2.z);
    }
    
    public static Vec3D mult(Vec3D vec, float num) {
        return new Vec3D(vec.x * num, vec.y * num, vec.z * num);
    }
    
    public Vec3D div(Vec3D vec) {
        x /= vec.x;
        y /= vec.y;
        z /= vec.z;
        return this;
    }
    
    public Vec3D div(float num) {
        x /= num;
        y /= num;
        z /= num;
        return this;
    }
    
    public static Vec3D div(Vec3D vec1, Vec3D vec2) {
        return new Vec3D(vec1.x / vec2.x, vec1.y / vec2.y, vec1.z / vec2.z);
    }
    
    public static Vec3D div(Vec3D vec, float num) {
        return new Vec3D(vec.x / num, vec.y / num, vec.z / num);
    }
    
    public Vec3D normalize() {
        float mag = magnitude();
        x /=  mag;
        y /= mag;
        z /= mag;
        return this;
    }
    
    public static Vec3D normalize(Vec3D vec) {
        float mag = vec.magnitude();
        return new Vec3D(vec.x / mag, vec.y / mag, vec.z / mag);
    }
    
    public float magnitude() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }
    
    public static float magnitude(Vec3D vec) {
        return (float) Math.sqrt(vec.x * vec.x + vec.y * vec.y + vec.z * vec.z);
    }
    
    public float dot(Vec3D vec) {
        return x * vec.x + y * vec.y + z * vec.z;
    }
    
    public static float dot(Vec3D vec1, Vec3D vec2) {
        return vec1.x * vec2.x + vec1.y * vec2.y + vec1.z * vec2.z;
    }
    
    public Vec3D cross(Vec3D vec) {
        return new Vec3D(y * vec.z - z * vec.y,
                            z * vec.x - x * vec.z,
                            x * vec.y - y * vec.x);
    }
    
    public static Vec3D cross(Vec3D vec1, Vec3D vec2) {
        return new Vec3D(vec1.y * vec2.z - vec1.z * vec2.y,
                            vec1.z * vec2.x - vec1.x * vec2.z,
                            vec1.x * vec2.y - vec1.y * vec2.x);
    }
    
    public Vec3D lerp(Vec3D vec, float t) {
        x = t * x + (1 - t) * vec.x;
        y = t * y + (1 - t) * vec.y;
        z = t * z + (1 - t) * vec.z;
        return this;
    }
    
    public static Vec3D lerp(Vec3D vec1, Vec3D vec2, float t) {
        return new Vec3D(t * vec1.x + (1 - t) * vec2.x,
                            t * vec1.y + (1 - t) * vec2.y,
                            t * vec1.z + (1 - t) * vec2.z);
    }
}
