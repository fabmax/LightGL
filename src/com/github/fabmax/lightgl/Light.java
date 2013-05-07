package com.github.fabmax.lightgl;

/**
 * A standard light. It can be a point light (posW = 1) or a directional light (posW = 0) and has
 * a position and a color. Notice that the used shader must support lights.
 * 
 * @author fabmax
 * 
 */
public class Light {

    /** Light position */
    public float posX = 0, posY = 0, posZ = 0, posW = 1;

    /** Light color */
    public float colorR = 1, colorG = 1, colorB = 1, colorA = 1;
    
    /**
     * Creates a point light with the specified position and color.
     * 
     * @return a point light with the specified position and color
     */
    public static Light createPointLight(float posX, float posY, float posZ, float r, float g, float b) {
        Light l = new Light();
        l.posX = posX;     l.posY = posY;     l.posZ = posZ;     l.posW = 1;
        l.colorR = r;      l.colorG = g;      l.colorB = b;      l.colorA = 1;
        return l;
    }

    /**
     * Creates a directional light with the specified position and color.
     * 
     * @return a directional light with the specified position and color
     */
    public static Light createDirectionalLight(float dirX, float dirY, float dirZ, float r, float g, float b) {
        Light l = new Light();
        l.posX = dirX;     l.posY = dirY;     l.posZ = dirZ;     l.posW = 0;
        l.colorR = r;      l.colorG = g;      l.colorB = b;      l.colorA = 1;
        return l;
    }
}
