package de.fabmax.lightgl.scene;

import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_INT;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glDeleteBuffers;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glGenBuffers;

import java.nio.Buffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import android.util.Log;

import de.fabmax.lightgl.GfxState;
import de.fabmax.lightgl.Shader;
import de.fabmax.lightgl.ShaderAttributeBinder;
import de.fabmax.lightgl.util.MeshFactory;

/**
 * A triangle mesh. Currently only triangle meshes are supported by LightGl.
 * 
 * @see MeshFactory
 * @author fabmax
 * 
 */
public class Mesh extends Node {

    private static final String TAG = "Mesh";

    // vertex attributes
    private ShaderAttributeBinder mPositionBinder;
    private ShaderAttributeBinder mNormalBinder;
    private ShaderAttributeBinder mTexCoordBinder;
    private ShaderAttributeBinder mColorBinder;

    // index buffer
    private int mIndexBufferHandle;
    private int mIndexBufferSize;
    private int mIndexBufferType;

    // mesh material
    private Shader mMeshShader;

    /**
     * Constructs a Mesh with the specified indices and attribute binders. A Mesh can only be
     * created with a valid GL context.
     * 
     * @see MeshFactory
     * 
     * @param indexBuffer
     *            Buffer with vertex indices. Must be an IntBuffer or a ShortBuffer.
     * @param positions
     *            Attribute binder for vertex positions. Must not be null.
     * @param normals
     *            Attribute binder for vertex normals. Can be null.
     * @param texCoords
     *            Attribute binder for vertex texture coordinates. Can be null.
     * @param colors
     *            Attribute binder for vertex colors. Can be null.
     */
    public Mesh(Buffer indexBuffer, ShaderAttributeBinder positions, ShaderAttributeBinder normals,
            ShaderAttributeBinder texCoords, ShaderAttributeBinder colors) {
        createIndexBufferObject(indexBuffer);

        if (positions == null) {
            throw new IllegalArgumentException("Vertex positions attribute binder is null");
        }
        
        mPositionBinder = positions;
        mNormalBinder = normals;
        mTexCoordBinder = texCoords;
        mColorBinder = colors;
    }

    /**
     * Creates a GL buffer object from the given indexBuffer.
     * 
     * @param indexBuffer
     *            Mesh index buffer. Must either be an IntBuffer or a ShortBuffer.
     */
    private void createIndexBufferObject(Buffer indexBuffer) {
        int sizeInBytes = 0;
        if (indexBuffer instanceof ShortBuffer) {
            sizeInBytes = indexBuffer.capacity() * 2;
            mIndexBufferType = GL_UNSIGNED_SHORT;
        } else if (indexBuffer instanceof IntBuffer) {
            sizeInBytes = indexBuffer.capacity() * 4;
            mIndexBufferType = GL_UNSIGNED_INT;
        } else {
            throw new IllegalArgumentException(
                    "indexBuffer must either be an IntBuffer or a ShortBuffer");
        }
        mIndexBufferSize = indexBuffer.capacity();

        // create buffer object
        int[] buf = new int[1];
        glGenBuffers(1, buf, 0);
        mIndexBufferHandle = buf[0];

        // copy buffer data
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mIndexBufferHandle);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeInBytes, indexBuffer, GL_STATIC_DRAW);
    }
    
    /**
     * Deletes all buffers associated with this mesh.
     */
    public void delete() {
        if (mPositionBinder != null) {
            mPositionBinder.delete();
            mPositionBinder = null;
        }
        if (mNormalBinder != null) {
            mNormalBinder.delete();
            mNormalBinder = null;
        }
        if (mTexCoordBinder!= null) {
            mTexCoordBinder.delete();
            mTexCoordBinder = null;
        }
        if (mColorBinder != null) {
            mColorBinder.delete();
            mColorBinder = null;
        }
        if (mIndexBufferHandle != 0) {
            int[] buf = new int[] { mIndexBufferHandle };
            glDeleteBuffers(1, buf, 0);
            mIndexBufferHandle = 0;
            mIndexBufferSize = 0;
            mIndexBufferType = 0;
        }
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
     * @param positionBinder
     *            the binder for vertex positions
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
     * @param normalBinder
     *            the binder for vertex normals
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
     * @param texCoordBinder
     *            the binder for vertex texture coordinates
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
     * @param colorBinder
     *            the binder for vertex positions
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
            // bind this mesh as input to the used shader
            shader.bindMesh(this);
            // draw triangles
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mIndexBufferHandle);
            glDrawElements(GL_TRIANGLES, mIndexBufferSize, mIndexBufferType, 0);
            shader.unbindMesh();
        } else {
            Log.w(TAG, "Failed rendering mesh: null material");
        }
    }

}
