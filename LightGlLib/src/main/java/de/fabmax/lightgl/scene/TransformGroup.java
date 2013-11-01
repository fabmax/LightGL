package de.fabmax.lightgl.scene;

import android.opengl.Matrix;

import de.fabmax.lightgl.GfxState;

/**
 * A TransformGroup is a {@link Group} that applies a transformation to its children.
 * 
 * @author fabmax
 * 
 */
public class TransformGroup extends Group {

    // transformation matrix
    protected final float[] mTransformationM = new float[16];

    // temp matrices needed for matrix computations
    private final float[] mTemp1 = new float[16];

    /**
     * Creates a new TransformGroup that applies no transformation at all.
     */
    public TransformGroup() {
        resetTransform();
    }

    /**
     * Returns the transformation matrix of this group.
     * 
     * @return the transformation matrix of this group
     */
    public float[] getTransformation() {
        return mTransformationM;
    }

    /**
     * Copies the specified transformation matrix to this transformation matrix.
     * 
     * @param transformation
     *            the transformation matrix to copy
     */
    public void setTransformation(float[] transformation) {
        System.arraycopy(transformation, 0, mTransformationM, 0, 16);
    }

    /**
     * Applies the specified transformation by multiplying it to the current transformation.
     * 
     * @param transformation
     *            the transformation to apply
     */
    public void applyTransformation(float[] transformation) {
        System.arraycopy(mTransformationM, 0, mTemp1, 0, 16);
        Matrix.multiplyMM(mTransformationM, 0, mTemp1, 0, transformation, 0);
    }
    
    /**
     * Resets the transformation matrix to an identity matrix.
     */
    public void resetTransform() {
        Matrix.setIdentityM(mTransformationM, 0);
    }

    /**
     * Rotates this TransformGroup around the specified axis by the specified angle in degrees.
     * 
     * @param angle
     *            rotation angle in degrees
     * @param x
     *            rotation axis X component
     * @param y
     *            rotation axis Y component
     * @param z
     *            rotation axis Z component
     */
    public void rotate(float angle, float x, float y, float z) {
        System.arraycopy(mTransformationM, 0, mTemp1, 0, 16);
        Matrix.rotateM(mTransformationM, 0, mTemp1, 0, angle, x, y, z);
    }

    /**
     * Translates this TransformGroup by the specified amount.
     * 
     * @param x
     *            X translation distance
     * @param y
     *            Y translation distance
     * @param z
     *            Z translation distance
     */
    public void translate(float x, float y, float z) {
        System.arraycopy(mTransformationM, 0, mTemp1, 0, 16);
        Matrix.translateM(mTransformationM, 0, mTemp1, 0, x, y, z);
    }

    /**
     * Scales this TransformGroup by the specified amount.
     * 
     * @param sX
     *            X scaling factor
     * @param sY
     *            Y scaling factor
     * @param sZ
     *            Z scaling factor
     */
    public void scale(float sX, float sY, float sZ) {
        System.arraycopy(mTransformationM, 0, mTemp1, 0, 16);
        Matrix.scaleM(mTransformationM, 0, mTemp1, 0, sX, sY, sZ);
    }

    /**
     * @see Group#render(GfxState)
     */
    @Override
    public void render(GfxState state) {
        // push current model matrix
        state.pushModelMatrix();
        
        // apply transformation
        Matrix.multiplyMM(mTemp1, 0, state.getModelMatrix(), 0, mTransformationM, 0);
        state.setModelMatrix(mTemp1);
        
        // render children
        super.render(state);
        
        // restore previous model matrix
        state.popModelMatrix();
    }

}
