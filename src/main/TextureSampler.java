/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.ArrayList;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

/**
 *
 * @author Administrator
 */
public class TextureSampler {
    private ArrayList<Texture> textures;
    private float heightFalloff;
    private float slopeFalloff;

    public TextureSampler(float pHeightFalloff, float pSlopeFalloff) {
        textures = new ArrayList<>();
        heightFalloff = pHeightFalloff;
        slopeFalloff = pSlopeFalloff;
    }
    
    public void addTexture(String name, float minHeight, float maxHeight, float minSlope, float maxSlope, int scale) {
        textures.add(new Texture(name, minHeight, maxHeight, minSlope, maxSlope, scale));
    }
    
    public Color sample(float x, float y, float height, float slope) {
        for (Texture t : textures) {
            if (t.contains(height, slope)) {
                return t.sample(x, y, height, slope);
            }
        }
        return Color.BLACK;
    }
    
    private class Texture {
        Image texture;
        PixelReader textureReader;
        float minHeight;
        float maxHeight;
        float minSlope;
        float maxSlope;
        float scale;
        
        Texture(String pName, float pMinHeight, float pMaxHeight, float pMinSlope, float pMaxSlope, float pScale) {
            texture = new Image(getClass().getResource(pName).toExternalForm());
            textureReader = texture.getPixelReader();
            minHeight = pMinHeight;
            maxHeight = pMaxHeight;
            minSlope = pMinSlope;
            maxSlope = pMaxSlope;
            scale = pScale;
        }
        
        boolean contains(float height, float slope) {
            return height >= minHeight && height < maxHeight && slope >= minSlope && slope < maxSlope;
        }
        
        Color sample(float x, float y, float height, float slope) {
            float px = (float) texture.getWidth() * (x % scale) / scale;
            float py = (float) texture.getHeight() * (y % scale) / scale;
            int pxInt1 = (int) px;
            int pxInt2 = ((int) px + 1) % (int) texture.getWidth(); 
            int pyInt1 = (int) py;
            int pyInt2 = ((int) px + 1) % (int) texture.getHeight();
            float tx = px - pxInt1;
            float ty = py - pyInt1;
            Color c1 = textureReader.getColor(pxInt1, pyInt1);
            Color c2 = textureReader.getColor(pxInt1, pyInt2);
            Color c3 = textureReader.getColor(pxInt2, pyInt1);
            Color c4 = textureReader.getColor(pxInt2, pyInt2);
            return c1.interpolate(c2, tx).interpolate(c3.interpolate(c4, tx), ty);
        }
    }
}
