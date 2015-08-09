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
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javafx.collections.ObservableFloatArray;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

/**
 *
 * @author Administrator
 */
public class QuadSquare {
    private static final float DETAIL_THRESHOLD = 200;
    private SharedData data; 
    private QuadSquare parent;
    private QuadSquare[] neighbors = new QuadSquare[4];
    private QuadSquare[] children = new QuadSquare[4];
    private Coordinate[] corners = new Coordinate[4];
    private Coordinate[] verts = new Coordinate[5];
    private float maxError;
    private Coordinate errorVert;
    private int index;
    private float size;
    private boolean subdivided;
    private boolean enabled;
     boolean isDirty;
    private int level;
    
    private Group meshGroup;
    private MeshView mesh;
    
    public QuadSquare(float initSize, Noise2D noise) {
        data = new SharedData(noise);
        size = initSize;
        level = 1;
        
        corners[0] = new Coordinate(0.5f * size, 0.5f * size);
        corners[1] = new Coordinate(-0.5f * size, 0.5f * size);
        corners[2] = new Coordinate(-0.5f * size, -0.5f * size);
        corners[3] = new Coordinate(0.5f * size, -0.5f * size);
        
        verts[0] = new Coordinate(0.5f * size, 0f);
        verts[1] = new Coordinate(0f, 0.5f * size);
        verts[2] = new Coordinate(-0.5f * size, 0f);
        verts[3] = new Coordinate(0f, -0.5f * size);
        verts[4] = new Coordinate(0f, 0f);
        
        for (int i = 0; i < corners.length; i++) {
            data.addVertex(corners[i], null, null);
        }
        
        float error;
        for (int i = 0; i < verts.length; i++) {
            if (i == 4) {
                error = data.addVertex(verts[i], null, null);
            } else {
                error = data.addVertex(verts[i], corners[i], corners[(i+3) % 4]);
            }
            if (error > maxError) {
                maxError = error;
                errorVert = verts[i];
            }
        }
        subdivide();
        enabled = true;
        isDirty = true;
        meshGroup = new Group();
    }
    
    public QuadSquare(QuadSquare parent, int index) {
        data = parent.data;
        this.parent = parent;
        this.index = index;
        level = parent.level + 1;
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

        verts[0] = new Coordinate(corners[1].getX() + size, corners[1].getY() - 0.5f * size);
        verts[1] = new Coordinate(corners[1].getX() + 0.5f * size, corners[1].getY());
        verts[2] = new Coordinate(corners[1].getX(), corners[1].getY() - 0.5f * size);
        verts[3] = new Coordinate(corners[1].getX() + 0.5f * size, corners[1].getY() - size);
        verts[4] = new Coordinate(corners[1].getX() + 0.5f * size, corners[1].getY() - 0.5f * size);
        
        float error;
        for (int i = 0; i < verts.length; i++) {
            if (i == 4) {
                error = data.addVertex(verts[i], corners[index], corners[(index + 2) % 4]);
            } else {
                error = data.addVertex(verts[i], corners[i], corners[(i+3) % 4]);
            }
            if (error > maxError) {
                errorVert = verts[i];
                maxError = error;
            }
        }

        meshGroup = new Group();
    }
    
