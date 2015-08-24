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
import javafx.scene.shape.VertexFormat;

/**
 *
 * @author Administrator
 */
public class QuadSquare {
    private static final float DETAIL_THRESHOLD = 200;
    private static final QuadSquare NULL_NEIGHBOR = new QuadSquare();
    private SharedData data; 
    private QuadSquare parent;
    private Vec3D[] normals = new Vec3D[8];
    private float[] weights = new float[8];
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
    private boolean isDirty;
    private int level;
    
    private Group meshGroup;
    private MeshView mesh;
    
    private QuadSquare() {
        //Null neighbor;
    }
    
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
        
        for (int i = 0; i < normals.length; i += 2) {
            float avg = 0.5f * data.getHeight(corners[i / 2]) + 0.5f * data.getHeight(corners[(i / 2 + 1) % 4]);
            Vec3D v1 = new Vec3D(corners[i / 2].getX() - verts[4].getX(),
                                    corners[i / 2].getY() - verts[4].getY(),
                                    data.getHeight(corners[i / 2]) - data.getHeight(verts[4]));
            Vec3D v2 = new Vec3D(verts[(i / 2 + 1) % 4].getX() - verts[4].getX(),
                                    verts[(i / 2 + 1) % 4].getY() - verts[4].getY(),
                                    avg - data.getHeight(verts[4]));
            Vec3D v3 = new Vec3D(corners[(i / 2 + 1) % 4].getX() - verts[4].getX(),
                                    corners[(i / 2 + 1) % 4].getY() - verts[4].getY(),
                                    data.getHeight(corners[(i / 2 + 1) % 4]) - data.getHeight(verts[4]));
            normals[i] = Vec3D.cross(v1, v2).mult(0.5f);
            weights[i] = normals[i].magnitude();
            data.addNormal(normals[i], weights[i], corners[i / 2], verts[(i / 2 + 1) % 4], verts[4]);
            normals[i + 1] = Vec3D.cross(v2, v3).mult(0.5f);
            weights[i + 1] = normals[i + 1].magnitude();
            data.addNormal(normals[i + 1], weights[i + 1], corners[(i / 2 + 1) % 4], verts[(i / 2 + 1) % 4], verts[4]);
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
            if (!Thread.interrupted()) {
                if (i == 4) {
                    error = data.addVertex(verts[i], corners[index], corners[(index + 2) % 4]);
                } else {
                    error = data.addVertex(verts[i], corners[i], corners[(i+3) % 4]);
                }
                if (error > maxError) {
                    errorVert = verts[i];
                    maxError = error;
                }
            } else {
                for (int j = i - 1; j >= 0; j--) {
                    if (j == index) {
                        QuadSquare neighbor = parent.getNeighbor(j);
                        if (neighbor == NULL_NEIGHBOR || !neighbor.enabled) {
                            data.removeVertex(verts[j]);
                        }
                    } else if (j == (index + 1) % 4) {
                        QuadSquare neighbor = parent.getNeighbor(j);
                        if (neighbor == NULL_NEIGHBOR || !neighbor.enabled) {
                            data.removeVertex(verts[j]);
                        }
                    } else {
                        data.removeVertex(verts[j]);
                    }
                }
            }
        }
        
