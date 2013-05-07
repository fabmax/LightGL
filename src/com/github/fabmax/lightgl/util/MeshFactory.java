package com.github.fabmax.lightgl.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.github.fabmax.lightgl.scene.Mesh;

/**
 * The MeshFactory supplies a few helper functions to create meshes.
 * 
 * @see Mesh
 * @author fabmax
 * 
 */
public class MeshFactory {

    // temp buffer for vertex data during mesh construction
    private static FloatBuffer mTmpVertBuf;
    private static void ensureVertexBufferCapacity(int sz) {
        if(mTmpVertBuf == null || mTmpVertBuf.capacity() < sz) {
            // create a FlotBuffer with the specified capacity by allocating a byte buffer with
            // correct size (1 float = 4 bytes)
            mTmpVertBuf = ByteBuffer.allocateDirect(sz * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        }
        mTmpVertBuf.rewind();
    }

    // temp buffer for vertex indices during mesh construction
    private static ShortBuffer mTmpIdxBuf;
    private static void ensureIndexBufferCapacity(int sz) {
        if(mTmpIdxBuf == null || mTmpIdxBuf.capacity() < sz) {
            // create a FlotBuffer with the specified capacity by allocating a byte buffer with
            // correct size (1 short = 2 bytes)
            mTmpIdxBuf = ByteBuffer.allocateDirect(sz * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
        }
        mTmpIdxBuf.rewind();
    }
    
    /**
     * Creates a new Mesh with the specified attributes.
     * 
     * @param pos
     *            vertex positions (x, y, z)
     * @param norms
     *            vertex normals (x, y, z), can be null if normals are not needed
     * @param uvs
     *            vertex texture (u, v), coordinates, can be null if texture coordinates are not needed
     * @param colors
     *            vertex colors (r, g, b), can be null if vertex colors are not needed
     * @param indcs
     *            vertex indices
     * @return the created mesh
     */
    public static Mesh createMesh(float[] pos, float[] norms, float[] uvs, float[] colors, int[] indcs) {
        int posOffset = 0;
        int normOffset = 0;
        int uvOffset = 0;
        int colorOffset = 0;
        
        // determine elements per vertex, 3 elements needed for vertex position
        int elems = 3;
        if(norms != null) {
            // add normals to buffer
            normOffset = elems;
            elems += 3;
        }
        if(uvs != null) {
            // add texture coordinates to buffer
            uvOffset = elems;
            elems += 2;
        }
        if(colors != null) {
            // add colors to buffer
            colorOffset = elems;
            elems += 3;
        }
        
        // determine vertex buffer size
        int vertCnt = pos.length / 3;
        ensureVertexBufferCapacity(vertCnt * elems);
        // fill vertex buffer
        for (int i = 0, j = 0, k = 0; i < vertCnt; i++, j += 3, k += 2) {
            // vertex position
            mTmpVertBuf.put(pos, j, 3);
            if(norms != null) {
                // vertex normal
                mTmpVertBuf.put(norms, j, 3);
            }
            if(uvs != null) {
                // vertex normal
                mTmpVertBuf.put(uvs, k, 2);
            }
            if(colors != null) {
                // vertex normal
                mTmpVertBuf.put(colors, j, 3);
            }
        }
        mTmpVertBuf.rewind();
        
        // fill index buffer
        ensureIndexBufferCapacity(indcs.length);
        for (int i = 0; i < indcs.length; i++) {
            mTmpIdxBuf.put((short) indcs[i]);
        }
        mTmpIdxBuf.rewind();
        
        return new Mesh(mTmpVertBuf, mTmpIdxBuf, elems, posOffset, normOffset, uvOffset, colorOffset);
    }
    
    /**
     * Creates a color cube mesh.
     */
    public static Mesh createColorCube() {
        float[] pos = {
            // front
                -1.0f,  1.0f,  1.0f,
                -1.0f, -1.0f,  1.0f,
                 1.0f, -1.0f,  1.0f,
                 1.0f,  1.0f,  1.0f,
            // rear
                -1.0f,  1.0f, -1.0f,
                -1.0f, -1.0f, -1.0f,
                 1.0f, -1.0f, -1.0f,
                 1.0f,  1.0f, -1.0f,
            // left
                -1.0f, -1.0f,  1.0f,
                -1.0f, -1.0f, -1.0f,
                -1.0f,  1.0f, -1.0f,
                -1.0f,  1.0f,  1.0f,
            // right
                 1.0f, -1.0f,  1.0f,
                 1.0f, -1.0f, -1.0f,
                 1.0f,  1.0f, -1.0f,
                 1.0f,  1.0f,  1.0f,
            // bottom
                -1.0f, -1.0f,  1.0f,
                -1.0f, -1.0f, -1.0f,
                 1.0f, -1.0f, -1.0f,
                 1.0f, -1.0f,  1.0f,
            // top
                -1.0f,  1.0f,  1.0f,
                -1.0f,  1.0f, -1.0f,
                 1.0f,  1.0f, -1.0f,
                 1.0f,  1.0f,  1.0f,
        };
        float[] norms = {
            // front
                 0.0f,  0.0f,  1.0f,
                 0.0f,  0.0f,  1.0f,
                 0.0f,  0.0f,  1.0f,
                 0.0f,  0.0f,  1.0f,
            // rear
                 0.0f,  0.0f, -1.0f,
                 0.0f,  0.0f, -1.0f,
                 0.0f,  0.0f, -1.0f,
                 0.0f,  0.0f, -1.0f,
            // left
                -1.0f,  0.0f,  0.0f,
                -1.0f,  0.0f,  0.0f,
                -1.0f,  0.0f,  0.0f,
                -1.0f,  0.0f,  0.0f,
            // right
                 1.0f,  0.0f,  0.0f,
                 1.0f,  0.0f,  0.0f,
                 1.0f,  0.0f,  0.0f,
                 1.0f,  0.0f,  0.0f,
            // bottom
                 0.0f, -1.0f,  0.0f,
                 0.0f, -1.0f,  0.0f,
                 0.0f, -1.0f,  0.0f,
                 0.0f, -1.0f,  0.0f,
            // top
                 0.0f,  1.0f,  0.0f,
                 0.0f,  1.0f,  0.0f,
                 0.0f,  1.0f,  0.0f,
                 0.0f,  1.0f,  0.0f,
        };
        float[] colors = {
            // front
                 1.0f, 0.0f, 0.0f,
                 1.0f, 0.0f, 0.0f,
                 1.0f, 0.0f, 0.0f,
                 1.0f, 0.0f, 0.0f,
            // rear
                 0.0f, 1.0f, 0.0f,
                 0.0f, 1.0f, 0.0f,
                 0.0f, 1.0f, 0.0f,
                 0.0f, 1.0f, 0.0f,
            // left
                 0.0f, 0.0f, 1.0f,
                 0.0f, 0.0f, 1.0f,
                 0.0f, 0.0f, 1.0f,
                 0.0f, 0.0f, 1.0f,
            // right
                 1.0f, 1.0f, 0.0f,
                 1.0f, 1.0f, 0.0f,
                 1.0f, 1.0f, 0.0f,
                 1.0f, 1.0f, 0.0f,
            // bottom
                 1.0f, 0.0f, 1.0f,
                 1.0f, 0.0f, 1.0f,
                 1.0f, 0.0f, 1.0f,
                 1.0f, 0.0f, 1.0f,
            // top
                 0.0f, 1.0f, 1.0f,
                 0.0f, 1.0f, 1.0f,
                 0.0f, 1.0f, 1.0f,
                 0.0f, 1.0f, 1.0f,
        };
        int[] indcs = {
            // front
                 0,  1,  2,
                 0,  2,  3,
            // rear
                 4,  6,  5,
                 4,  7,  6,
            // left
                 8, 10,  9,
                 8, 11, 10,
            // right
                12, 13, 14,
                12, 14, 15,
            // bottom
                16, 17, 18,
                16, 18, 19,
            // top
                20, 22, 21,
                20, 23, 22,
        };
        
        return MeshFactory.createMesh(pos, norms, null, colors, indcs);
    }
}
