package com.github.fabmax.lightgl;

/**
 * A standard light. It can be a point light (mPosW = 1) or a directional light (mPosW = 0) and has
 * a position and a color.
 * 
 * @author fabmax
 * 
 */
public class Light {

    /** Light position */
    public float mPosX = 0, mPosY = 0, mPosZ = 0, mPosW = 1;

    /** Light color */
    public float mColorR = 1, mColorG = 1, mColorB = 1, mColorA = 1;
    
    /**
     * Creates a point light with the specified position and color.
     * 
     * @return a point light with the specified position and color
     */
    public static Light createPointLight(float posX, float posY, float posZ, float r, float g, float b) {
        Light l = new Light();
        l.mPosX = posX;     l.mPosY = posY;     l.mPosZ = posZ;     l.mPosW = 1;
        l.mColorR = r;      l.mColorG = g;      l.mColorB = b;      l.mColorA = 1;
        return l;
    }

    /**
     * Creates a directional light with the specified position and color.
     * 
     * @return a directional light with the specified position and color
     */
    public static Light createDirectionalLight(float dirX, float dirY, float dirZ, float r, float g, float b) {
        Light l = new Light();
        l.mPosX = dirX;     l.mPosY = dirY;     l.mPosZ = dirZ;     l.mPosW = 0;
        l.mColorR = r;      l.mColorG = g;      l.mColorB = b;      l.mColorA = 1;
        return l;
    }
}