        for (int i = 0; i < normals.length; i += 2) {
            float avg = 0.5f * data.getHeight(corners[i / 2]) + 0.5f * data.getHeight(corners[(i / 2 + 1) % 4]);
            Vec3D v1 = new Vec3D(corners[i / 2].getX() - verts[4].getX(),
                                    corners[i / 2].getY() - verts[4].getY(),
                                    data.getHeight(corners[i / 2]) - data.getHeight(verts[4]));
            Vec3D v2 = new Vec3D(verts[(i / 2 + 1) % 4].getX() - verts[4].getX(),
                                    verts[(i / 2 + 1) % 4].getY() - verts[4].getY(),
                                    avg - data.getHeight(verts[4]));
            Vec3D v3 = new Vec3D(corners[(i / 2 + 1) % 4].getX() - verts[4].getX(),
                                    corners[(i / 2 + 1) % 4].getY() - verts[4].getY(),
                                    data.getHeight(corners[(i / 2 + 1) % 4]) - data.getHeight(verts[4]));
            normals[i] = Vec3D.cross(v1, v2).mult(0.5f);
            weights[i] = normals[i].magnitude();
            normals[i + 1] = Vec3D.cross(v2, v3).mult(0.5f);
            weights[i + 1] = normals[i + 1].magnitude();
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
                
                data.removeVertex(children[i].verts[4]);
                for (int j = 0; j < verts.length - 1; j++) {
                    if (j == i) {
                        QuadSquare neighbor = getNeighbor(j);
                        if (neighbor == NULL_NEIGHBOR || !neighbor.enabled) {
                            data.removeVertex(children[i].verts[j]);
                        } else {
                            data.removeNormal(children[i].normals[(2 * j + 6) % 8], children[i].weights[(2 * j + 6) % 8], children[i].verts[j]);
                            data.removeNormal(children[i].normals[(2 * j + 7) % 8], children[i].weights[(2 * j + 7) % 8], children[i].verts[j]);
                        }
                    } else if (j == (i + 1) % 4) {
                        QuadSquare neighbor = getNeighbor(j);
                        if (neighbor == NULL_NEIGHBOR || !neighbor.enabled) {
                            data.removeVertex(children[i].verts[j]);
                        } else {
                            data.removeNormal(children[i].normals[(2 * j + 6) % 8], children[i].weights[(2 * j + 6) % 8], children[i].verts[j]);
                            data.removeNormal(children[i].normals[(2 * j + 7) % 8], children[i].weights[(2 * j + 7) % 8], children[i].verts[j]);
                        }
                    } else {
                        data.removeVertex(children[i].verts[j]);
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
        if (children[childIndex].enabled) {
            children[childIndex].markDirty();
            return true;
        }
        
        if (!children[childIndex].subdivided) {
            children[childIndex].subdivide();
        }
        
        int vert1 = childIndex;
        int vert2 = (childIndex + 1) % 4;
        
        if (!data.isEnabled(verts[vert1]) && enableVertexNeighbor(vert1)) {
            recomputeNormal((2 * vert1 + 6) % 8);
            recomputeNormal((2 * vert1 + 7) % 8);
            markDirty();
        }
        
        if (!data.isEnabled(verts[vert2]) && enableVertexNeighbor(vert2)) {
            recomputeNormal((2 * vert2 + 6) % 8);
            recomputeNormal((2 * vert2 + 7) % 8);
            markDirty();
        }
        
        if (data.isEnabled(verts[vert1]) && data.isEnabled(verts[vert2])) {
            children[childIndex].enabled = true;
            recomputeChildNormals(childIndex);
            children[childIndex].markDirty();
            data.incDependencyCount(verts[childIndex]);
            data.incDependencyCount(verts[(childIndex + 1) % 4]);
            data.setEnabled(children[childIndex].verts[4], true);
            return true;
        } else {
            return false;
        }
    }
    
    public void disableChild(int childIndex) {
        children[childIndex].merge();
        children[childIndex].enabled = false;
        recomputeChildNormals(childIndex);
        data.decDependencyCount(verts[childIndex]);
        data.decDependencyCount(verts[(childIndex + 1) % 4]);
        data.setEnabled(children[childIndex].verts[4], false);
    }
    
    public void notifyVertexDisable(int vertIndex) {
        data.setEnabled(verts[vertIndex], false);
        if (neighbors[vertIndex] != NULL_NEIGHBOR) {
            neighbors[vertIndex].recomputeNormal((2 * vertIndex + 2) % 8);
            neighbors[vertIndex].recomputeNormal((2 * vertIndex + 3) % 8);
            neighbors[vertIndex].markDirty();
        }
    }
    
    public QuadSquare getNeighbor(int neighborIndex) {
        QuadSquare neighbor = neighbors[neighborIndex];
        if (neighbor != null) {
            return neighbor;
        } else if (parent != null) {
            int childIndex = (index ^ 1) ^ ((neighborIndex & 1) << 1);
            QuadSquare neighborParent;
            if (((neighborIndex - index) & 2) != 0) {
                neighborParent = parent;
            } else if (parent.neighbors[neighborIndex] != null &&
                        parent.neighbors[neighborIndex] != NULL_NEIGHBOR) {
                neighborParent = parent.neighbors[neighborIndex];
            } else {
                neighbors[neighborIndex] = NULL_NEIGHBOR;
                return NULL_NEIGHBOR;
            }
            neighbor = neighborParent.children[childIndex];
            if (neighbor == null) {
                int localX = ((childIndex % 3) == 0) ? 1 : -1;
                int localY = ((childIndex & 2) == 0) ? 1 : -1;
                Coordinate center = new Coordinate(
                        neighborParent.verts[4].getX() +
                                localX * 0.25f * neighborParent.size,
                        neighborParent.verts[4].getY() +
                                localY * 0.25f * neighborParent.size);
                if (data.connectChild(center)) {
                    neighbor = neighborParent.children[childIndex];
                } else {
                    return null;
                }
            }
            neighbors[neighborIndex] = neighbor;
            neighbor.neighbors[(neighborIndex + 2) % 4] = this;
            return neighbor;
        } else {
            neighbors[neighborIndex] = NULL_NEIGHBOR;
            return NULL_NEIGHBOR;
        }
    }
    
    public boolean enableVertexNeighbor(int vertIndex) {
        QuadSquare neighbor = getNeighbor(vertIndex);
        if (neighbor == null) {
            return false;
        } else if (neighbor == NULL_NEIGHBOR) {
            data.setEnabled(verts[vertIndex], true);
            return true;
        } else {
            if (neighbor.enabled) {
                data.setEnabled(verts[vertIndex], true);
                neighbor.recomputeNormal((2 * vertIndex + 2) % 8);
                neighbor.recomputeNormal((2 * vertIndex + 3) % 8);
                neighbor.markDirty();
                return true;
            } else if (neighbor.parent.enableChild(neighbor.index)) {
                data.setEnabled(verts[vertIndex], true);
                neighbor.recomputeNormal((2 * vertIndex + 2) % 8);
                neighbor.recomputeNormal((2 * vertIndex + 3) % 8);
                neighbor.markDirty();
                return true;
            } else {
                return false;
            }
        }
    }
    
    public void recomputeChildNormals(int childIndex) {
        int normIndex1 = (2 * childIndex) % 8;
        int normIndex2 = (2 * childIndex + 7) % 8;
        QuadSquare child = children[childIndex];
        if (child.enabled) {
            data.removeNormal(normals[normIndex1], weights[normIndex1],
                                verts[(childIndex + 1) % 4], corners[childIndex], verts[4]);
            data.removeNormal(normals[normIndex2], weights[normIndex2],
                                verts[childIndex], corners[childIndex], verts[4]);
            for (int i = 0; i < child.normals.length; i++) {
                data.addNormal(child.normals[i], child.weights[i],
                                child.corners[((i + 1) / 2) % 4]);
            }
        } else {
            for (int i = 0; i < child.normals.length; i++) {
                data.removeNormal(child.normals[i], child.weights[i],
                                child.corners[((i + 1) / 2) % 4]);
            }
            data.addNormal(normals[normIndex1], weights[normIndex1],
                                verts[(childIndex + 1) % 4], corners[childIndex], verts[4]);
            data.addNormal(normals[normIndex2], weights[normIndex2],
                                verts[childIndex], corners[childIndex], verts[4]);
        }
        child.notifyNormalsChange();
    }
    
    public void recomputeNormal(int normalIndex) {
        int cornerIndex = ((normalIndex + 1) / 2) % 4;
        int vertIndex = (normalIndex % 2 == 0) ? (cornerIndex + 1) % 4 : cornerIndex;
        data.removeNormal(normals[normalIndex], weights[normalIndex],
                            corners[cornerIndex], verts[vertIndex], verts[4]);
        Vec3D v1 = new Vec3D(corners[cornerIndex].getX() - verts[4].getX(),
                                corners[cornerIndex].getY() - verts[4].getY(),
                                data.getHeight(corners[cornerIndex]) - data.getHeight(verts[4]));
        Vec3D v2;
        if (data.isEnabled(verts[vertIndex])) {
            v2 = new Vec3D(verts[vertIndex].getX() - verts[4].getX(),
                            verts[vertIndex].getY() - verts[4].getY(),
                            data.getHeight(verts[vertIndex]) - data.getHeight(verts[4]));
        } else {
            float avg = 0.5f * data.getHeight(corners[vertIndex]) +
                        0.5f * data.getHeight(corners[(vertIndex + 3) % 4]);
            v2 = new Vec3D(verts[vertIndex].getX() - verts[4].getX(),
                            verts[vertIndex].getY() - verts[4].getY(),
                            avg - data.getHeight(verts[4]));
        }
        normals[normalIndex] = (normalIndex % 2 == 0) ?
                                Vec3D.cross(v1, v2).mult(0.5f) :
                                Vec3D.cross(v2, v1).mult(0.5f);
        weights[normalIndex] = normals[normalIndex].magnitude();
        data.addNormal(normals[normalIndex], weights[normalIndex],
                        corners[cornerIndex], verts[vertIndex], verts[4]);
        switch(normalIndex) {
            case 0:
                notifyNormalChange(0, 1);
                break;
            case 1:
                notifyNormalChange(2, 0);
                break;
            case 2:
                notifyNormalChange(1, 2);
                break;
            case 3:
                notifyNormalChange(3, 1);
                break;
            case 4:
                notifyNormalChange(2, 3);
                break;
            case 5:
                notifyNormalChange(0, 2);
                break;
            case 6:
                notifyNormalChange(3, 0);
                break;
            case 7:
                notifyNormalChange(1, 3);
                break;
        }
    }
    
    public void notifyNormalsChange() {
        QuadSquare neighbor1 = parent.notifyNormalChange(index, (index + 1) % 4);
        QuadSquare neighbor2 = parent.notifyNormalChange((index + 1) % 4, (index + 3) % 4);
        if (neighbor1 != null && neighbor2 != null) {
            if (neighbor1.level >= neighbor2.level) {
                neighbor1.notifyNormalChange((index + 1) % 4, (index + 2) % 4);
            } else {
                neighbor2.notifyNormalChange(index, (index + 2) % 4);
            }
        } else if (neighbor1 != null) {
            neighbor1.notifyNormalChange((index + 1) % 4, (index + 2) % 4);
        } else if (neighbor2 != null) {
            neighbor2.notifyNormalChange(index, (index + 2) % 4);
        } else {
            return;
        }
        neighbor1 = getNeighbor((index + 3) % 4);
        neighbor2 = getNeighbor((index + 2) % 4);
        if (neighbor1 != null && neighbor1.enabled) {
            neighbor1.markDirty();
            neighbor1.notifyNormalChange(index, (index + 1) % 4);
            neighbor1.notifyNormalChange((index + 2) % 4, index);
        }
        if (neighbor2 != null && neighbor2.enabled) {
            neighbor2.markDirty();
            neighbor2.notifyNormalChange((index + 1) % 4, (index + 3) % 4);
            neighbor2.notifyNormalChange((index + 3) % 4, index);
        }
    }
    
    public QuadSquare notifyNormalChange(int neighborIndex, int cornerIndex) {
        QuadSquare neighbor = getNeighbor(neighborIndex);
        if (neighbor != null && neighbor != NULL_NEIGHBOR) {
            if (neighbor.enabled) {
                if (neighbor.children[cornerIndex] != null && neighbor.children[cornerIndex].enabled) {
                    neighbor = neighbor.children[cornerIndex];
                }
                neighbor.markDirty();
                return neighbor;
            } else if (parent != null && parent != neighbor.parent) {
                neighbor.parent.markDirty();
                return neighbor.parent;
            }
        }
        return null;
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
                        recomputeNormal((2 * i + 6) % 8);
                        recomputeNormal((2 * i + 7) % 8);
                        markDirty();
                        //data.setEnabled(verts[i], true);
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
                    recomputeNormal((2 * i + 6) % 8);
                    recomputeNormal((2 * i + 7) % 8);
                    markDirty();
                    //data.setEnabled(verts[i], false);
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
                parent.disableChild(index);
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
    
    public void render3() {
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
    
    public void render() {
        if (!isDirty) {
            return;
        }
        
        if (mesh == null) {
            mesh = new MeshView(new TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD));
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
            squareMesh.getNormals().clear();
            squareMesh.getTexCoords().clear();
            squareMesh.getFaces().clear();
            //squareMesh.getFaceSmoothingGroups().clear();
            squareMesh.getTexCoords().addAll(0f, 0f);
            ObservableFloatArray points = squareMesh.getPoints();
            ObservableFloatArray norms = squareMesh.getNormals();
            points.addAll(verts[4].getX(), verts[4].getY(), data.getHeight(verts[4]));
            
            for (int i = 0; i < 4; i++) {
                if (children[i] != null && children[i].enabled) {
                    if (children[i].isDirty) {
                        children[i].render();
                    }
                    connectPoints = false;
                    if (children[(i + 1) % 4] == null || (children[(i + 1) % 4] != null && !children[(i + 1) % 4].enabled)) {
                        points.addAll(verts[(i + 1) % 4].getX(), verts[(i + 1) % 4].getY(), data.getHeight(verts[(i + 1) % 4]));
                        Vec3D normalized = Vec3D.normalize(normals[2 * i + 1]);
                        norms.addAll(normalized.getX(), normalized.getY(), normalized.getZ());
                        connectPoints = true;
                    }
                } else {
                    if (children[i] != null && children[i].isDirty) {
                        children[i].render();
                    }
                    points.addAll(corners[i].getX(), corners[i].getY(), data.getHeight(corners[i]));
                    Vec3D normalized = Vec3D.normalize(normals[2 * i]);
                    norms.addAll(normalized.getX(), normalized.getY(), normalized.getZ());
                    if (!connectPoints) {
                        connectPoints = true;
                    } else {
                        int last = (points.size() / 3) - 1;
                        squareMesh.getFaces().addAll(0, last - 2, 0, last - 1, last - 2, 0, last, last - 2, 0);
                        //squareMesh.getFaceSmoothingGroups().addAll(1 << squareMesh.getFaces().size());
                    }
                    if (data.isEnabled(verts[(i + 1) % 4])) {
                        points.addAll(verts[(i + 1) % 4].getX(), verts[(i + 1) % 4].getY(), data.getHeight(verts[(i + 1) % 4]));
                        normalized = Vec3D.normalize(normals[2 * i + 1]);
                        norms.addAll(normalized.getX(), normalized.getY(), normalized.getZ());
                        int last = (points.size() / 3) - 1;
                        squareMesh.getFaces().addAll(0, last - 2, 0, last - 1, last - 2, 0, last, last - 2, 0);
                        //squareMesh.getFaceSmoothingGroups().addAll(1 << squareMesh.getFaces().size());
                    }
                }
            }
            if (children[0] == null || !children[0].enabled) {
                int last = (points.size() / 3) - 1;
                squareMesh.getFaces().addAll(0, last - 1, 0, last, last - 1, 0, 1, last - 1, 0);
                //squareMesh.getFaceSmoothingGroups().addAll(1 << squareMesh.getFaces().size());
            }
            
            mesh.setMesh(squareMesh);
            if (points.size() <= 3) {
                points.clear();
            }
        } else {
            TriangleMesh squareMesh = (TriangleMesh) mesh.getMesh();
            squareMesh.getPoints().clear();
            squareMesh.getNormals().clear();
            squareMesh.getTexCoords().clear();
            squareMesh.getFaces().clear();
            //squareMesh.getFaceSmoothingGroups().clear();
            meshGroup.getChildren().remove(1, meshGroup.getChildren().size());
        }
        isDirty = false;
    }
    
    public void render(Color c) {
        if (!isDirty) {
            return;
        }
        
        if (mesh == null) {
            mesh = new MeshView(new TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD));
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
        
        //mesh.setMaterial(new PhongMaterial(c));
        if (enabled) {
            boolean connectPoints = false;
            TriangleMesh squareMesh = (TriangleMesh) mesh.getMesh();
            squareMesh.getPoints().clear();
            squareMesh.getNormals().clear();
            squareMesh.getTexCoords().clear();
            squareMesh.getFaces().clear();
            //squareMesh.getFaceSmoothingGroups().clear();
            squareMesh.getTexCoords().addAll(0f, 0f);
            ObservableFloatArray points = squareMesh.getPoints();
            ObservableFloatArray norms = squareMesh.getNormals();
            points.addAll(verts[4].getX(), verts[4].getY(), data.getHeight(verts[4]));
            Vec3D normalized = data.getNormal(verts[4]);
            norms.addAll(normalized.getX(), normalized.getY(), normalized.getZ());
            
            for (int i = 0; i < 4; i++) {
                if (children[i] != null && children[i].enabled) {
                    //if (children[i].isDirty) {
                        children[i].render(c);
                    //}
                    connectPoints = false;
                    if (children[(i + 1) % 4] == null || (children[(i + 1) % 4] != null && !children[(i + 1) % 4].enabled)) {
                        points.addAll(verts[(i + 1) % 4].getX(), verts[(i + 1) % 4].getY(), data.getHeight(verts[(i + 1) % 4]));
                        normalized = data.getNormal(verts[(i + 1) % 4]);
                        norms.addAll(normalized.getX(), normalized.getY(), normalized.getZ());
                        connectPoints = true;
                    }
                } else {
                    if (children[i] != null) {// && children[i].isDirty) {
                        children[i].render(c);
                    }
                    points.addAll(corners[i].getX(), corners[i].getY(), data.getHeight(corners[i]));
                    normalized = data.getNormal(corners[i]);
                    norms.addAll(normalized.getX(), normalized.getY(), normalized.getZ());
                    if (!connectPoints) {
                        connectPoints = true;
                    } else {
                        int last = (points.size() / 3) - 1;
                        squareMesh.getFaces().addAll(0, 0, 0, last - 1, last - 1, 0, last, last, 0);
                        //squareMesh.getFaceSmoothingGroups().addAll(1 << squareMesh.getFaces().size());
                    }
                    if (data.isEnabled(verts[(i + 1) % 4])) {
                        points.addAll(verts[(i + 1) % 4].getX(), verts[(i + 1) % 4].getY(), data.getHeight(verts[(i + 1) % 4]));
                        normalized = data.getNormal(verts[(i + 1) % 4]);
                        norms.addAll(normalized.getX(), normalized.getY(), normalized.getZ());
                        int last = (points.size() / 3) - 1;
                        squareMesh.getFaces().addAll(0, 0, 0, last - 1, last - 1, 0, last, last, 0);
                        //squareMesh.getFaceSmoothingGroups().addAll(1 << squareMesh.getFaces().size());
                    }
                }
            }
            if (children[0] == null || !children[0].enabled) {
                int last = (points.size() / 3) - 1;
                squareMesh.getFaces().addAll(0, 0, 0, last, last, 0, 1, 1, 0);
                //squareMesh.getFaceSmoothingGroups().addAll(1 << squareMesh.getFaces().size());
            }
            
            mesh.setMesh(squareMesh);
            if (points.size() <= 3) {
                points.clear();
                norms.clear();
            }
        } else {
            TriangleMesh squareMesh = (TriangleMesh) mesh.getMesh();
            squareMesh.getPoints().clear();
            squareMesh.getNormals().clear();
            squareMesh.getTexCoords().clear();
            squareMesh.getFaces().clear();
            //squareMesh.getFaceSmoothingGroups().clear();
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
    
    public void test(TriangleMesh normals, TriangleMesh lights, float x, float y, float z) {
        ObservableFloatArray normalPoints = normals.getPoints();
        normalPoints.clear();
        normals.getFaces().clear();
        ObservableFloatArray lightPoints = lights.getPoints();
        lightPoints.clear();
        lights.getFaces().clear();
        for (Coordinate vert : data.vertices.keySet()) {
            if (data.isEnabled(vert)) {
                Vec3D point = new Vec3D(vert.getX(), vert.getY(), data.getHeight(vert));
                Vec3D normal = Vec3D.add(point, data.getNormal(vert).normalize().mult(2f));
                Vec3D light = Vec3D.add(point, (new Vec3D(point.getX(), point.getY(), point.getZ(), x, y, z)).normalize());
                normalPoints.addAll(point.getX(), point.getY(), point.getZ());
                lightPoints.addAll(point.getX(), point.getY(), point.getZ());
                normalPoints.addAll(normal.getX(), normal.getY(), normal.getZ());
                lightPoints.addAll(light.getX(), light.getY(), light.getZ());
                int index1 = (normalPoints.size() / 3) - 1;
                int index2 = (lightPoints.size() / 3) - 1;
                normals.getFaces().addAll(index1 - 1, 0, index1 - 1, 0, index1, 0);
                lights.getFaces().addAll(index2 - 1, 0, index2 - 1, 0, index2, 0);
            }
        }
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
            if (futureChild != null && !futureChild.isCancelled()) {
                if (futureChild.isDone()) {
                    try {
                        QuadSquare child = futureChild.get();
                        data.removeVertex(child.verts[4]);
                        for (int j = 0; j < child.verts.length - 1; j++) {
                            if (j == child.index) {
                                QuadSquare neighbor = child.parent.getNeighbor(j);
                                if (neighbor == NULL_NEIGHBOR || !neighbor.enabled) {
                                    data.removeVertex(child.verts[j]);
                                }
                            } else if (j == (child.index + 1) % 4) {
                                QuadSquare neighbor = child.parent.getNeighbor(j);
                                if (neighbor == NULL_NEIGHBOR || !neighbor.enabled) {
                                    data.removeVertex(child.verts[j]);
                                }
                            } else {
                                data.removeVertex(child.verts[j]);
                            }
                        }
                        quadSquareBuffer.remove(center);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                } else {
                    futureChild.cancel(true);
                    quadSquareBuffer.remove(center);
                }
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
                        for (int i =  0; i < child.normals.length; i += 2) {
                            addNormal(child.normals[i], child.weights[i],
                                        child.verts[(i / 2 + 1) % 4], child.verts[4]);
                            addNormal(child.normals[i + 1], child.weights[i + 1],
                                        child.verts[(i / 2 + 1) % 4], child.verts[4]);
                        }
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
        
        void addNormal(Vec3D normal, float weight, Coordinate ... cs) {
            for (Coordinate c : cs) {
                vertices.get(c).addNormal(normal, weight);
            }
        }
        
        VertexData getVertex(Coordinate c) {
            return vertices.get(c);
        }
        
        Vec3D getNormal(Coordinate c) {
            return vertices.get(c).getNormal();
        }
        
        void removeNormal(Vec3D normal, float weight, Coordinate ... cs) {
            for (Coordinate c : cs) {
                vertices.get(c).removeNormal(normal, weight);
            }
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