package com.github.fabmax.lightgl.scene;

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glDrawElements;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.github.fabmax.lightgl.GfxState;
import com.github.fabmax.lightgl.Shader;
import com.github.fabmax.lightgl.ShaderAttributeBinder;
import com.github.fabmax.lightgl.util.MeshFactory;

/**
 * A triangle mesh. Currently only triangle meshes are supported by LightGl.
 * 
 * @see MeshFactory
 * @author fabmax
 * 
 */
public class Mesh extends Node {

    // vertex attributes
    private ShaderAttributeBinder mPositionBinder;
    private ShaderAttributeBinder mNormalBinder;
    private ShaderAttributeBinder mTexCoordBinder;
    private ShaderAttributeBinder mColorBinder;

    // index buffer
    private ShortBuffer mIndexBuffer;
    private int mIndexBufferSize;

    // mesh material
    private Shader mMeshShader;

    /**
     * Constructs a new Mesh from the supplied data. A attribute binder for vertex positions is
     * created. If more attributes should be used set the binder using the corresponding setters.
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
     */
    public Mesh(ShortBuffer indexBuffer, FloatBuffer dataBuffer, int stride, int positionOffset) {
        mIndexBuffer = indexBuffer;
        mIndexBufferSize = indexBuffer.capacity();

        // create attribute binders
        mPositionBinder = ShaderAttributeBinder.createFloatBufferBinder(dataBuffer, 3, stride);
        mPositionBinder.setOffset(positionOffset);
    }

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
     * @param indexBuffer
     *            buffer with vertex indices of all triangles for this Mesh
     * @param dataBuffer
     *            buffer with the vertex data for this Mesh
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
    public Mesh(ShortBuffer indexBuffer, FloatBuffer dataBuffer, int stride, int positionOffset,
            int normalOffset, int uvOffset, int colorOffset) {
        mIndexBufferSize = indexBuffer.capacity();
        mIndexBuffer = indexBuffer;

        // create attribute binders, offsets and stride must be converted to byte indices (1 float = 4 bytes)
        mPositionBinder = ShaderAttributeBinder.createFloatBufferBinder(dataBuffer, 3, stride);
        mPositionBinder.setOffset(positionOffset);
        mNormalBinder = ShaderAttributeBinder.createFloatBufferBinder(dataBuffer, 3, stride);
        mNormalBinder.setOffset(normalOffset);
        mTexCoordBinder = ShaderAttributeBinder.createFloatBufferBinder(dataBuffer, 2, stride);
        mTexCoordBinder.setOffset(uvOffset);
        mColorBinder = ShaderAttributeBinder.createFloatBufferBinder(dataBuffer, 3, stride);
        mColorBinder.setOffset(colorOffset);
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
     * Returns the binder for vertex positions.
     * 
     * @return the binder for vertex positions
     */
    public ShaderAttributeBinder getVertexPositionBinder() {
        return mPositionBinder;
    }

    /**
     * Sets the binder for vertex positions.
     * 
     * @param positionBinder the binder for vertex positions
     */
    public void setVertexPositionBinder(ShaderAttributeBinder positionBinder) {
        mPositionBinder = positionBinder;
    }

    /**
     * Returns the binder for vertex normals.
     * 
     * @return the binder for vertex normals
     */
    public ShaderAttributeBinder getVertexNormalBinder() {
        return mNormalBinder;
    }

    /**
     * Sets the binder for vertex normals.
     * 
     * @param normalBinder the binder for vertex normals
     */
    public void setVertexNormalBinder(ShaderAttributeBinder normalBinder) {
        mNormalBinder = normalBinder;
    }

    /**
     * Returns the binder for vertex texture coordinates.
     * 
     * @return the binder for vertex texture coordinates
     */
    public ShaderAttributeBinder getVertexTexCoordBinder() {
        return mTexCoordBinder;
    }

    /**
     * Sets the binder for vertex texture coordinates.
     * 
     * @param texCoordBinder the binder for vertex texture coordinates
     */
    public void setVertexTexCoordBinder(ShaderAttributeBinder texCoordBinder) {
        mTexCoordBinder = texCoordBinder;
    }

    /**
     * Returns the binder for vertex colors.
     * 
     * @return the binder for vertex colors
     */
    public ShaderAttributeBinder getVertexColorBinder() {
        return mColorBinder;
    }

    /**
     * Sets the binder for vertex colors.
     * 
     * @param colorBinder the binder for vertex positions
     */
    public void setVertexColorBinder(ShaderAttributeBinder colorBinder) {
        mColorBinder = colorBinder;
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
        if (shader != null) {
            // bind vertex buffer
            shader.bindMesh(this);
            // draw triangles
            glDrawElements(GL_TRIANGLES, mIndexBufferSize, GL_UNSIGNED_SHORT, mIndexBuffer);
        }
    }

}