    public Group getMeshGroup() {
        return meshGroup;
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
    
    public void merge() {
        markDirty();
        for (int i = 0; i < children.length; i++) {
            if (children[i] != null) {
                for (int j = 0; j < neighbors.length; j++) {
                    if (children[i].neighbors[j] != null) {
                        children[i].neighbors[j].neighbors[(j + 2) % 4] = null;
                        children[i].neighbors[j] = null;
                    }
                }
                children[i] = null;
            } else {
                int localX = ((i % 3) == 0) ? 1 : -1;
                int localY = ((i & 2) == 0) ? 1 : -1;
                Coordinate center = new Coordinate(verts[4].getX() + localX * 0.25f * size, verts[4].getY() + localY * 0.25f * size);
                data.removeChild(center);
            }
        }
        subdivided = false;
    }
    
    public boolean enableChild(int childIndex) {
        if (!children[childIndex].subdivided) {
            children[childIndex].subdivide();
        }
        
        if (children[childIndex].enabled) {
            children[childIndex].markDirty();
            return true;
        } else if (enableVertexNeighbor(childIndex) && enableVertexNeighbor((childIndex + 1) % 4)) {
            children[childIndex].markDirty();
            data.setEnabled(verts[childIndex], true);
            data.setEnabled(verts[(childIndex + 1) % 4], true);
            data.setEnabled(children[childIndex].verts[4], true);
            data.incDependencyCount(verts[childIndex]);
            data.incDependencyCount(verts[(childIndex + 1) % 4]);
            children[childIndex].enabled = true;
            return true;
        } else {
            return false;
        }
    }
    
    public void notifyVertexDisable(int vertIndex) {
        if (neighbors[vertIndex] != null) {
            neighbors[vertIndex].markDirty();
        }
    }
    
    public boolean enableVertexNeighbor(int vertIndex) {
        if (data.isEnabled(verts[vertIndex])) {
            return true;
        } else if (neighbors[vertIndex] != null) {
            QuadSquare neighbor = neighbors[vertIndex];
            if (neighbor.enabled) {
                neighbor.markDirty();
                return true;
            } else {
                return neighbor.parent.enableChild(neighbor.index);
            }
        } else if (parent != null) {
            int childIndex = (index ^ 1) ^ ((vertIndex & 1) << 1);
            QuadSquare neighborParent;
            if (((vertIndex - index) & 2) != 0) {
                neighborParent = parent;
            } else if (parent.neighbors[vertIndex] != null) {
                neighborParent = parent.neighbors[vertIndex];
            } else {
                return true;
            }
            QuadSquare child = neighborParent.children[childIndex];
            if (child == null) {
                int localX = ((childIndex % 3) == 0) ? 1 : -1;
                int localY = ((childIndex & 2) == 0) ? 1 : -1;
                Coordinate center = new Coordinate(
                        neighborParent.verts[4].getX() +
                                localX * 0.25f * neighborParent.size,
                        neighborParent.verts[4].getY() +
                                localY * 0.25f * neighborParent.size);
                if (data.connectChild(center)) {
                    child = neighborParent.children[childIndex];
                } else {
                    return false;
                }
            }
            neighbors[vertIndex] = child;
            child.neighbors[(vertIndex + 2) % 4] = this;
            return neighborParent.enableChild(childIndex);
        } else {
            return true;
        }
    }
    
    public void markDirty() {
        isDirty = true;
        QuadSquare current = this;
        while (current.parent != null && !current.parent.isDirty) {
            current = current.parent;
            current.isDirty = true;
        }
    }
    
    public void update(float x, float y, float z, float vx, float vy, float vz) {
        float dist;
        boolean hasEnabled = false;
        for (int i = 0; i < verts.length - 1; i++) {
            if (!data.isEnabled(verts[i])) {
                dist = distance(x, y, z, verts[i]);
                float toX = (verts[i].getX() - x) / dist;
                float toY = (verts[i].getY() - y) / dist;
                float toZ = (data.getHeight(verts[i]) - z) / dist;
                float dot = Math.max(0, toX * vx + toY * vy + toZ * vz);
                if (dot * data.getError(verts[i]) * DETAIL_THRESHOLD > dist) {
                    if (enableVertexNeighbor(i)) {
                        markDirty();
                        data.setEnabled(verts[i], true);
                    }
                    hasEnabled = true;
                }
            } else if (data.getDependencyCount(verts[i]) == 0) {
                dist = distance(x, y, z, verts[i]);
                float toX = (verts[i].getX() - x) / dist;
                float toY = (verts[i].getY() - y) / dist;
                float toZ = (data.getHeight(verts[i]) - z) / dist;
                float dot = toX * vx + toY * vy + toZ * vz;
                if (dot * data.getError(verts[i]) * DETAIL_THRESHOLD <= dist) {
                    notifyVertexDisable(i);
                    markDirty();
                    data.setEnabled(verts[i], false);
                }
            }
        }
        
        if (level > 1 && !hasEnabled && (!data.isEnabled(verts[0]) && !data.isEnabled(verts[1]) && !data.isEnabled(verts[2]) && !data.isEnabled(verts[3]))) {
            dist = distance(x, y, z, verts[4]);
            float toX = (verts[4].getX() - x) / dist;
            float toY = (verts[4].getY() - y) / dist;
            float toZ = (data.getHeight(verts[4]) - z) / dist;
            float dot = Math.max(0, toX * vx + toY * vy + toZ * vz);
            if (dot * data.getError(verts[4]) * DETAIL_THRESHOLD <= dist) {
                merge();
                enabled = false;
                data.decDependencyCount(corners[(index + 1) % 4]);
                data.decDependencyCount(corners[(index + 3) % 4]);
                data.setEnabled(verts[4], false);
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
                    float toX = (children[i].errorVert.getX() - x) / dist;
                    float toY = (children[i].errorVert.getY() - y) / dist;
                    float toZ = (data.getHeight(children[i].errorVert) - z) / dist;
                    float dot = Math.max(0, toX * vx + toY * vy + toZ * vz);
                    if (dot * children[i].maxError * DETAIL_THRESHOLD > dist) {
                        if (enableChild(i)) {
                            children[i].update(x, y, z, vx, vy, vz);
                        }
                    }
                } else {
                    children[i].update(x, y, z, vx, vy, vz);
                }
            }
        }
    }
    
