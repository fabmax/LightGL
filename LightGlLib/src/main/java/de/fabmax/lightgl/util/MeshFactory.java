package de.fabmax.lightgl.util;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import de.fabmax.lightgl.ShaderAttributeBinder;
import de.fabmax.lightgl.scene.Mesh;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glGenBuffers;

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
     * @param info      Data used to construct the mesh
     * @return the created mesh
     */
    public static Mesh createStaticMesh(MeshConstructionInfo info) {
        int normOffset = 0;
        int uvOffset = 0;
        int colorOffset = 0;
        
        // determine elements per vertex, 3 elements needed for vertex position
        int elems = 3;
        if (info.normals != null) {
            // add normals to buffer
            normOffset = elems;
            elems += 3;
        }
        if (info.texCoords != null) {
            // add texture coordinates to buffer
            uvOffset = elems;
            elems += 2;
        }
        if (info.colors != null) {
            // add colors to buffer
            colorOffset = elems;
            elems += 3;
        }
        
        // determine vertex buffer size
        int vertCnt = info.positions.length / 3;
        FloatBuffer vertData = BufferHelper.createFloatBuffer(vertCnt * elems);
        // fill vertex buffer
        for (int i = 0, j = 0, k = 0; i < vertCnt; i++, j += 3, k += 2) {
            // vertex position
            vertData.put(info.positions, j, 3);
            if (info.normals != null) {
                // vertex normal
                vertData.put(info.normals, j, 3);
            }
            if (info.texCoords != null) {
                // vertex normal
                vertData.put(info.texCoords, k, 2);
            }
            if (info.colors != null) {
                // vertex normal
                vertData.put(info.colors, j, 3);
            }
        }
        vertData.rewind();
        // put vertex data in a VBO
        int[] buf = new int[1];
        glGenBuffers(1, buf, 0);
        glBindBuffer(GL_ARRAY_BUFFER, buf[0]);
        glBufferData(GL_ARRAY_BUFFER, vertData.capacity() * 4, vertData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        
        // create and fill index buffer
        Buffer indexBuffer;
        if(info.positions.length / 3 < 65536) {
            indexBuffer = BufferHelper.createShortBuffer(info.indices.length);
            for (int i = 0; i < info.indices.length; i++) {
                ((ShortBuffer) indexBuffer).put((short) info.indices[i]);
            }
            indexBuffer.rewind();
        } else {
            indexBuffer = BufferHelper.createIntBuffer(info.indices);
        }
        
        // create mesh
        ShaderAttributeBinder posBinder = ShaderAttributeBinder.createVboBufferBinder(buf[0], 3, elems * 4);
        ShaderAttributeBinder normalBinder = null;
        ShaderAttributeBinder uvBinder = null;
        ShaderAttributeBinder colorBinder = null;
        if (info.normals != null) {
            // set attribute binder for vertex normals
            normalBinder = ShaderAttributeBinder.createVboBufferBinder(buf[0], 3, elems * 4);
            normalBinder.setOffset(normOffset);
        }
        if (info.texCoords != null) {
            // set attribute binder for vertex normals
            uvBinder = ShaderAttributeBinder.createVboBufferBinder(buf[0], 2, elems * 4);
            uvBinder.setOffset(uvOffset);
        }
        if (info.colors != null) {
            // set attribute binder for vertex normals
            colorBinder = ShaderAttributeBinder.createVboBufferBinder(buf[0], 3, elems * 4);
            colorBinder.setOffset(colorOffset);
        }
        return new Mesh(indexBuffer, posBinder, normalBinder, uvBinder, colorBinder);
    }

    /**
     * Creates {@link de.fabmax.lightgl.util.MeshFactory.MeshConstructionInfo} for a cylinder with
     * the specified dimension. Center is at (0, 0, 0), cylinder axis is the y-axis. If a non-null
     * 4x4 transform matrix is passed the mesh vertices are transformed accordingly.
     * Use {@link #createStaticMesh(de.fabmax.lightgl.util.MeshFactory.MeshConstructionInfo)} to
     * create a {@link de.fabmax.lightgl.scene.Mesh} from the returned construction info.
     *
     * @param radius    Cylinder radius
     * @param height    Cylinder height
     * @param steps     Number of points used for circle approximation
     * @param transform 4x4 transform matrix, can be null
     * @return A cylinder mesh with the specified dimensions
     */
    public static MeshConstructionInfo createCylinder(float radius, float height, int steps, float[] transform) {
        FloatList pos = new FloatList();
        FloatList norms = new FloatList();
        IntList indcs = new IntList();

        int idxV = 0;
        float s = GlMath.PI * 2 / steps;
        float a = 0;

        // belt
        for (int i = 0; i <= steps; i++) {
            float x = (float) Math.cos(a);
            float z = (float) Math.sin(a);
            a += s;

            pos.add(x * radius);
            pos.add(-height / 2);
            pos.add(z * radius);

            pos.add(x * radius);
            pos.add(height / 2);
            pos.add(z * radius);

            norms.add(x);
            norms.add(0);
            norms.add(z);

            norms.add(x);
            norms.add(0);
            norms.add(z);

            if (i > 0) {
                indcs.add(idxV-2);
                indcs.add(idxV-1);
                indcs.add(idxV);
                indcs.add(idxV-1);
                indcs.add(idxV+1);
                indcs.add(idxV);
            }
            idxV += 2;
        }

        // bottom cap
        int vStart = idxV;
        for (int i = 0; i < steps; i++) {
            float x = (float) Math.cos(a);
            float z = (float) Math.sin(a);
            a += s;

            pos.add(x * radius);
            pos.add(-height / 2);
            pos.add(z * radius);

            norms.add(0);
            norms.add(-1);
            norms.add(0);

            if (i > 1) {
                indcs.add(vStart);
                indcs.add(idxV-1);
                indcs.add(idxV);
            }
            idxV++;
        }

        // top cap
        vStart = idxV;
        for (int i = 0; i < steps; i++) {
            float x = (float) Math.cos(a);
            float z = (float) Math.sin(a);
            a += s;


            pos.add(x * radius);
            pos.add(height / 2);
            pos.add(z * radius);

            norms.add(0);
            norms.add(1);
            norms.add(0);

            if (i > 1) {
                indcs.add(vStart);
                indcs.add(idxV);
                indcs.add(idxV-1);
            }
            idxV++;
        }

        MeshConstructionInfo info = new MeshConstructionInfo();
        info.indices = indcs.asArray();
        info.positions = pos.asArray();
        info.normals = norms.asArray();
        if (transform != null) {
            transformVerts(transform, info.positions, 1);
            transformVerts(transform, info.normals, 0);
        }

        return info;
    }
    
    /**
     * Creates {@link de.fabmax.lightgl.util.MeshFactory.MeshConstructionInfo} for a cube mesh with
     * normals, vertex colors and texture coordinates. The texture coordinates are the same for
     * every side, vertex colors are different for different sides. If a non-null 4x4 transform
     * matrix is passed the mesh vertices are transformed accordingly.
     * Use {@link #createStaticMesh(de.fabmax.lightgl.util.MeshFactory.MeshConstructionInfo)} to
     * create a {@link de.fabmax.lightgl.scene.Mesh} from the returned construction info.
     *
     * @param sizeX     width of the cube
     * @param sizeY     height of the cube
     * @param sizeZ     depth of the cube
     * @param transform 4x4 transform matrix, can be null
     * @return Construction info for a box mesh with the specified dimensions
     */
    public static MeshConstructionInfo createColorCube(float sizeX, float sizeY, float sizeZ, float[] transform) {
        sizeX /= 2.0f;
        sizeY /= 2.0f;
        sizeZ /= 2.0f;

        MeshConstructionInfo info = new MeshConstructionInfo();

        info.positions = new float[] {
            // front
                -sizeX,  sizeY,  sizeZ,
                -sizeX, -sizeY,  sizeZ,
                 sizeX, -sizeY,  sizeZ,
                 sizeX,  sizeY,  sizeZ,
            // rear
                -sizeX,  sizeY, -sizeZ,
                -sizeX, -sizeY, -sizeZ,
                 sizeX, -sizeY, -sizeZ,
                 sizeX,  sizeY, -sizeZ,
            // left
                -sizeX, -sizeY,  sizeZ,
                -sizeX, -sizeY, -sizeZ,
                -sizeX,  sizeY, -sizeZ,
                -sizeX,  sizeY,  sizeZ,
            // right
                 sizeX, -sizeY,  sizeZ,
                 sizeX, -sizeY, -sizeZ,
                 sizeX,  sizeY, -sizeZ,
                 sizeX,  sizeY,  sizeZ,
            // bottom
                -sizeX, -sizeY,  sizeZ,
                -sizeX, -sizeY, -sizeZ,
                 sizeX, -sizeY, -sizeZ,
                 sizeX, -sizeY,  sizeZ,
            // top
                -sizeX,  sizeY,  sizeZ,
                -sizeX,  sizeY, -sizeZ,
                 sizeX,  sizeY, -sizeZ,
                 sizeX,  sizeY,  sizeZ,
        };
        info.normals = new float[] {
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
        info.texCoords = new float[] {
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
        info.colors = new float[] {
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
        info.indices = new int[] {
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

        if (transform != null) {
            transformVerts(transform, info.positions, 1);
            transformVerts(transform, info.normals, 0);
        }

        return info;
    }

    private static void transformVerts(float[] transform, float[] verts, float w) {
        int n = verts.length / 3;
        for (int i = 0, j = 0; i < n; i++, j += 3) {
            GlMath.transformVector(verts, j, w, transform, 0);
        }
    }

    /**
     * MeshConstructionInfo holds all information needed to create a
     * {@link de.fabmax.lightgl.scene.Mesh}. Only {@link #indices} and {@link #positions} are
     * required to create a mesh. All other information is optional. Moreover the
     * {@link de.fabmax.lightgl.Shader}, used to render the mesh, must support texturing or coloring
     * in order to reflect additional mesh information like texture coordinates or vertex colors.
     */
    public static class MeshConstructionInfo {

        /** Triangle vertex indices */
        public int[] indices;
        /** 3-element vertex positions (x, y, z) */
        public float[] positions;
        /** 3-element Vertex normals (x, y, z), optional */
        public float[] normals;
        /** Vertex texture coordinates (u, v), optional */
        public float[] texCoords;
        /** Vertex colors (r, g, b), optional */
        public float[] colors;

    }
}
