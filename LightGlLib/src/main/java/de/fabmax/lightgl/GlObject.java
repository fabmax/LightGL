package de.fabmax.lightgl;

/**
 * GlObject is the base class for all Objects representing loaded stuff on the GPU. E.g. de.fabmax.lightgl.demo.textures,
 * VBOs, etc.
 * 
 * @author fabmax
 * 
 */
public abstract class GlObject {

    /**
     * The OpenGL object handle. This is a one-element array for easier calling of many GL
     * functions.
     */
    protected int[] mHandle = new int[1];

    /**
     * Creates a GlObject that has not yet assigned an OpenGL object handle. Notice that there is no
     * public setGlHandle() method as the handle should be handled with care. Sub-classes can use
     * the protected version or can write the protected field mHandle directly once they got their
     * OpenGL object handle.
     */
    public GlObject() {
        mHandle[0] = 0;
    }

    /**
     * Creates a GlObject referencing the specified OpenGL object.
     * 
     * @param handle
     *            OpenGL object handle
     */
    public GlObject(int handle) {
        mHandle[0] = handle;
    }

    /**
     * Returns the associated OpenGL object handle.
     * 
     * @return the OpenGL object handle associated to this GlObject
     */
    public int getGlHandle() {
        return mHandle[0];
    }

    /**
     * Sets the associated OpenGL object handle. This method can be called by sub-classes. Moreover
     * this method is called with 0 as argument by a corresponding Manager class after the OpenGL
     * context was recreated.
     * 
     * @param handle
     */
    protected void setGlHandle(int handle) {
        mHandle[0] = handle;
    }

    /**
     * Deletes the corresponding OpenGL object. Sub.classes must implement this to call the matching
     * glDelete* function.
     */
    public abstract void delete();

    /**
     * Returns true if this GlObject holds a valid OpenGL object handle; false otherwise.
     * 
     * @return true if this GlObject holds a valid OpenGL object handle; false otherwise
     */
    public boolean isValid() {
        return mHandle[0] != 0;
    }

}