    public void render() {
        if (mesh == null) {
            mesh = new MeshView(new TriangleMesh());
            //mesh.setDrawMode(DrawMode.LINE);
            meshGroup.getChildren().add(mesh);
            if (parent != null) {
                parent.meshGroup.getChildren().add(meshGroup);
            }
            if (!enabled) {
                isDirty = false;
                return;
            }
        }
        
        if (enabled) {
            boolean connectPoints = false;
            TriangleMesh squareMesh = (TriangleMesh) mesh.getMesh();
            squareMesh.getPoints().clear();
            squareMesh.getTexCoords().clear();
            squareMesh.getFaces().clear();
            squareMesh.getFaceSmoothingGroups().clear();
            squareMesh.getTexCoords().addAll(0f, 0f);
            ObservableFloatArray points = squareMesh.getPoints();
            points.addAll(verts[4].getX(), verts[4].getY(), data.getHeight(verts[4]));
            
            for (int i = 0; i < 4; i++) {
                if (children[i] != null && children[i].enabled) {
                    if (children[i].isDirty) {
                        children[i].render();
                    }
                    connectPoints = false;
                    if (children[(i + 1) % 4] == null || (children[(i + 1) % 4] != null && !children[(i + 1) % 4].enabled)) {
                        points.addAll(verts[(i + 1) % 4].getX(), verts[(i + 1) % 4].getY(), data.getHeight(verts[(i + 1) % 4]));
                        connectPoints = true;
                    }
                } else {
                    if (children[i] != null && children[i].isDirty) {
                        children[i].render();
                    }
                    points.addAll(corners[i].getX(), corners[i].getY(), data.getHeight(corners[i]));
                    if (!connectPoints) {
                        connectPoints = true;
                    } else {
                        int last = (points.size() / 3) - 1;
                        squareMesh.getFaces().addAll(0, 0, last - 1, 0, last, 0);
                        squareMesh.getFaceSmoothingGroups().addAll(1 << squareMesh.getFaces().size());
                    }
                    if (data.isEnabled(verts[(i + 1) % 4])) {
                        points.addAll(verts[(i + 1) % 4].getX(), verts[(i + 1) % 4].getY(), data.getHeight(verts[(i + 1) % 4]));
                        int last = (points.size() / 3) - 1;
                        squareMesh.getFaces().addAll(0, 0, last - 1, 0, last, 0);
                        squareMesh.getFaceSmoothingGroups().addAll(1 << squareMesh.getFaces().size());
                    }
                }
            }
            if (children[0] == null || !children[0].enabled) {
                squareMesh.getFaces().addAll(0, 0, (points.size() / 3) - 1, 0, 1, 0);
                squareMesh.getFaceSmoothingGroups().addAll(1 << squareMesh.getFaces().size());
            }
            
            mesh.setMesh(squareMesh);
            if (points.size() <= 3) {
                points.clear();
            }
        } else {
            TriangleMesh squareMesh = (TriangleMesh) mesh.getMesh();
            squareMesh.getPoints().clear();
            squareMesh.getTexCoords().clear();
            squareMesh.getFaces().clear();
            squareMesh.getFaceSmoothingGroups().clear();
            meshGroup.getChildren().remove(1, meshGroup.getChildren().size());
        }
        isDirty = false;
    }
    
