/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javafx.scene.Group;

/**
 *
 * @author Administrator
 */
public class QuadSquare {
    private SharedData data; 
    private QuadSquare parent;
    private QuadSquare[] children;
    private Coordinate[] corners = new Coordinate[4];
    private Coordinate[] verts = new Coordinate[5];
    private Coordinate maxError;
    private int index;
    private float size;
    private boolean enabled;
    
    public QuadSquare(QuadSquare parent, int index) {
        data = parent.data;
        this.parent = parent;
        this.index = index;
        size = 0.5f * parent.size;
        switch (index) {
            case 0:
                corners[0] = parent.corners[0];
                corners[1] = parent.verts[2];
                corners[2] = parent.verts[0];
                corners[3] = parent.verts[1];
                break;
            case 1:
                corners[0] = parent.verts[2];
                corners[1] = parent.corners[1];
                corners[2] = parent.verts[3];
                corners[3] = parent.verts[0];
                break;
            case 2:
                corners[0] = parent.verts[0];
                corners[1] = parent.verts[3];
                corners[2] = parent.corners[2];
                corners[3] = parent.verts[4];
                break;
            case 3:
                corners[0] = parent.verts[1];
                corners[1] = parent.verts[0];
                corners[2] = parent.verts[4];
                corners[3] = parent.corners[3];
                break;
        }
        
        verts[0] = new Coordinate(corners[1].getX() + 0.5f * size, corners[1].getY() + 0.5f * size);
        verts[1] = new Coordinate(corners[1].getX() + size, corners[1].getY() + 0.5f * size);
        verts[2] = new Coordinate(corners[1].getX() + 0.5f * size, corners[1].getY());
        verts[3] = new Coordinate(corners[1].getX(), corners[1].getY() + 0.5f * size);
        verts[4] = new Coordinate(corners[1].getX() + 0.5f * size, corners[1].getY() + size);
        
        float error;
        data.addVertex(verts[0]);
        if ((index & 1) == 1) {
            error = Math.abs(data.getHeight(verts[0]) - (data.getHeight(corners[1]) + data.getHeight(corners[3])) * 0.5f);
            data.setError(verts[0], error);
        } else {
            error = Math.abs(data.getHeight(verts[0]) - (data.getHeight(corners[1]) + data.getHeight(corners[3])) * 0.5f);
            data.setError(verts[0], error);
        }
        maxError = verts[0];
        float maxErrorValue = data.getError(maxError);
        for (int i = 1; i < verts.length; i++) {
            if (!data.containsVertex(verts[i])) {
                data.addVertex(verts[i]);
                error = Math.abs(data.getHeight(verts[i]) - (data.getHeight(corners[(i+6)%4]) + data.getHeight(corners[i-1]))*0.5f);
                data.setError(verts[i], error);
                if (error > maxErrorValue) {
                    maxError = verts[i];
                    maxErrorValue = data.getError(maxError);
                }
            } else {
                if (data.getError(verts[i]) > maxErrorValue) {
                    maxError = verts[i];
                    maxErrorValue = data.getError(maxError);
                }
            }
        }   
    }
    
    private class SharedData {
        Noise2D noise;
        ExecutorService quadSquareLoader;
        HashMap<Coordinate, VertexData> vertices;
        
        public SharedData(Noise2D noise) {
            Random r = new Random();
            noise = new Noise2D(r.nextLong());
            quadSquareLoader = Executors.newSingleThreadExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(final Runnable runnable) {
                    Thread thread = Executors.defaultThreadFactory()
                            .newThread(runnable);
                    thread.setDaemon(true);
                    return thread;
                }
            });
            vertices = new HashMap<>();
        }
        
        boolean containsVertex(Coordinate c) {
            return vertices.containsKey(c);
        }
        
        void addVertex(Coordinate c) {
            vertices.put(c, new VertexData((float)noise.getValue(c.getX(), c.getY())));
        }
        
        float getHeight(Coordinate c) {
            return vertices.get(c).getHeight();
        }
        
        float getError(Coordinate c) {
            return vertices.get(c).getError();
        }
        
        void setError(Coordinate c, float error) {
            vertices.get(c).setError(error);
        }
        
        float isEnabled(Coordinate c) {
            return vertices.get(c).getError();
        }
        
        void setEnabled(Coordinate c, boolean enabled) {
            vertices.get(c).setEnabled(enabled);
        }
    }
}
