package de.fabmax.lightgl.util;

import java.util.Arrays;
import java.util.Comparator;

import de.fabmax.lightgl.BoundingBox;
import de.fabmax.lightgl.Ray;

/**
 * A KdTree for efficient ray testing against a triangle mesh. A hit test determines the hit point,
 * the distance between the test ray's origin and the hit point and the face normal at the hit
 * point.
 *
 * @author fabmax
 */
public class TriangleKdTree {
    // maximum number of triangles in a tree leaf
    private static final int BUCKET_SIZE = 10;

    // temp vector offsets
    private static final int E1 = 0;
    private static final int E2 = 3;
    private static final int S = 6;
    private static final int P = 9;
    private static final int Q = 12;
    private float[] mTemp = new float[15];

    // triangle vertex positions
    private float[] mPoints;

    private KdNode mRoot;
    private Triangle[] mTriangles;

    /**
     * Creates a new TriangleKdTree, which contains the triangles defined by points and indices.
     *
     * @param points     Triangle vertex positions
     * @param indices    Triangle vertex indices.
     */
    public TriangleKdTree(float[] points, int[] indices) {
        mPoints = points;
        mTriangles = new Triangle[indices.length / 3];
        for (int i = 0; i < mTriangles.length; i++) {
            mTriangles[i] = new Triangle(indices[i * 3], indices[i * 3 + 1], indices[i * 3 + 2]);
        }

        mRoot = new KdNode(0, mTriangles.length);
    }

    /**
     * Tests the specified ray for intersection with this TriangleKdTree. The hit result is stored
     * in the passed result object.
     *
     * @param ray       Ray used for hit testing
     * @param result    Hit test result object
     */
    public void hitTest(Ray ray, HitTestResult result) {
        result.distanceSqr = Float.MAX_VALUE;
        mRoot.hitTest(ray, result);
    }

    /**
     * A TriangleKdTree node. Can be either an intermediate node with two sub nodes or a leaf with
     * no sub nodes but up to BUCKET_SIZE triangles.
     */
    private class KdNode {
        private BoundingBox mBounds;
        private KdNode mLeft;
        private KdNode mRight;
        private int mStart;
        private int mEnd;

        /**
         * Creates a new tree node containing the specified triangles. If the number of triangles is
         * greater than BUCKET_SIZE, they are split in two sub nodes.
         */
        private KdNode(int start, int end) {
            mStart = start;
            mEnd = end;

            // compute bounding box of this node
            computeBounds(start, end);

            if (end - start > BUCKET_SIZE) {
                // split triangles in sub nodes
                split();
            }
        }

        /**
         * Tests the specified ray for intersection with this node.
         */
        private void hitTest(Ray ray, HitTestResult result) {
            float d = mBounds.computeHitDistanceSqr(ray);
            if (d == Float.MAX_VALUE) {
                // ray does not intersect this node
                return;
            }

            if (!isLeaf()) {
                // test sub nodes for hit
                mLeft.hitTest(ray, result);
                if (result.distanceSqr == Float.MAX_VALUE) {
                    // left sub node is not hit by ray, return result for right sub node
                    mRight.hitTest(ray, result);

                } else {
                    // left sub node is hit by ray, test if right sub node can return a closer hit
                    d = mRight.mBounds.computeHitDistanceSqr(ray);
                    if (d < result.distanceSqr) {
                        // bounding box hit is closer, run full hit test
                        mRight.hitTest(ray, result);
                    }
                }
            } else {
                // this node is a leaf, test triangles for hit
                for (int i = mStart; i < mEnd; i++) {
                    mTriangles[i].hitTest(ray, result);
                }
            }
        }

        /**
         * Returns true if this node is a leaf.
         */
        private boolean isLeaf() {
            return mLeft == null;
        }

        /**
         * Determines the longest axis of the bounding boy and splits this node by this axis.
         */
        private void split() {
            float szX = mBounds.maxX - mBounds.minX;
            float szY = mBounds.maxY - mBounds.minY;
            float szZ = mBounds.maxZ - mBounds.minZ;

            if (szX > szY && szX > szZ) {
                // sort along x-axis
                Arrays.sort(mTriangles, mStart, mEnd, X_COMPARATOR);
            } else if (szY > szX && szY > szZ) {
                // sort along by y-axis
                Arrays.sort(mTriangles, mStart, mEnd, Y_COMPARATOR);
            } else {
                // sort along by z-axis
                Arrays.sort(mTriangles, mStart, mEnd, Z_COMPARATOR);
            }

            // create left and right sub nodes
            int startLeft = mStart;
            int endLeft = mStart + (mEnd - mStart) / 2;
            int startRight = endLeft;
            int endRight = mEnd;
            mLeft = new KdNode(startLeft, endLeft);
            mRight = new KdNode(startRight, endRight);
        }

        /**
         * Computes the node's bounding box from the specified triangles.
         */
        private void computeBounds(int start, int end) {
            // put first triangle in bounding box
            mBounds = new BoundingBox(mPoints, mTriangles[start].mOff0);
            mBounds.addPoint(mPoints, mTriangles[start].mOff1);
            mBounds.addPoint(mPoints, mTriangles[start].mOff2);

            // put remaining triangles in bounding box
            for (int i = start + 1; i < end; i++) {
                mBounds.addPoint(mPoints, mTriangles[i].mOff0);
                mBounds.addPoint(mPoints, mTriangles[i].mOff1);
                mBounds.addPoint(mPoints, mTriangles[i].mOff2);
            }
        }
    }

