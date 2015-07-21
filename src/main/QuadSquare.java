/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.ArrayList;
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
import javafx.scene.shape.TriangleMesh;

/**
 *
 * @author Administrator
 */
public class QuadSquare {
    private static final float DETAIL_THRESHOLD = 1;
    private SharedData data; 
    private QuadSquare parent;
    private QuadSquare[] children;
    private Coordinate[] corners = new Coordinate[4];
    private Coordinate[] verts = new Coordinate[5];
    private float maxError;
    private Coordinate errorVert;
    private int index;
    private float size;
    private boolean enabled;
    
    public QuadSquare(QuadSquare parent, int index) {
        data = parent.data;
        this.parent = parent;
        this.index = index;
        size = 0.5f * parent.size;
        
        for (int i = 0; i < corners.length; i++) {
            if (i == index) {
                corners[i] = parent.corners[i];
            } else if (((i + 1) % 4) == index) {
                corners[i] = parent.verts[index];
            } else if (((i + 2) % 4) == index) {
                corners[i] = parent.verts[4];
            } else {
                corners[i] = parent.verts[(index + 1) % 4];
            }
        }

        verts[0] = new Coordinate(corners[1].getX() + size, corners[1].getY() + 0.5f * size);
        verts[1] = new Coordinate(corners[1].getX() + 0.5f * size, corners[1].getY());
        verts[2] = new Coordinate(corners[1].getX(), corners[1].getY() + 0.5f * size);
        verts[3] = new Coordinate(corners[1].getX() + 0.5f * size, corners[1].getY() + size);
        verts[4] = new Coordinate(corners[1].getX() + 0.5f * size, corners[1].getY() + 0.5f * size);
        
        float error;
        for (int i = 0; i < verts.length; i++) {
            error = data.addVertex(verts[i]);
            if (error > maxError) {
                errorVert = verts[i];
            }
        }   
    }
    
    public boolean subdivide(int childIndex) {
        for (int i = 0; i < children.length; i++) {
            int localX = ((i % 3) == 0) ? 1 : -1;
            int localY = ((i & 2) == 0) ? 1 : -1;
            Coordinate center = new Coordinate(corners[1].getX() + localX * 0.25f * size, corners[1].getY() + localY * 0.25f * size);
            if (!data.isLoading(center)) {
                data.load(center, this, i);
            }
        }
        
        if (data.isEnabled(children[childIndex].verts[0])) {
            return true;
        } else if (subdivideNeighbor(childIndex) && subdivideNeighbor((childIndex + 1) % 3)) {
            data.setEnabled(verts[childIndex], true);
            data.setEnabled(verts[(childIndex + 1) % 3], true);
            data.setEnabled(children[childIndex].verts[0], true);
            return true;
        } else {
            return false;
        }
    }
    
