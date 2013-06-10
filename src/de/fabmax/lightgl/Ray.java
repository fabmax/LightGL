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
    public float[] origin;

    /**
     * Ray direction (x, y, z, w). w should have a value of 0. If nothing else is stated, all
     * functions taking a Ray as argument expect the direction vector to have an arbitrary non-zero
     * length.
     */
    public float[] direction;

    /**
     * Creates a Ray with origin = (0, 0, 0, 1) and direction (1, 0, 0, 0).
     * 
     * @see GfxEngine#getPickRay(int, int, Ray)
     */
    public Ray() {
        origin = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
        direction = new float[] { 1.0f, 0.0f, 0.0f, 0.0f };
    }

}
