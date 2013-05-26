package com.github.fabmax.lightgl.util;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glGenBuffers;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.github.fabmax.lightgl.ShaderAttributeBinder;
import com.github.fabmax.lightgl.scene.Mesh;

/**
 * The MeshFactory supplies a few helper functions to create meshes.
 * 
 * @see Mesh
 * @author fabmax
 * 
 */
public class MeshFactory {
    /**
     * Creates a static mesh with the specified attributes. A static mesh uses a GL Vertex Buffer Object
     * to store the vertex data.
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
    public static Mesh createStaticMesh(float[] pos, float[] norms, float[] uvs, float[] colors, int[] indcs) {
        int normOffset = 0;
        int uvOffset = 0;
        int colorOffset = 0;
        
        // determine elements per vertex, 3 elements needed for vertex position
        int elems = 3;
        if (norms != null) {
            // add normals to buffer
            normOffset = elems;
            elems += 3;
        }
        if (uvs != null) {
            // add texture coordinates to buffer
            uvOffset = elems;
            elems += 2;
        }
        if (colors != null) {
            // add colors to buffer
            colorOffset = elems;
            elems += 3;
        }
        
        // determine vertex buffer size
        int vertCnt = pos.length / 3;
        FloatBuffer vertData = BufferHelper.createFloatBuffer(vertCnt * elems);
        // fill vertex buffer
        for (int i = 0, j = 0, k = 0; i < vertCnt; i++, j += 3, k += 2) {
            // vertex position
            vertData.put(pos, j, 3);
            if (norms != null) {
                // vertex normal
                vertData.put(norms, j, 3);
            }
            if (uvs != null) {
                // vertex normal
                vertData.put(uvs, k, 2);
            }
            if (colors != null) {
                // vertex normal
                vertData.put(colors, j, 3);
            }
        }
        vertData.rewind();
        // put vertex data in a VBO
        int[] buf = new int[1];
        glGenBuffers(1, buf, 0);
        glBindBuffer(GL_ARRAY_BUFFER, buf[0]);
        glBufferData(GL_ARRAY_BUFFER, vertData.capacity() * 4, vertData, GL_STATIC_DRAW);
        
        // create and fill index buffer
        Buffer indexBuffer;
        if(pos.length / 3 < 65536) {
            indexBuffer = BufferHelper.createShortBuffer(indcs.length);
            for (int i = 0; i < indcs.length; i++) {
                ((ShortBuffer) indexBuffer).put((short) indcs[i]);
            }
            indexBuffer.rewind();
        } else {
            indexBuffer = BufferHelper.createIntBuffer(indcs);
        }
        
        // create mesh
        ShaderAttributeBinder posBinder = ShaderAttributeBinder.createVboBufferBinder(buf[0], 3, elems * 4);
        ShaderAttributeBinder normalBinder = null;
        ShaderAttributeBinder uvBinder = null;
        ShaderAttributeBinder colorBinder = null;
        if (norms != null) {
            // set attribute binder for vertex normals
            normalBinder = ShaderAttributeBinder.createVboBufferBinder(buf[0], 3, elems * 4);
            normalBinder.setOffset(normOffset);
        }
        if (uvs != null) {
            // set attribute binder for vertex normals
            uvBinder = ShaderAttributeBinder.createVboBufferBinder(buf[0], 2, elems * 4);
            uvBinder.setOffset(uvOffset);
        }
        if (colors != null) {
            // set attribute binder for vertex normals
            colorBinder = ShaderAttributeBinder.createVboBufferBinder(buf[0], 3, elems * 4);
            colorBinder.setOffset(colorOffset);
        }
        return new Mesh(indexBuffer, posBinder, normalBinder, uvBinder, colorBinder);
    }
    
    /**
     * Creates a cube mesh with valid vertex colors and texture coordinates. The texture coordinates
     * are the same for every side, vertex colors are different for different sides.
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
        float[] uvs = {
            // front
                 0.0f, 1.0f,
                 0.0f, 0.0f,
                 1.0f, 0.0f,
                 1.0f, 1.0f,
            // rear
                 0.0f, 1.0f,
                 0.0f, 0.0f,
                 1.0f, 0.0f,
                 1.0f, 1.0f,
            // left
                 0.0f, 1.0f,
                 0.0f, 0.0f,
                 1.0f, 0.0f,
                 1.0f, 1.0f,
            // right
                 0.0f, 1.0f,
                 0.0f, 0.0f,
                 1.0f, 0.0f,
                 1.0f, 1.0f,
            // bottom
                 0.0f, 1.0f,
                 0.0f, 0.0f,
                 1.0f, 0.0f,
                 1.0f, 1.0f,
            // top
                 0.0f, 1.0f,
                 0.0f, 0.0f,
                 1.0f, 0.0f,
                 1.0f, 1.0f,
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
        
        return MeshFactory.createStaticMesh(pos, norms, uvs, colors, indcs);
    }
}
