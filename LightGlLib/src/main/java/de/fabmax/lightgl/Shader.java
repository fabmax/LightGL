package de.fabmax.lightgl;

import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import de.fabmax.lightgl.scene.Mesh;

/**
 * Base class for custom shader implementations.
 * 
 * @author fabmax
 * 
 */
public abstract class Shader extends GlObject {

    public static final int ATTRIBUTE_POSITIONS = 0;
    public static final int ATTRIBUTE_NORMALS = 1;
    public static final int ATTRIBUTE_TEXTURE_COORDS = 2;
    public static final int ATTRIBUTE_COLORS = 3;

    private final ShaderManager mShaderMgr;
    
    /** Shader attribute pointers */
    protected int[] mVertexAttributes;
    
    /**
     * Initializes the shader attributes.
     */
    public Shader(ShaderManager shaderMgr) {
        mShaderMgr = shaderMgr;
        mShaderMgr.registerShader(this);
        
        mVertexAttributes = new int[4];
        for (int i = 0; i < mVertexAttributes.length; i++) {
            mVertexAttributes[i] = -1;
        }
    }

    /**
     * Call this to load the shader program. Is called automatically when the shader is bound for
     * the first time and it is not already loaded.
     * 
     * @param shaderMgr
     *            the shader manager
     */
    public abstract void loadShader(ShaderManager shaderMgr);

    /**
     * Is called if the shader is bound. Implementations should update all their shader uniforms
     * here.
     * 
     * @param state
     *            Current graphics engine state
     */
    protected abstract void onBind(GfxState state);
    
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
        
        int handle = getGlHandle();
        mVertexAttributes[attrib] = glGetAttribLocation(handle, attribName);
    }

    /**
     * Disables the specified attribute for this shader. This method is called by concrete Shader
     * implementations to set the vertex attributes used by the implementation.
     * 
     * @param attrib
     *            the attribute to disable
     */
    protected void disableAttribute(int attrib) {
        if(attrib < 0 || attrib > mVertexAttributes.length) {
            throw new IllegalArgumentException("Illegal vertex attribute specified");
        }
        
        mVertexAttributes[attrib] = -1;
    }

    /**
     * Binds the specified Mesh as input to this shader. The mesh's ShaderAttributBinders will
     * be bound to the Shader attributes.
     * 
     * @param mesh
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
    
    /**
     * Disables all vertex attribute arrays that where bound with the last Mesh.
     */
    public void unbindMesh() {
        int ptr = mVertexAttributes[ATTRIBUTE_POSITIONS];
        if (ptr != -1) {
            glDisableVertexAttribArray(ptr);
        }
        ptr = mVertexAttributes[ATTRIBUTE_NORMALS];
        if (ptr != -1) {
            glDisableVertexAttribArray(ptr);
        }
        ptr = mVertexAttributes[ATTRIBUTE_TEXTURE_COORDS];
        if (ptr != -1) {
            glDisableVertexAttribArray(ptr);
        }
        ptr = mVertexAttributes[ATTRIBUTE_COLORS];
        if (ptr != -1) {
            glDisableVertexAttribArray(ptr);
        }
    }

    /**
     * Deletes the shader program in GPU memory.
     */
    @Override
    public void delete() {
        if (isValid()) {
            mShaderMgr.deleteShader(this);
        }
    }
}