    public float distance(float x1, float y1, float z1, Coordinate c) {
        float xDiff = c.getX() - x1;
        float yDiff = c.getY() - y1;
        float zDiff = data.getHeight(c) - z1;
        return (float)Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff); 
    }
    
    public String toString() {
        if (parent != null) {
            return parent.toString() + "|" + index + level;
        } else {
            return "" + index + level;
        }
    }
    
    private class SharedData {
        Noise2D noise;
        ExecutorService quadSquareLoader = Executors.newFixedThreadPool(16, new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable runnable) {
                Thread thread = Executors.defaultThreadFactory()
                        .newThread(runnable);
                thread.setDaemon(true);
                return thread;
            }
        });
        ConcurrentHashMap<Coordinate, VertexData> vertices = new ConcurrentHashMap<>();
        HashMap<Coordinate, Future<QuadSquare>> quadSquareBuffer = new HashMap<>();
        
        public SharedData(Noise2D noise) {
            this.noise = noise;
            vertices = new ConcurrentHashMap<>();
        }
        
        void loadChild(Coordinate center, QuadSquare parent, int index) {
            Callable<QuadSquare> callable = () -> new QuadSquare(parent, index);
            quadSquareBuffer.put(center, quadSquareLoader.submit(callable));
        }
        
        void removeChild(Coordinate center) {
            Future<QuadSquare> futureChild = quadSquareBuffer.get(center);
            if (futureChild != null) {
                futureChild.cancel(true);
                quadSquareBuffer.remove(center);
            }
        }
        
        boolean isChildLoading(Coordinate center) {
            return quadSquareBuffer.containsKey(center);
        }
        
        boolean connectChild(Coordinate center) {
            Future<QuadSquare> futureChild = quadSquareBuffer.get(center);
            if (futureChild != null && futureChild.isDone()) {
                try {
                    QuadSquare child = futureChild.get();
                        if (child.parent != null) {
                        child.parent.children[child.index] = child;
                        quadSquareBuffer.remove(center);
                        return true;
                    } else {
                        return false;
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    return false;
                }
            } else {
                return false;
            }
        }
           
        boolean containsVertex(Coordinate c) {
            return vertices.containsKey(c);
        }
        
        float addVertex(Coordinate c, Coordinate firstCorner, Coordinate secondCorner) {
            VertexData vert = vertices.computeIfAbsent(c, key -> {
                float height = (float)noise.getValue(key.getX(), key.getY());
                float error;
                if (firstCorner != null && secondCorner != null) {
                    error = Math.abs(height - (getHeight(firstCorner) + getHeight(secondCorner)) * 0.5f);
                } else {
                    error = 0;
                }
                return new VertexData(height, error);
            });
            return vert.getError();
        }
        
        void removeVertex(Coordinate c) {
            vertices.remove(c);
        }
        
        float getHeight(Coordinate c) {
            if (vertices.get(c) != null) {
                return vertices.get(c).getHeight();
            } else {
                return 0f;
            }
        }
        
        float getError(Coordinate c) {
            if (vertices.get(c) != null) {
                return vertices.get(c).getError();
            } else {
                return 0f;
            }
        }
        
        boolean isEnabled(Coordinate c) {
            if (vertices.get(c) != null) {
                return vertices.get(c).isEnabled();
            } else {
                return false;
            }
        }
        
        void setEnabled(Coordinate c, boolean enabled) {
            if (vertices.get(c) != null) {
                vertices.get(c).setEnabled(enabled);
            }
        }
        
        void incDependencyCount(Coordinate c) {
            vertices.get(c).incDependencyCount();
        }
        
        void decDependencyCount(Coordinate c) {
            vertices.get(c).decDependencyCount();
        }
        
        int getDependencyCount(Coordinate c) {
            return vertices.get(c).getDependencyCount();
        }
    }
}