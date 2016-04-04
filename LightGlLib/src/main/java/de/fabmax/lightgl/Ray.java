package de.fabmax.lightgl;

/**
 * A Ray with an origin and a direction.
 * 
 * @author fabmax
 * 
 */
public class Ray {

    /**
     * Ray origin (x, y, z, w). w should have a value of 1.
     */
    public final float[] origin;

    /**
     * Ray direction (x, y, z, w). w should have a value of 0. If nothing else is stated, all
     * functions taking a Ray as argument expect the direction vector to have an arbitrary non-zero
     * length.
     */
    public final float[] direction;

    /**
     * Creates a Ray with origin = (0, 0, 0) and direction (1, 0, 0).
     * 
     * @see Camera#getPickRay(int[], float, float, Ray)
     */
    public Ray() {
        origin = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
        direction = new float[] { 1.0f, 0.0f, 0.0f, 0.0f };
    }

    /**
     * Sets the direction vector of this Ray.
     */
    public void setDirection(float x, float y, float z) {
        direction[0] = x;
        direction[1] = y;
        direction[2] = z;
        direction[3] = 0;
    }

    /**
     * Sets the origin vector of this Ray.
     */
    public void setOrigin(float x, float y, float z) {
        origin[0] = x;
        origin[1] = y;
        origin[2] = z;
        origin[3] = 1;
    }

}
