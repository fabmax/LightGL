package de.fabmax.lightgl;

/**
 * An axis aligned bounding box.
 * 
 * @author fabmax
 * 
 */
public class BoundingBox {

    public float minX, minY, minZ;
    public float maxX, maxY, maxZ;

    /**
     * Creates a new BoundingBox with the specified initial values for minX, minY, minZ, maxX, maxY
     * and maxZ.
     * 
     * @param x
     *            initial value for minX and maxX
     * @param y
     *            initial value for minY and maxY
     * @param z
     *            initial value for minZ and maxZ
     */
    public BoundingBox(float x, float y, float z) {
        reset(x, y, z);
    }

    /**
     * Creates a new BoundingBox with the specified initial values for minX, minY, minZ, maxX, maxY
     * and maxZ.
     * 
     * @param minX
     *            initial value for minX
     * @param maxX
     *            initial value for maxX
     * @param minY
     *            initial value for minY
     * @param maxY
     *            initial value for maxY
     * @param minZ
     *            initial value for minZ
     * @param maxZ
     *            initial value for maxZ
     */
    public BoundingBox(float minX, float maxX, float minY, float maxY, float minZ, float maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    /**
     * Resets minX, minY, minZ, maxX, maxY and maxZ to the specified values.
     * 
     * @param x
     *            reset value for minX and maxX
     * @param y
     *            reset value for minY and maxY
     * @param z
     *            reset value for minZ and maxZ
     */
    public void reset(float x, float y, float z) {
        minX = maxX = x;
        minY = maxY = y;
        minZ = maxZ = z;
    }

    /**
     * Resets minX, minY, minZ, maxX, maxY and maxZ to the specified values.
     * 
     * @param point
     *            reset values
     */
    public void reset(float[] point) {
        minX = maxX = point[0];
        minY = maxY = point[1];
        minZ = maxZ = point[2];
    }
    
    /**
     * Sets this BoundingBox to the same dimensions as box.
     * 
     * @param box
     *            the BoundingBox dimensions to set
     */
    public void set(BoundingBox box) {
        minX = box.minX;
        minY = box.minY;
        minZ = box.minZ;
        maxX = box.maxX;
        maxY = box.maxY;
        maxZ = box.maxZ;
    }

    /**
     * Adds a point to this BoundingBox. If the point is included by the BoundingBox the Box remains
     * the same. Otherwise the Box is expanded to include the specified point.
     * 
     * @param x
     *            X value to add
     * @param y
     *            Y value to add
     * @param z
     *            Z value to add
     */
    public void addPoint(float x, float y, float z) {
        if (x < minX) {
            minX = x;
        } else if (x > maxX) {
            maxX = x;
        }

        if (y < minY) {
            minY = y;
        } else if (y > maxY) {
            maxY = y;
        }

        if (z < minZ) {
            minZ = z;
        } else if (z > maxZ) {
            maxZ = z;
        }
    }

    /**
     * Adds a point to this BoundingBox. If the point is included by the BoundingBox the Box remains
     * the same. Otherwise the Box is expanded to include the specified point.
     * 
     * @param point
     *            the point to add
     */
    public void addPoint(float[] point) {
        if (point[0] < minX) {
            minX = point[0];
        } else if (point[0] > maxX) {
            maxX = point[0];
        }

        if (point[1] < minY) {
            minY = point[1];
        } else if (point[1] > maxY) {
            maxY = point[1];
        }

        if (point[2] < minZ) {
            minZ = point[2];
        } else if (point[2] > maxZ) {
            maxZ = point[2];
        }
    }

    /**
     * Tests whether the specified coordinates are included by this BoundingBox.
     * 
     * @param x
     *            X coordinate to test
     * @param y
     *            Y coordinate to test
     * @param z
     *            Z coordinate to test
     * @return true if the point is included, false otherwise
     */
    public boolean includes(float x, float y, float z) {
        return x >= minX && x <= maxX &&
               y >= minY && y <= maxY &&
               z >= minZ && z <= maxZ;
    }
    
    /**
     * Computes the squared hit distance for the specified {@link Ray}. If the Ray does not
     * intersect this BoundingBox {@link Float#MAX_VALUE} is returned. If the ray origin is inside
     * the BoundingBox 0 is returned. The method returns the squared distance because it's faster to
     * compute. If you need the exact distance use {@link Math#sqrt(double)} with the result.
     * 
     * @param r
     *            Ray to test
     * @return squared distance between Ray origin and BoundingBox or Float.MAX_VALUE if the Ray
     *         does not intersects the BoundingBox
     */
    public float computeHitDistanceSqr(Ray r) {
        float tmin, tmax, tymin, tymax, tzmin, tzmax, div;
        float[] ro = r.origin;
        float[] rd = r.direction;
        
        if (includes(ro[0], ro[1], ro[2])) {
            return 0.0f;
        }

        div = 1.0f / rd[0];
        if (div >= 0.0f) {
            tmin = (minX - ro[0]) * div;
            tmax = (maxX - ro[0]) * div;
        } else {
            tmin = (maxX - ro[0]) * div;
            tmax = (minX - ro[0]) * div;
        }

        div = 1.0f / rd[1];
        if (div >= 0.0f) {
            tymin = (minY - ro[1]) * div;
            tymax = (maxY - ro[1]) * div;
        } else {
            tymin = (maxY - ro[1]) * div;
            tymax = (minY - ro[1]) * div;
        }

        if ((tmin > tymax) || (tymin > tmax)) {
            // no intersection
            return Float.MAX_VALUE;
        }
        if (tymin > tmin) {
            tmin = tymin;
        }
        if (tymax < tmax) {
            tmax = tymax;
        }

        div = 1.0f / rd[2];
        if (div >= 0.0f) {
            tzmin = (minZ - ro[2]) * div;
            tzmax = (maxZ - ro[2]) * div;
        } else {
            tzmin = (maxZ - ro[2]) * div;
            tzmax = (minZ - ro[2]) * div;
        }

        if ((tmin > tzmax) || (tzmin > tmax)) {
            // no intersection
            return Float.MAX_VALUE;
        }
        if (tzmin > tmin) {
            tmin = tzmin;
        }
        if (tzmax < tmax) {
            tmax = tzmax;
        }

        if (tmin > 0) {
            // hit! calculate square distance between ray origin and hit point
            float comp = rd[0] * tmin;
            float dist = comp * comp;
            comp = rd[1] * tmin;
            dist += comp * comp;
            comp = rd[2] * tmin;
            dist += comp * comp;
            return dist;
        } else {
            // no intersection
            return Float.MAX_VALUE;
        }
    }

    /**
     * Returns the minimum point included by this BoundingBox.
     * 
     * @param point
     *            array to store the minimum point coordinates in.
     */
    public void getMin(float[] point) {
        point[0] = minX;
        point[1] = minY;
        point[2] = minZ;
    }

    /**
     * Returns the maximum point included by this BoundingBox.
     * 
     * @param point
     *            array to store the maximum point coordinates in.
     */
    public void getMax(float[] point) {
        point[0] = maxX;
        point[1] = maxY;
        point[2] = maxZ;
    }
}
