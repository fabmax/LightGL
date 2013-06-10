package de.fabmax.lightgl;

import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;

import de.fabmax.lightgl.scene.Mesh;

/**
 * Base class for custom shader implementations.
 * 
 * @author fabmax
 * 
 */
public abstract class Shader {

    public static final int ATTRIBUTE_POSITIONS = 0;
    public static final int ATTRIBUTE_NORMALS = 1;
    public static final int ATTRIBUTE_TEXTURE_COORDS = 2;
    public static final int ATTRIBUTE_COLORS = 3;

    /** Shader attribute pointers */
    protected int[] mVertexAttributes;
    
    /**
     * Initializes the shader attributes.
     */
    public Shader() {
        mVertexAttributes = new int[4];
        for (int i = 0; i < mVertexAttributes.length; i++) {
            mVertexAttributes[i] = -1;
        }
    }
    
    /**
     * Returns the GL shader handle of this shader.
     * 
     * @return the GL shader handle of this shader
     */
    public abstract int getShaderHandle();
    
    /**
     * Is called if the shader is bound. Implementations should update all their shader uniforms
     * here.
     * 
     * @param state
     *            Current graphics engine state
     */
    public abstract void onBind(GfxState state);
    
    /**
     * Is called if the MVP matrix has changed. Implementations should update their transform matrix
     * uniforms here.
     * 
     * @param state
     *            Current graphics engine state
     */
    public abstract void onMatrixUpdate(GfxState state);
    
    /**
     * Enables the specified attribute for this shader. This method is called by concrete Shader
     * implementations to set the vertex attributes used by the implementation.
     * 
     * @param attrib
     *            the attribute to enable
     * @param attribName
     *            name of the attribute in shader code
     */
    protected void enableAttribute(int attrib, String attribName) {
        if(attrib < 0 || attrib > mVertexAttributes.length) {
            throw new IllegalArgumentException("Illegal vertex attribute specified");
        }
        
        int handle = getShaderHandle();
        mVertexAttributes[attrib] = glGetAttribLocation(handle, attribName);
    }

    /**
     * Binds the specified Mesh as input to this shader. The mesh's ShaderAttributBinders will
     * be bound to the Shader attributes.
     * 
     * @param Mesh
     *            Mesh to use as input for shader execution
     */
    public void bindMesh(Mesh mesh) {
        int ptr = mVertexAttributes[ATTRIBUTE_POSITIONS];
        if (ptr != -1) {
            ShaderAttributeBinder binder = mesh.getVertexPositionBinder();
            if (binder != null && binder.bindAttribute(ptr)) {
                glEnableVertexAttribArray(ptr);
            }
        }
        ptr = mVertexAttributes[ATTRIBUTE_NORMALS];
        if (ptr != -1) {
            ShaderAttributeBinder binder = mesh.getVertexNormalBinder();
            if (binder != null && binder.bindAttribute(ptr)) {
                glEnableVertexAttribArray(ptr);
            }
        }
        ptr = mVertexAttributes[ATTRIBUTE_TEXTURE_COORDS];
        if (ptr != -1) {
            ShaderAttributeBinder binder = mesh.getVertexTexCoordBinder();
            if (binder != null && binder.bindAttribute(ptr)) {
                glEnableVertexAttribArray(ptr);
            }
        }
        ptr = mVertexAttributes[ATTRIBUTE_COLORS];
        if (ptr != -1) {
            ShaderAttributeBinder binder = mesh.getVertexColorBinder();
            if (binder != null && binder.bindAttribute(ptr)) {
                glEnableVertexAttribArray(ptr);
            }
        }
    }
}
