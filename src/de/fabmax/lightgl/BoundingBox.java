package de.fabmax.lightgl;

/**
 * An axis aligned bounding box.
 * 
 * @author fabmax
 * 
 */
public class BoundingBox {

    private float mMinX, mMinY, mMinZ;
    private float mMaxX, mMaxY, mMaxZ;

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
        mMinX = minX;
        mMinY = minY;
        mMinZ = minZ;
        mMaxX = maxX;
        mMaxY = maxY;
        mMaxZ = maxZ;
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
        mMinX = mMaxX = x;
        mMinY = mMaxY = y;
        mMinZ = mMaxZ = z;
    }

    /**
     * Resets minX, minY, minZ, maxX, maxY and maxZ to the specified values.
     * 
     * @param point
     *            reset values
     */
    public void reset(float[] point) {
        mMinX = mMaxX = point[0];
        mMinY = mMaxY = point[1];
        mMinZ = mMaxZ = point[2];
    }
    
    /**
     * Sets this BoundingBox to the same dimensions as box.
     * 
     * @param box
     *            the BoundingBox dimensions to set
     */
    public void set(BoundingBox box) {
        mMinX = box.mMinX;
        mMinY = box.mMinY;
        mMinZ = box.mMinZ;
        mMaxX = box.mMaxX;
        mMaxY = box.mMaxY;
        mMaxZ = box.mMaxZ;
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
        if (x < mMinX) {
            mMinX = x;
        } else if (x > mMaxX) {
            mMaxX = x;
        }

        if (y < mMinY) {
            mMinY = y;
        } else if (y > mMaxY) {
            mMaxY = y;
        }

        if (z < mMinZ) {
            mMinZ = z;
        } else if (z > mMaxZ) {
            mMaxZ = z;
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
        if (point[0] < mMinX) {
            mMinX = point[0];
        } else if (point[0] > mMaxX) {
            mMaxX = point[0];
        }

        if (point[1] < mMinY) {
            mMinY = point[1];
        } else if (point[1] > mMaxY) {
            mMaxY = point[1];
        }

        if (point[2] < mMinZ) {
            mMinZ = point[2];
        } else if (point[2] > mMaxZ) {
            mMaxZ = point[2];
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
        return x >= mMinX && x <= mMaxX &&
               y >= mMinY && y <= mMaxY &&
               z >= mMinZ && z <= mMaxZ;
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
            tmin = (mMinX - ro[0]) * div;
            tmax = (mMaxX - ro[0]) * div;
        } else {
            tmin = (mMaxX - ro[0]) * div;
            tmax = (mMinX - ro[0]) * div;
        }

        div = 1.0f / rd[1];
        if (div >= 0.0f) {
            tymin = (mMinY - ro[1]) * div;
            tymax = (mMaxY - ro[1]) * div;
        } else {
            tymin = (mMaxY - ro[1]) * div;
            tymax = (mMinY - ro[1]) * div;
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
            tzmin = (mMinZ - ro[2]) * div;
            tzmax = (mMaxZ - ro[2]) * div;
        } else {
            tzmin = (mMaxZ - ro[2]) * div;
            tzmax = (mMinZ - ro[2]) * div;
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
        point[0] = mMinX;
        point[1] = mMinY;
        point[2] = mMinZ;
    }

    /**
     * Returns the maximum point included by this BoundingBox.
     * 
     * @param point
     *            array to store the maximum point coordinates in.
     */
    public void getMax(float[] point) {
        point[0] = mMaxX;
        point[1] = mMaxY;
        point[2] = mMaxZ;
    }

    /**
     * Returns the minimum X value included by this BoundingBox.
     * 
     * @return the minimum X value
     */
    public float getMinX() {
        return mMinX;
    }

    /**
     * Sets the minimum X value included by this BoundingBox.
     * 
     * @param minX
     *            minimum X value
     */
    public void setMinX(float minX) {
        mMinX = minX;
    }

    /**
     * Returns the minimum Y value included by this BoundingBox.
     * 
     * @return the minimum Y value
     */
    public float getMinY() {
        return mMinY;
    }

    /**
     * Sets the minimum Y value included by this BoundingBox.
     * 
     * @param minY
     *            minimum Y value
     */
    public void setMinY(float minY) {
        mMinY = minY;
    }

    /**
     * Returns the minimum Z value included by this BoundingBox.
     * 
     * @return the minimum Z value
     */
    public float getMinZ() {
        return mMinZ;
    }

    /**
     * Sets the minimum Z value included by this BoundingBox.
     * 
     * @param minZ
     *            minimum Z value
     */
    public void setMinZ(float minZ) {
        mMinZ = minZ;
    }

    /**
     * Returns the maximum X value included by this BoundingBox.
     * 
     * @return the maximum X value
     */
    public float getMaxX() {
        return mMaxX;
    }

    /**
     * Sets the maximum X value included by this BoundingBox.
     * 
     * @param maxX
     *            maximum X value
     */
    public void setMaxX(float maxX) {
        mMaxX = maxX;
    }

    /**
     * Returns the maximum Y value included by this BoundingBox.
     * 
     * @return the maximum Y value
     */
    public float getMaxY() {
        return mMaxY;
    }

    /**
     * Sets the maximum X value included by this BoundingBox.
     * 
     * @param maxY
     *            maximum Y value
     */
    public void setMaxY(float maxY) {
        mMaxY = maxY;
    }

    /**
     * Returns the maximum Z value included by this BoundingBox.
     * 
     * @return the maximum Z value
     */
    public float getMaxZ() {
        return mMaxZ;
    }

    /**
     * Sets the maximum Z value included by this BoundingBox.
     * 
     * @param maxZ
     *            maximum Z value
     */
    public void setMaxZ(float maxZ) {
        mMaxZ = maxZ;
    }
}
