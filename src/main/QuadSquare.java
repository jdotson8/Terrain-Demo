/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
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
                corners[1] = parent.verts[1];
                corners[2] = parent.verts[4];
                corners[3] = parent.verts[0];
                break;
            case 1:
                corners[0] = parent.verts[1];
                corners[1] = parent.corners[1];
                corners[2] = parent.verts[2];
                corners[3] = parent.verts[4];
                break;
            case 2:
                corners[0] = parent.verts[4];
                corners[1] = parent.verts[2];
                corners[2] = parent.corners[2];
                corners[3] = parent.verts[3];
                break;
            case 3:
                corners[0] = parent.verts[0];
                corners[1] = parent.verts[4];
                corners[2] = parent.verts[3];
                corners[3] = parent.corners[3];
                break;
        }

        verts[0] = new Coordinate(corners[1].getX() + size, corners[1].getY() + 0.5f * size);
        verts[1] = new Coordinate(corners[1].getX() + 0.5f * size, corners[1].getY());
        verts[2] = new Coordinate(corners[1].getX(), corners[1].getY() + 0.5f * size);
        verts[3] = new Coordinate(corners[1].getX() + 0.5f * size, corners[1].getY() + size);
        verts[4] = new Coordinate(corners[1].getX() + 0.5f * size, corners[1].getY() + 0.5f * size);
        
        float error;
        data.addVertex(verts[4]);
        if ((index & 1) == 1) {
            error = Math.abs(data.getHeight(verts[4]) - (data.getHeight(corners[1]) + data.getHeight(corners[3])) * 0.5f);
            data.setError(verts[4], error);
        } else {
            error = Math.abs(data.getHeight(verts[4]) - (data.getHeight(corners[1]) + data.getHeight(corners[3])) * 0.5f);
            data.setError(verts[4], error);
        }
        maxError = verts[4];
        float maxErrorValue = data.getError(maxError);
        for (int i = 0; i < verts.length - 1; i++) {
            if (!data.containsVertex(verts[i])) {
                data.addVertex(verts[i]);
                error = Math.abs(data.getHeight(verts[i]) - (data.getHeight(corners[(i-1)%4]) + data.getHeight(corners[i]))*0.5f);
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
    
    public boolean subdivide(int childIndex) {
        data.setEnabled(children[childIndex].verts[0], true);
        if (enableEdgeVertex(childIndex) && enableEdgeVertex((childIndex + 1) % 3)) {
            
        }
        return true;
    }
    
    public boolean enableEdgeVertex(int vertIndex) {
        int childIndex;
        QuadSquare current = this;
        LinkedList<Integer> opposites = new LinkedList<>();
        do {
            childIndex = current.index;
            opposites.push((childIndex ^ 1) ^ ((vertIndex & 1) << 1));
            current = current.parent;
        } while (((vertIndex - childIndex) & 2) == 0);
        
        /*QuadSquare child;
        while (!opposites.isEmpty()) {
            child = current.children[opposites.pop()];
            if (child == null && ) {
                current = child;
            } else {
                data.create
            }
        }*/ return true;
    }
    
    private class SharedData {
        Noise2D noise;
        ExecutorService quadSquareLoader = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable runnable) {
                Thread thread = Executors.defaultThreadFactory()
                        .newThread(runnable);
                thread.setDaemon(true);
                return thread;
            }
        });
        ConcurrentHashMap<Coordinate, VertexData> vertices = new ConcurrentHashMap<>();
        HashMap<Coordinate, Task<QuadSquare>> quadSquareBuffer = new HashMap<>();
        
        public SharedData(Noise2D noise) {
            Random r = new Random();
            noise = new Noise2D(r.nextLong());
            vertices = new ConcurrentHashMap<>();
        }
        
        boolean childLoading(Coordinate center) {
            return quadSquareBuffer.containsKey(center);
        }
        
        boolean addLoadedChild(Coordinate center) {
            Task<QuadSquare> task = quadSquareBuffer.get(center);
            if (task != null && task.getState().equals(Worker.State.SUCCEEDED)) {
                QuadSquare child = task.getValue();
                if (child.parent != null) {
                    child.parent.children[child.index] = child;
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
           
        boolean containsVertex(Coordinate c) {
            return vertices.containsKey(c);
        }
        
        float addVertex(Coordinate c) {
            VertexData vert = vertices.computeIfAbsent(c, key -> new VertexData((float)noise.getValue(key.getX(), key.getY())));
            if (vert == null) {
                return Float.NaN;
            } else {
                return vert.getError();
            }
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
