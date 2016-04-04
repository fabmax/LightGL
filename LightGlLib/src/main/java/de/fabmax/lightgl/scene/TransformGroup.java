package de.fabmax.lightgl.scene;

import android.opengl.Matrix;

import de.fabmax.lightgl.GfxState;
import de.fabmax.lightgl.LightGlContext;

/**
 * A TransformGroup is a {@link Group} that applies a transformation to its children.
 * 
 * @author fabmax
 * 
 */
public class TransformGroup extends Group {

    // transformation matrix
    protected final float[] mTransformationM = new float[16];
    // inverse transformation matrix, only updated by getInverseTransform()
    protected final float[] mInverseTransformationM = new float[16];
    // inverse transform update matrix
    protected boolean mTransformDirty = false;

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
     * Returns the inverse transformation matrix of this group.
     *
     * @return the inverse transformation matrix of this group
     */
    public float[] getInverseTransformation() {
        if (mTransformDirty) {
            Matrix.invertM(mInverseTransformationM, 0, mTransformationM, 0);
            mTransformDirty = false;
        }
        return mInverseTransformationM;
    }

    /**
     * Copies the specified transformation matrix to this transformation matrix.
     *
     * @param transformation
     *            the transformation matrix to copy
     */
    public void setTransformation(float[] transformation) {
        System.arraycopy(transformation, 0, mTransformationM, 0, 16);
        mTransformDirty = true;
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
        mTransformDirty = true;
    }

    /**
     * Resets the transformation matrix to an identity matrix.
     */
    public void resetTransform() {
        Matrix.setIdentityM(mTransformationM, 0);
        Matrix.setIdentityM(mInverseTransformationM, 0);
        mTransformDirty = false;
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
        mTransformDirty = true;
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
        mTransformDirty = true;
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
        mTransformDirty = true;
    }

    /**
     * Applies this TransformGroup's transform matrix to the given {@link de.fabmax.lightgl.GfxState}.
     * The existing model matrix IS NOT pushed before it is modified.
     *
     * @param state    The GfxState whose model matrix is to be changed
     */
    public void applyTransform(GfxState state) {
        // apply transformation
        Matrix.multiplyMM(mTemp1, 0, state.getModelMatrix(), 0, mTransformationM, 0);
        state.setModelMatrix(mTemp1);
    }

    /**
     * @see Group#render(de.fabmax.lightgl.LightGlContext)
     */
    @Override
    public void render(LightGlContext context) {
        // push current model matrix
        context.getState().pushModelMatrix();

        applyTransform(context.getState());

        // render children
        super.render(context);

        // restore previous model matrix
        context.getState().popModelMatrix();
    }

}
