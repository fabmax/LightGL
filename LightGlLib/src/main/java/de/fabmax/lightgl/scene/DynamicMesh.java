package de.fabmax.lightgl.scene;


import android.util.Log;

import java.nio.ShortBuffer;

import de.fabmax.lightgl.LightGlContext;
import de.fabmax.lightgl.ShaderAttributeBinder;
import de.fabmax.lightgl.util.BufferHelper;
import de.fabmax.lightgl.util.IntList;
import de.fabmax.lightgl.util.MeshBuilder;
import de.fabmax.lightgl.util.MeshData;
import de.fabmax.lightgl.util.PackedVertexBuffer;

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glDrawElements;

/**
 * A dynamic (modifiable) mesh.
 *
 * @author fth
 */
public class DynamicMesh extends Mesh {

    private static final String TAG = "DynamicMesh";

    protected final int mGlPrimitiveType;

    protected final ShortBuffer mIndexBuffer;
    protected final PackedVertexBuffer mVertexBuffer;

    protected int mVertIndex = 0;
    protected int mElementIdxIndex = 0;

    public DynamicMesh(int maxVertices, int maxIndices, boolean hasNormals, boolean hasTexCoords, boolean hasColors) {
    	this(maxVertices, maxIndices, hasNormals, hasTexCoords, hasColors, GL_TRIANGLES);
    }

    public DynamicMesh(int maxVertices, int maxIndices, boolean hasNormals, boolean hasTexCoords, boolean hasColors,
                       int primitiveType) {
        mGlPrimitiveType = primitiveType;
        
        // determine index buffer type based on mesh size
        mIndexBuffer = BufferHelper.createShortBuffer(maxIndices);
        mVertexBuffer = new PackedVertexBuffer(maxVertices, hasNormals, hasTexCoords, hasColors);

        // create shader attribute binders
        ShaderAttributeBinder binder = ShaderAttributeBinder
                .createFloatBufferBinder(mVertexBuffer.data, 3, mVertexBuffer.strideBytes);
        binder.setOffset(mVertexBuffer.offsetPositions);
        setVertexPositionBinder(binder);

        if (hasNormals) {
            binder = ShaderAttributeBinder.createFloatBufferBinder(mVertexBuffer.data, 3, mVertexBuffer.strideBytes);
            binder.setOffset(mVertexBuffer.offsetNormals);
            setVertexNormalBinder(binder);
        }

        if (hasTexCoords) {
            binder = ShaderAttributeBinder.createFloatBufferBinder(mVertexBuffer.data, 2, mVertexBuffer.strideBytes);
            binder.setOffset(mVertexBuffer.offsetTexCoords);
            setVertexTexCoordBinder(binder);
        }

        if (hasColors) {
            binder = ShaderAttributeBinder.createFloatBufferBinder(mVertexBuffer.data, 4, mVertexBuffer.strideBytes);
            binder.setOffset(mVertexBuffer.offsetColors);
            setVertexColorBinder(binder);
        }

        clear();
    }

    public DynamicMesh(MeshData meshData) {
        this(meshData, GL_TRIANGLES);
    }

    public DynamicMesh(MeshData meshData, int primitiveType) {
        this(meshData.positions.length / 3,
                meshData.indices.length,
                meshData.hasNormals(),
                meshData.hasTextureCoordinates(),
                meshData.hasColors(),
                primitiveType);

        updateMeshData(meshData);
    }

    public PackedVertexBuffer getBuffer() {
        return mVertexBuffer;
    }

    public int addVertex(float[] position, int posOff, float[] color, int colOff) {
        mVertexBuffer.setVertexAt(mVertIndex, position, posOff, null, 0, null, 0, color, colOff);
        return mVertIndex++;
    }

    public int addVertex(float[] position, int posOff, float[] normal, int normalOff, float[] texCoord, int texOff) {
        mVertexBuffer.setVertexAt(mVertIndex, position, posOff, normal, normalOff, texCoord, texOff, null, 0);
        return mVertIndex++;
    }

    public void addElementIndex(int elementIndex) {
        mIndexBuffer.limit(mElementIdxIndex + 1);
        mIndexBuffer.put(mElementIdxIndex, (short) elementIndex);
        mElementIdxIndex++;
    }

    public void clear() {
        mIndexBuffer.limit(0);
        mVertIndex = 0;
        mElementIdxIndex = 0;
    }

    public int size() {
        return mIndexBuffer.limit();
    }

    public void updateMeshData(MeshBuilder builder) {
        IntList indices = builder.getIndices();
        int len = indices.size();
        if (len > mIndexBuffer.capacity()) {
            len  = mIndexBuffer.capacity();
            Log.w(TAG, "Supplied mesh data does not fit in this DynamicMesh, truncating...");
        }
        mIndexBuffer.limit(len);
        indices.copyToBuffer(mIndexBuffer);
        mIndexBuffer.rewind();
        mElementIdxIndex = len;

        mVertexBuffer.pack(builder);
        mVertIndex = builder.getVertexCount();
    }

    public void updateMeshData(MeshData data) {
        int len = data.indices.length;
        if (len > mIndexBuffer.capacity()) {
            len  = mIndexBuffer.capacity();
            Log.w(TAG, "Supplied mesh data does not fit in this DynamicMesh, truncating...");
        }
        mIndexBuffer.limit(len);
        //mIndexBuffer.put(data.indices, 0, len);
        for (int i = 0; i < data.indices.length; i++) {
            mIndexBuffer.put((short) data.indices[i]);
        }
        mIndexBuffer.rewind();
        mElementIdxIndex = len;

        mVertexBuffer.pack(data);
        mVertIndex = data.getVertexCount();
    }

    @Override
    protected void drawElements(LightGlContext context) {
        glDrawElements(mGlPrimitiveType, mIndexBuffer.remaining(), GL_UNSIGNED_SHORT, mIndexBuffer);
    }
}
