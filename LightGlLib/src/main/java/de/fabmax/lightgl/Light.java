package de.fabmax.lightgl;

/**
 * A standard light. It can be a point light (position[3] = 1) or a directional light (position[3] =
 * 0) and has a position and a color. Notice that the used shader must support lights.
 * 
 * @author fabmax
 * 
 */
public class Light {

    /** Light position ( X, Y, Z, W ) */
    public float[] position = new float[4];

    /** Light color ( R, G, B, A ) */
    public float[] color = new float[4];
    
    /**
     * Creates a point light with the specified position and color.
     * 
     * @return a point light with the specified position and color
     */
    public static Light createPointLight(float posX, float posY, float posZ, float r, float g, float b) {
        Light l = new Light();
        l.position[0] = posX;  l.position[1] = posY;  l.position[2] = posZ;  l.position[3] = 1;
        l.color[0] = r;        l.color[1] = g;        l.color[2] = b;        l.color[3] = 1;
        return l;
    }

    /**
     * Creates a directional light with the specified position and color.
     * 
     * @return a directional light with the specified position and color
     */
    public static Light createDirectionalLight(float dirX, float dirY, float dirZ, float r, float g, float b) {
        Light l = new Light();
        l.position[0] = dirX;  l.position[1] = dirY;  l.position[2] = dirZ;  l.position[3] = 0;
        l.color[0] = r;        l.color[1] = g;        l.color[2] = b;        l.color[3] = 1;
        return l;
    }
}