    public boolean subdivideNeighbor(int vertIndex) {
        if (data.isEnabled(verts[vertIndex])) {
            return true;
        }
        
        float xShift = (vertIndex == 0) ? 0.5f * size : ((vertIndex == 2) ? -0.5f * size : 0f);
        float yShift = (vertIndex == 1) ? 0.5f * size : ((vertIndex == 3) ? -0.5f * size : 0f);
        Coordinate neighbor = new Coordinate(verts[vertIndex].getX() + xShift, verts[vertIndex].getY() + yShift);
        if (data.isEnabled(neighbor)) {
            return true;
        }
        
        int childIndex;
        QuadSquare current = this;
        LinkedList<Integer> opposites = new LinkedList<>();
        do {
            childIndex = current.index;
            opposites.push((childIndex ^ 1) ^ ((vertIndex & 1) << 1));
            current = current.parent;
        } while (((vertIndex - childIndex) & 2) == 0);
        
        QuadSquare child;
        while (!opposites.isEmpty()) {
            childIndex = opposites.pop();
            child = current.children[childIndex];
            if (opposites.size() == 1) {
                if (child == null) {
                    int localX = ((childIndex % 3) == 0) ? 1 : -1;
                    int localY = ((childIndex & 2) == 0) ? 1 : -1;
                    Coordinate center = new Coordinate(corners[1].getX() + localX * 0.25f * size, corners[1].getY() + localY * 0.25f * size);
                    if (data.connectChild(center)) {
                        if (current.subdivide(childIndex)) {
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    if (current.subdivide(childIndex)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            } else {
                current = child;
            }
        }
        throw new IllegalStateException("That is weird");
    }
    
    public void update(float x, float y, float z) {
        for (int i = 0; i < verts.length - 1; i++) {
            if (data.getError(verts[i]) * DETAIL_THRESHOLD > distance(x, y, z, verts[i].getX(), verts[i].getY(), data.getHeight(verts[i]))) {
                if (subdivideNeighbor(i)) {
                    data.setEnabled(verts[i], true);
                }
            }
        }
        
        for (int i = 0; i < children.length; i++) {
            if (children[i].maxError * DETAIL_THRESHOLD > distance(x, y, z, children[i].errorVert.getX(), children[i].errorVert.getY(), data.getHeight(children[i].errorVert))) {
                if (subdivide(i)) {
                    children[i].update(x, y, z);
                }
            }
        }
    }
    
    public void render(Group buffer) {
        ArrayList<Integer> cornersToRender = new ArrayList<>();
        for (int i = 0; i < children.length; i++) {
            int localX = ((i % 3) == 0) ? 1 : -1;
            int localY = ((i & 2) == 0) ? 1 : -1;
            Coordinate center = new Coordinate(corners[1].getX() + localX * 0.25f * size, corners[1].getY() + localY * 0.25f * size);
            if (data.isEnabled(center)) {
                children[i].render(buffer);
            } else {
                cornersToRender.add(i);
            }
        }
        for (int i = 0; i < cornersToRender.size(); i++) {
            int next = (i+1) % 3;
            int prev = (i-1) % 3;
            if (cornersToRender.get(i) == cornersToRender.get(prev) + 1) {
                
            }
        }
    }
    
    public float distance(float x1, float y1, float z1, float x2, float y2, float z2) {
        float xDiff = x2 - x1;
        float yDiff = y2 - y1;
        float zDiff = z2 - z1;
        return (float)Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff); 
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
        
        void load(Coordinate center, QuadSquare parent, int index) {
            Task<QuadSquare> task = new Task<QuadSquare>(){
                @Override
                protected QuadSquare call() throws Exception {
                    return new QuadSquare(parent, index);
                }
            };
            quadSquareBuffer.put(center, task);
        }
        
        boolean isLoading(Coordinate center) {
            return quadSquareBuffer.containsKey(center);
        }
        
        boolean connectChild(Coordinate center) {
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
            VertexData vert = vertices.computeIfAbsent(c, key -> {
                float height = (float)noise.getValue(key.getX(), key.getY());
                float error = 0;
                if (key.equals(verts[4])) {
                    if ((index & 1) == 1) {
                        error = Math.abs(height - (getHeight(corners[1]) + getHeight(corners[3])) * 0.5f);
                    } else {
                        error = Math.abs(height - (getHeight(corners[1]) + getHeight(corners[3])) * 0.5f);
                    }
                } else {
                    for (int i = 0; i < 4; i++) {
                        if (key.equals(verts[i])) {
                            error = Math.abs(height - (getHeight(corners[(i-1)%4]) + getHeight(corners[i]))*0.5f);
                            break;
                        }
                    }
                }
                return new VertexData(height, error);
            });
            if (vert != null) {
                return vert.getError();
            } else {
                return getError(c);
            }
        }
        
        float getHeight(Coordinate c) {
            return vertices.get(c).getHeight();
        }
        
        float getError(Coordinate c) {
            return vertices.get(c).getError();
        }
        
        boolean isEnabled(Coordinate c) {
            return vertices.get(c).isEnabled();
        }
        
        void setEnabled(Coordinate c, boolean enabled) {
            vertices.get(c).setEnabled(enabled);
        }
    }
}
