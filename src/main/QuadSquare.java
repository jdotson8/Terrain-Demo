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
import javafx.collections.ObservableFloatArray;
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
    private boolean subdivided;
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
    
    public void subdivide() {
        for (int i = 0; i < children.length; i++) {
            int localX = ((i % 3) == 0) ? 1 : -1;
            int localY = ((i & 2) == 0) ? 1 : -1;
            Coordinate center = new Coordinate(verts[4].getX() + localX * 0.25f * size, verts[4].getY() + localY * 0.25f * size);
            data.loadChild(center, this, i);
        }
        subdivided = true;
    }
    
    public boolean enableChild(int childIndex) {
        if (!children[childIndex].subdivided) {
            children[childIndex].subdivide();
        }
        
        if (data.isEnabled(children[childIndex].verts[0])) {
            enabled = true;
            return true;
        } else if (enableVertexNeighbor(childIndex) && enableVertexNeighbor((childIndex + 1) % 3)) {
            data.setEnabled(verts[childIndex], true);
            data.setEnabled(verts[(childIndex + 1) % 3], true);
            data.setEnabled(children[childIndex].verts[0], true);
            children[childIndex].enabled = true;
            return true;
        } else {
            return false;
        }
    }
    
    public boolean enableVertexNeighbor(int vertIndex) {
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
                        if (current.enableChild(childIndex)) {
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    if (current.enableChild(childIndex)) {
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
        float dist;
        for (int i = 0; i < verts.length - 1; i++) {
            dist = distance(x, y, z, verts[i]);
            if (data.getError(verts[i]) * DETAIL_THRESHOLD > dist) {
                if (enableVertexNeighbor(i)) {
                    data.setEnabled(verts[i], true);
                }
            }
        }
        
        if (subdivided) {
            childLoop:
            for (int i = 0; i < children.length; i++) {
                if (children[i] == null) {
                    int localX = ((i % 3) == 0) ? 1 : -1;
                    int localY = ((i & 2) == 0) ? 1 : -1;
                    Coordinate center = new Coordinate(verts[4].getX() + localX * 0.25f * size, verts[4].getY() + localY * 0.25f * size);
                    if (!data.connectChild(center)) {
                        continue childLoop;
                    }
                }
                if (!children[i].enabled) {
                    dist = distance(x, y, z, children[i].errorVert);
                    if (children[i].maxError * DETAIL_THRESHOLD > dist) {
                        if (enableChild(i)) {
                            children[i].update(x, y, z);
                        }
                    }
                } else {
                    children[i].update(x, y, z);
                }
            }
        }
    }
    
    public void render(Group terrain) {
        boolean connectPoints = false;
        TriangleMesh squareMesh = new TriangleMesh();
        ObservableFloatArray points = squareMesh.getPoints();
        points.addAll(verts[4].getX(), verts[4].getY(), data.getHeight(verts[4]));
        for (int i = 0; i < 4; i++) {
            if (children[i].enabled) {
                children[i].render(terrain);
                connectPoints = false;
                if (children[(i + 1) % 4].enabled && data.isEnabled(verts[(i + 1) % 3])) {
                    points.addAll(verts[(i + 1) % 4].getX(), verts[(i + 1) % 4].getY(), data.getHeight(verts[(i + 1) % 4]));
                    int last = (points.size() / 3) - 1;
                    squareMesh.getFaces().addAll(0, 0, last - 1, 0, last, 0);
                    connectPoints = true;
                }
            } else {
                if (connectPoints) {
                    points.addAll(corners[i].getX(), corners[i].getY(), data.getHeight(corners[i]));
                    connectPoints = true;
                }
                if (data.isEnabled(verts[(i + 1) % 4])) {
                    points.addAll(verts[(i + 1) % 4].getX(), verts[(i + 1) % 4].getY(), data.getHeight(verts[(i + 1) % 4]));
                    int last = (points.size() / 3) - 1;
                    squareMesh.getFaces().addAll(0, 0, last - 1, 0, last, 0);
                }
            }
            if (data.isEnabled(verts[(i + 1) % 4])) {
                points.addAll(verts[(i + 1) % 4].getX(), verts[(i + 1) % 4].getY(), data.getHeight(verts[(i + 1) % 4]));
                int last = (points.size() / 3) - 1;
                squareMesh.getFaces().addAll(0, 0, last - 1, 0, last, 0);
            }
        }
    }
    
    public float distance(float x1, float y1, float z1, Coordinate c) {
        float xDiff = c.getX() - x1;
        float yDiff = c.getY() - y1;
        float zDiff = data.getHeight(c) - z1;
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
        
        void loadChild(Coordinate center, QuadSquare parent, int index) {
            Task<QuadSquare> task = new Task<QuadSquare>(){
                @Override
                protected QuadSquare call() throws Exception {
                    return new QuadSquare(parent, index);
                }
            };
            quadSquareBuffer.put(center, task);
        }
        
        boolean isChildLoading(Coordinate center) {
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
