package com.github.fabmax.lightgl;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glVertexAttribPointer;

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
     * Enables the specified attribute for this shader.
     * 
     * @param attrib
     *            the attribute to enable
     * @param attribName
     *            name of the attribute in shader code
     */
    public void enableAttribute(int attrib, String attribName) {
        if(attrib < 0 || attrib > mVertexAttributes.length) {
            throw new IllegalArgumentException("Illegal vertex attribute specified");
        }
        
        int handle = getShaderHandle();
        mVertexAttributes[attrib] = glGetAttribLocation(handle, attribName);
    }

    /**
     * Binds the specified GL vertex buffer object.
     * 
     * @param vertexBufPtr
     *            vertex buffer to bind
     */
    public void bindVertexBuffer(int vertexBufPtr) {
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufPtr);
    }

    /**
     * Configures a vertex attribute pointer and enables the attribute.
     * 
     * @param attrib
     *            the attribute to configure
     * @param offset
     *            vertex position offset in the GL buffer
     * @param stride
     *            buffer data stride
     */
    public void configureVertexAttribute(int attrib, int size, int offset, int stride) {
        if(attrib < 0 || attrib > mVertexAttributes.length) {
            throw new IllegalArgumentException("Illegal vertex attribute specified");
        }
        
        int ptr = mVertexAttributes[attrib];
        if (ptr != -1) {
            glVertexAttribPointer(ptr, size, GL_FLOAT, false, stride, offset);
            glEnableVertexAttribArray(ptr);
        }
    }
}