    /**
     * A triangle inside a TriangleKdTree.
     */
    private class Triangle {
        private int mOff0;
        private int mOff1;
        private int mOff2;

        /**
         * Creates a triangle with the specified vertex indices.
         */
        private Triangle(int idx0, int idx1, int idx2) {
            mOff0 = idx0 * 3;
            mOff1 = idx1 * 3;
            mOff2 = idx2 * 3;
        }

        /**
         * Returns the minimum x-coordinate of this triangle. Is needed for sorting during tree
         * construction.
         */
        private float getMinX() {
            return GlMath.min3(mPoints[mOff0], mPoints[mOff1], mPoints[mOff2]);
        }

        /**
         * Returns the minimum y-coordinate of this triangle. Is needed for sorting during tree
         * construction.
         */
        private float getMinY() {
            return GlMath.min3(mPoints[mOff0 + 1], mPoints[mOff1 + 1], mPoints[mOff2 + 1]);
        }

        /**
         * Returns the minimum z-coordinate of this triangle. Is needed for sorting during tree
         * construction.
         */
        private float getMinZ() {
            return GlMath.min3(mPoints[mOff0 + 2], mPoints[mOff1 + 2], mPoints[mOff2 + 2]);
        }

        /**
         * Tests whether this triangle intersects with the given ray.
         */
        private void hitTest(Ray ray, HitTestResult result) {
            // Attention: Magic is about to happen...
            GlMath.subtractVector(mTemp, E1, mPoints, mOff1, mPoints, mOff0);
            GlMath.subtractVector(mTemp, E2, mPoints, mOff2, mPoints, mOff0);
            GlMath.subtractVector(mTemp, S, ray.origin, 0, mPoints, mOff0);
            GlMath.crossVector(mTemp, P, ray.direction, 0, mTemp, E2);
            GlMath.crossVector(mTemp, Q, mTemp, S, mTemp, E1);

            float f = 1.0f / GlMath.dotVector(mTemp, P, mTemp, E1);
            float t = f * GlMath.dotVector(mTemp, Q, mTemp, E2);
            float u = f * GlMath.dotVector(mTemp, P, mTemp, S);
            float v = f * GlMath.dotVector(mTemp, Q, ray.direction, 0);

            if (u >= 0 && v >= 0 && u + v <= 1 && t >= 0 && t * t < result.distanceSqr) {
                // Ray hits triangle, compute hit position and face normal
                result.distance = t;
                result.distanceSqr = t * t;
                result.point[0] = ray.origin[0] + ray.direction[0] * t;
                result.point[1] = ray.origin[1] + ray.direction[1] * t;
                result.point[2] = ray.origin[2] + ray.direction[2] * t;
                result.normal[0] = mTemp[E1 + 1] * mTemp[E2 + 2] - mTemp[E1 + 2] * mTemp[E2 + 1];
                result.normal[1] = mTemp[E1 + 2] * mTemp[E2] - mTemp[E1] * mTemp[E2 + 2];
                result.normal[2] = mTemp[E1] * mTemp[E2 + 1] - mTemp[E1 + 1] * mTemp[E2];
                GlMath.normalize(result.normal, 0);
            }
        }
    }

    /*
     * Comparator for sorting triangles along their x-axis.
     */
    private static final Comparator<Triangle> X_COMPARATOR = new Comparator<Triangle>() {
        @Override
        public int compare(Triangle lhs, Triangle rhs) {
            float lmx = lhs.getMinX();
            float rmx = rhs.getMinX();
            if (lmx < rmx) {
                return -1;
            } else if (lmx > rmx) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    /*
     * Comparator for sorting triangles along their y-axis.
     */
    private static final Comparator<Triangle> Y_COMPARATOR = new Comparator<Triangle>() {
        @Override
        public int compare(Triangle lhs, Triangle rhs) {
            float lmy = lhs.getMinY();
            float rmy = rhs.getMinY();
            if (lmy < rmy) {
                return -1;
            } else if (lmy > rmy) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    /*
     * Comparator for sorting triangles along their z-axis.
     */
    private static final Comparator<Triangle> Z_COMPARATOR = new Comparator<Triangle>() {
        @Override
        public int compare(Triangle lhs, Triangle rhs) {
            float lmz = lhs.getMinZ();
            float rmz = rhs.getMinZ();
            if (lmz < rmz) {
                return -1;
            } else if (lmz > rmz) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    /**
     * Result object for ray hit tests against a kd tree.
     */
    public static class HitTestResult {
        /** Distance from ray origin to hit point. */
        public float distance;
        /** Squared distance from ray origin to hit point. */
        public float distanceSqr;
        /** Hit point coordinates. */
        public float[] point = new float[3];
        /** Hit point normal x component. */
        public float[] normal = new float[3];

        /**
         * Returns true if the hit test was positive, i.e. the ray hit a triangle.
         *
         * @return true if the hit test was positive, i.e. the ray hit a triangle
         */
        public boolean isHit() {
            return distanceSqr < Float.MAX_VALUE;
        }
    }
}
