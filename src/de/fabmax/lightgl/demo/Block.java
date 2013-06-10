package de.fabmax.lightgl.demo;

import java.nio.FloatBuffer;
import java.util.Random;

import de.fabmax.lightgl.BoundingBox;
import de.fabmax.lightgl.Ray;
import de.fabmax.lightgl.util.GlMath;

public class Block {
    private static final Random RAND = new Random();

    public static final int MAX_DURATION = 10000;
    public static final float MIN_HEIGHT = 0.4f;
    public static final float HEIGHT_RANGE = 5.0f;

    // private BlockAnimator mGrid;
    private FloatBuffer mVerts;
    private int mBufferOffset;
    private BoundingBox mBlockBounds;

    private float mHeight = MIN_HEIGHT;
    private float mHeightStart = MIN_HEIGHT;
    private float mHeightEnd = MIN_HEIGHT;
    private long mAnimStart = 0;
    private long mAnimDuration = 0;

    /**
     * Creates a block for the specified grid.
     * 
     * @param grid
     *            parent block grid
     * @param bufferOffset
     *            offset in the grid's vertex buffer
     */
    protected Block(BlockAnimator grid, int bufferOffset) {
        // mGrid = grid;
        mBufferOffset = bufferOffset;
        mVerts = grid.getVertexBuffer();

        // compute BoundingBox for this block
        mBlockBounds = new BoundingBox(mVerts.get(bufferOffset + 0), mVerts.get(bufferOffset + 1),
                mVerts.get(bufferOffset + 2));
        for (int i = bufferOffset + 3; i < bufferOffset + 60; i += 3) {
            mBlockBounds.addPoint(mVerts.get(i + 0), mVerts.get(i + 1), mVerts.get(i + 2));
        }
    }

    /**
     * Animate the height of this block to the specified value in the specified time.
     * 
     * @param height
     *            target block height
     * @param duration
     *            animation time in milliseconds
     */
    public void animateToHeight(float height, int duration) {
        if (height < MIN_HEIGHT) {
            height = MIN_HEIGHT;
        }
        mAnimStart = System.currentTimeMillis();
        mAnimDuration = duration;
        mHeightStart = mHeight;
        mHeightEnd = height;
    }

    /**
     * Computes the square distance from the {@link Ray} origin to this Block.
     * 
     * @see BoundingBox#computeHitDistanceSqr(Ray)
     * 
     * @param r
     *            Ray to test this block with
     * @return squared hit distance
     */
    public float computeHitDistanceSqr(Ray r) {
        return mBlockBounds.computeHitDistanceSqr(r);
    }

    /**
     * Interpolates the height of this block for the specified timestamp and returns the
     * corresponding color.
     * 
     * @param t
     *            current time
     */
    public int interpolateHeight(long t) {
        if (t >= mAnimStart + mAnimDuration) {
            mAnimStart = t;
            mAnimDuration = MAX_DURATION / 2 + RAND.nextInt(MAX_DURATION / 2);
            mHeightStart = mHeightEnd;
            mHeightEnd = RAND.nextFloat() * HEIGHT_RANGE + MIN_HEIGHT;
            mHeight = mHeightStart;

        } else {
            float p = (float) (t - mAnimStart) / mAnimDuration;
            mHeight = mHeightStart * (1.0f - p) + mHeightEnd * p;
        }

        float yl = mHeight - 0.1f;
        mVerts.put(mBufferOffset + 1, yl);
        mVerts.put(mBufferOffset + 10, yl);
        mVerts.put(mBufferOffset + 13, yl);
        mVerts.put(mBufferOffset + 22, yl);
        mVerts.put(mBufferOffset + 31, yl);
        mVerts.put(mBufferOffset + 34, yl);
        mVerts.put(mBufferOffset + 43, yl);
        mVerts.put(mBufferOffset + 46, yl);
        mVerts.put(mBufferOffset + 49, mHeight);
        mVerts.put(mBufferOffset + 52, mHeight);
        mVerts.put(mBufferOffset + 55, mHeight);
        mVerts.put(mBufferOffset + 58, mHeight);

        // update block bounds for new height
        mBlockBounds.setMaxY(mHeight);

        return getColor();
    }

    /**
     * Calculates the color value for the current block height.
     * 
     * @see GlMath#packedColor(float, float, float, float)
     * @see GlMath#packedHsvColor(float, float, float, float)
     * 
     * @return color corresponding to the height
     */
    private int getColor() {
        float normH = (mHeight - MIN_HEIGHT) / HEIGHT_RANGE;

        // colorful
        float hue = 300.0f * normH;
        float sat = 0.77f;
        float val = 0.89f;
        return GlMath.packedHsvColor(hue, sat, val, 1);

        // float hue = 196;
        // float sat = 0.77f;
        // float val = 0.89f * (normH / 2 + 0.5f);
        // return GlMath.packedHsvColor(hue, sat, val, 1);
    }

}
