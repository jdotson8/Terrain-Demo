/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import javafx.beans.property.DoubleProperty;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 *
 * @author Administrator
 */
public class TransformLayer extends Group {
    
    public enum RotateOrder {
        XYZ, XZY, YXZ, YZX, ZXY, ZYX
    }

    private Translate t  = new Translate(); 
    private Translate p  = new Translate(); 
    private Translate ip = new Translate(); 
    private Rotate rx = new Rotate();
    private Rotate ry = new Rotate();
    private Rotate rz = new Rotate();
    private Scale s = new Scale();

    {
        rx.setAxis(Rotate.X_AXIS); 
        ry.setAxis(Rotate.Y_AXIS); 
        rz.setAxis(Rotate.Z_AXIS);
        ip.xProperty().bind(p.xProperty().negate());
        ip.yProperty().bind(p.yProperty().negate());
        ip.zProperty().bind(p.zProperty().negate());
    }

    public TransformLayer() { 
        super(); 
        getTransforms().addAll(t, rz, ry, rx, s); 
    }

    public TransformLayer(RotateOrder rotateOrder) { 
        super(); 
        switch (rotateOrder) {
        case XYZ:
            getTransforms().addAll(t, p, rz, ry, rx, s, ip); 
            break;
        case XZY:
            getTransforms().addAll(t, p, ry, rz, rx, s, ip); 
            break;
        case YXZ:
            getTransforms().addAll(t, p, rz, rx, ry, s, ip); 
            break;
        case YZX:
            getTransforms().addAll(t, p, rx, rz, ry, s, ip);
            break;
        case ZXY:
            getTransforms().addAll(t, p, ry, rx, rz, s, ip); 
            break;
        case ZYX:
            getTransforms().addAll(t, p, rx, ry, rz, s, ip); 
            break;
        }
    }
    
    public DoubleProperty txProperty() {
        return t.xProperty();
    }
    
    public DoubleProperty tyProperty() {
        return t.yProperty();
    }
    
    public DoubleProperty tzProperty() {
        return t.zProperty();
    }

    public void setTranslate(double x, double y, double z) {
        t.setX(x);
        t.setY(y);
        t.setZ(z);
    }

    public void setTranslate(double x, double y) {
        t.setX(x);
        t.setY(y);
    }
    
    public void setTx(double x) {
        t.setX(x);
    }
    
    public void setTy(double y) {
        t.setY(y);
    }

    public void setTz(double z) {
        t.setZ(z);
    }
    
    public DoubleProperty rxProperty() {
        return rx.angleProperty();
    }
    
    public DoubleProperty ryProperty() {
        return ry.angleProperty();
    }
    
    public DoubleProperty rzProperty() {
        return rz.angleProperty();
    }

    public void setRotate(double x, double y, double z) {
        rx.setAngle(x);
        ry.setAngle(y);
        rz.setAngle(z);
    }

    public void setRotateX(double x) {
        rx.setAngle(x);
    }
    
    public void setRotateY(double y) {
        ry.setAngle(y);
    }
    
    public void setRotateZ(double z) {
        rz.setAngle(z);
    }
    
    public DoubleProperty sxProperty() {
        return s.xProperty();
    }
    
    public DoubleProperty syProperty() {
        return s.yProperty();
    }
    
    public DoubleProperty szProperty() {
        return s.zProperty();
    }
    
    public void setRx(double x) {
        rx.setAngle(x);
    }
    
    public void setRy(double y) {
        ry.setAngle(y);
    }
    
    public void setRz(double z) {
        rz.setAngle(z);
    }

    public void setScale(double scaleFactor) {
        s.setX(scaleFactor);
        s.setY(scaleFactor);
        s.setZ(scaleFactor);
    }

    public void setScale(double x, double y, double z) {
        s.setX(x);
        s.setY(y);
        s.setZ(z);
    }

    public void setSx(double x) {
        s.setX(x);
    }
    
    public void setSy(double y) {
        s.setY(y);
    }
    
    public void setSz(double z) {
        s.setZ(z);
    }

    public DoubleProperty pxProperty() {
        return p.xProperty();
    }
    
    public DoubleProperty pyProperty() {
        return p.yProperty();
    }
    
    public DoubleProperty pzProperty() {
        return p.zProperty();
    }
    
    public void setPivot(double x, double y, double z) {
        p.setX(x);
        p.setY(y);
        p.setZ(z);
    }
    
    public void setPx(double x) {
        p.setX(x);
    }
    
    public void setPy(double y) {
        p.setY(y);
    }
    
    public void setPz(double z) {
        p.setZ(z);
    }

    public void reset() {
        t.setX(0.0);
        t.setY(0.0);
        t.setZ(0.0);
        rx.setAngle(0.0);
        ry.setAngle(0.0);
        rz.setAngle(0.0);
        s.setX(1.0);
        s.setY(1.0);
        s.setZ(1.0);
        p.setX(0.0);
        p.setY(0.0);
        p.setZ(0.0);
        ip.setX(0.0);
        ip.setY(0.0);
        ip.setZ(0.0);
    }

    public void resetTSP() {
        t.setX(0.0);
        t.setY(0.0);
        t.setZ(0.0);
        s.setX(1.0);
        s.setY(1.0);
        s.setZ(1.0);
        p.setX(0.0);
        p.setY(0.0);
        p.setZ(0.0);
        ip.setX(0.0);
        ip.setY(0.0);
        ip.setZ(0.0);
    }
}
