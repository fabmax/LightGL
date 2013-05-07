package com.github.fabmax.lightgl.scene;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glGenBuffers;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.github.fabmax.lightgl.GfxState;
import com.github.fabmax.lightgl.Shader;
import com.github.fabmax.lightgl.util.MeshFactory;

/**
 * A triangle mesh. Currently only triangle meshes are supported by LightGl.
 * 
 * @see MeshFactory
 * @author fabmax
 * 
 */
public class Mesh extends Node {

    // vertex data buffer
    private int mVertexBufferHandle = -1;
    private int mVertexBufferStride = 0;
    private int mPositionOffset = 0;
    private int mNormalOffset = 0;
    private int mUvOffset = 0;
    private int mColorOffset = 0;

    // index buffer
    private int mIndexBufferHandle = -1;
    private int mIndexBufferSize = 0;
    
    // mesh material
    private Shader mMeshShader;
    
    /**
     * Constructs a new Mesh from the supplied data. The data buffer does not have to contain data
     * for all supported vertex attributes (positions, normals, texture coordinates and colors). The
     * required attributes depend on the used shader. Offset values for unused attributes are
     * ignored. Assuming a buffer with vertex positions and normals and a layout of [pos_x0, pos_y0,
     * pos_z0, n_x0, n_y0, n_z0, pos_x1, ...] the stride would be 6, positionOffset would be 0 and
     * normalOffset would be 3. Notice that the construction of a Mesh involves GL-calls, hence a
     * valid GL context has to be available.
     * 
     * @see MeshFactory
     * 
     * @param dataBuffer
     *            buffer with the vertex data for this Mesh
     * @param indexBuffer
     *            buffer with vertex indices of all triangles for this Mesh
     * @param stride
     *            data buffer element stride
     * @param positionOffset
     *            offset for vertex positions inside the buffer
     * @param normalOffset
     *            offset for vertex normals inside the buffer
     * @param uvOffset
     *            offset for vertex texture coordinates inside the buffer
     * @param colorOffset
     *            offset for vertex colors inside the buffer
     */
    public Mesh(FloatBuffer dataBuffer, ShortBuffer indexBuffer, int stride, int positionOffset, int normalOffset, int uvOffset, int colorOffset) {
        // set buffer layout, offsets and stride must be converted to byte indices (1 float = 4 bytes)
        mVertexBufferStride = stride * 4;
        mPositionOffset = positionOffset * 4;
        mNormalOffset = normalOffset * 4;
        mUvOffset = uvOffset * 4;
        mColorOffset = colorOffset * 4;
        mIndexBufferSize = indexBuffer.capacity();
        
        generateBuffers();
        
        // bind GL vertex buffer object
        glBindBuffer(GL_ARRAY_BUFFER, mVertexBufferHandle);
        // copy vertex buffer data (size is specified in bytes => 1 float = 4 bytes)
        glBufferData(GL_ARRAY_BUFFER, dataBuffer.capacity() * 4, dataBuffer, GL_STATIC_DRAW);
        
        // bind GL index buffer object
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mIndexBufferHandle);
        // copy index buffer data (size is specified in bytes => 1 short = 2 bytes)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * 2, indexBuffer, GL_STATIC_DRAW);
    }
    
    /**
     * Generates GL buffer objects for the vertex and index buffers.
     * 
     * @return pointer to the generated GL buffer
     */
    private void generateBuffers() {
        int[] buffers = new int[2];
        glGenBuffers(2, buffers, 0);
        mVertexBufferHandle = buffers[0];
        mIndexBufferHandle = buffers[1];
    }

    /**
     * Returns the shader used to render this mesh.
     * 
     * @return the shader used to render this mesh
     */
    public Shader getShader() {
        return mMeshShader;
    }

    /**
     * Sets the shader used to render this mesh.
     * 
     * @param shader
     *            the shader used to render this mesh
     */
    public void setShader(Shader shader) {
        this.mMeshShader = shader;
    }

    /**
     * Draws this mesh.
     * 
     * @see Node#render(GfxState)
     */
    @Override
    public void render(GfxState state) {
        // bind shader for this mesh
        state.bindShader(mMeshShader);

        // setup shader for mesh rendering, the active shader is not necessarily mMeshShader
        Shader shader = state.getBoundShader();
        if(shader != null) {
            // bind vertex buffer
            shader.bindVertexBuffer(mVertexBufferHandle);
            // setup vertex buffer layout
            shader.configureVertexAttribute(Shader.ATTRIBUTE_POSITIONS, 3, mPositionOffset, mVertexBufferStride);
            shader.configureVertexAttribute(Shader.ATTRIBUTE_NORMALS, 3, mNormalOffset, mVertexBufferStride);
            shader.configureVertexAttribute(Shader.ATTRIBUTE_TEXTURE_COORDS, 2, mUvOffset, mVertexBufferStride);
            shader.configureVertexAttribute(Shader.ATTRIBUTE_COLORS, 3, mColorOffset, mVertexBufferStride);
            
            // draw triangles
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mIndexBufferHandle);
            glDrawElements(GL_TRIANGLES, mIndexBufferSize, GL_UNSIGNED_SHORT, 0);
        }
    }

}
