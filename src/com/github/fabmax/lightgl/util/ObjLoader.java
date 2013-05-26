package com.github.fabmax.lightgl.util;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glGenBuffers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Locale;
import java.util.StringTokenizer;

import android.content.Context;
import android.util.Log;
import android.util.SparseIntArray;

import com.github.fabmax.lightgl.GlException;
import com.github.fabmax.lightgl.ShaderAttributeBinder;
import com.github.fabmax.lightgl.scene.Mesh;

/**
 * Basic model loader for .obj files. Supports vertex positions, normals and texture coordinates in
 * arbitrary combinations, but only triangle meshes and no material definitions.
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Wavefront_.obj_file">http://en.wikipedia.org/wiki/Wavefront_.obj_file</a>
 * @author fabmax
 * 
 */
public class ObjLoader {

    private static final String TAG = "ObjLoader";

    /**
     * Loads the specified file from the assets directory.
     * 
     * @param context
     *            application context, needed to access the assets directory
     * @param file
     *            file name of model file
     * @return the loaded {@link Mesh}
     * @throws GlException
     *             if an error occurred during model loading
     */
    public static Mesh loadObj(Context context, String file) throws GlException {
        try {
            // open specified OBJ file
            InputStream in = context.getAssets().open(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            GrowingFloatArray verts = new GrowingFloatArray();
            GrowingFloatArray texCoords = new GrowingFloatArray();
            GrowingFloatArray normals = new GrowingFloatArray();
            GrowingIntArray indices = new GrowingIntArray();

            // parse elements from OBJ file
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("v ")) {
                    // parse vertex position
                    parseFloats(line, 3, verts);

                } else if (line.startsWith("vt ")) {
                    // parse texture coordinate
                    parseFloats(line, 2, texCoords);

                } else if (line.startsWith("vn ")) {
                    // parse vertex normal
                    parseFloats(line, 3, normals);

                } else if (line.startsWith("f ")) {
                    // parse face vertex indices
                    parseFaceIndices(line, indices);

                }
                // all other lines are ignored
            }

            Log.d(TAG, String.format(Locale.ENGLISH, "Parsed OBJ file: vp:%d, vt:%d, vn:%d, f:%d",
                    verts.size() / 3, texCoords.size() / 2, normals.size() / 3, indices.size() / 3));

            // determine number of indices per face vertex
            int idxPerVertex = 1;
            int vertElements = 3;
            int normalOffset = 0;
            int texCoordOffset = 0;
            if (!texCoords.isEmpty()) {
                idxPerVertex++;
                texCoordOffset = vertElements;
                vertElements += 2;
            }
            if (!normals.isEmpty()) {
                idxPerVertex++;
                normalOffset = vertElements;
                vertElements += 3;
            }

            // OBJ supports different indices for attributes per vertex, OpenGL not - we need to
            // rebuild the vertex list
            int capa = GlMath.max3(verts.size(), normals.size(), texCoords.size()) * vertElements;
            GrowingFloatArray vertexData = new GrowingFloatArray(capa);
            GrowingIntArray glIndices = new GrowingIntArray();
            SparseIntArray glIndexMap = new SparseIntArray();
            float[] vertex = new float[vertElements];
            for (int i = 0; i < indices.size(); i += idxPerVertex) {
                int ii = i;
                int vi = 0;
                int vtIdx = 0;
                int vnIdx = 0;

                // get vertex position
                int idx = indices.get(ii++) * 3;
                vertex[vi++] = verts.get(idx);
                vertex[vi++] = verts.get(idx + 1);
                vertex[vi++] = verts.get(idx + 2);

                // get vertex texture coordinate (if present)
                if (!texCoords.isEmpty()) {
                    vtIdx = indices.get(ii++) * 2;
                    vertex[vi++] = texCoords.get(vtIdx);
                    vertex[vi++] = texCoords.get(vtIdx + 1);
                }

                // get vertex normal (if present)
                if (!normals.isEmpty()) {
                    vnIdx = indices.get(ii++) * 3;
                    vertex[vi++] = normals.get(vnIdx);
                    vertex[vi++] = normals.get(vnIdx + 1);
                    vertex[vi++] = normals.get(vnIdx + 2);
                }

                int hash = getIndexHash(idx, vtIdx, vnIdx);
                int glIdx = glIndexMap.get(hash);
                if (glIdx != 0) {
                    // this vertex is already in vertex data list, add its index
                    glIndices.add(glIdx - 1);
                } else {
                    // this vertex is not yet in vertex data list, add it
                    vertexData.add(vertex);
                    // add new index
                    glIdx = vertexData.size() / vertElements;
                    glIndices.add(glIdx - 1);
                    glIndexMap.put(hash, glIdx);
                }
            }

            // create Mesh
            IntBuffer meshIndices = BufferHelper.createIntBuffer(glIndices.size());
            glIndices.copyToBuffer(meshIndices);

            // put vertex data in a VBO
            int[] buf = new int[1];
            FloatBuffer meshData = BufferHelper.createFloatBuffer(vertexData.size());
            vertexData.copyToBuffer(meshData);
            glGenBuffers(1, buf, 0);
            glBindBuffer(GL_ARRAY_BUFFER, buf[0]);
            glBufferData(GL_ARRAY_BUFFER, meshData.capacity() * 4, meshData, GL_STATIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            
            // create attribute binders
            ShaderAttributeBinder posBinder = ShaderAttributeBinder.createVboBufferBinder(buf[0], 3, vertElements * 4);
            ShaderAttributeBinder normalBinder = null;
            ShaderAttributeBinder uvBinder = null;
            if (!normals.isEmpty()) {
                normalBinder = ShaderAttributeBinder.createVboBufferBinder(buf[0], 3, vertElements * 4);
                normalBinder.setOffset(normalOffset);
            }
            if (!texCoords.isEmpty()) {
                uvBinder = ShaderAttributeBinder.createVboBufferBinder(buf[0], 2, vertElements * 4);
                uvBinder.setOffset(texCoordOffset);
            }

            Log.d(TAG, String.format(Locale.ENGLISH, "Created Mesh: %d vertices, %d faces",
                            vertexData.size() / vertElements, glIndices.size() / 3));

            return new Mesh(meshIndices, posBinder, normalBinder, uvBinder, null);

        } catch (Exception e) {
            throw new GlException("Failed parsing OBJ file: " + e.getMessage(), e);
        }
    }

    /**
     * Computes a hash from the specified position, normal and texture coordinate indices.
     * 
     * @param ip
     *            vertex position index
     * @param in
     *            vertex normal index
     * @param it
     *            vertex texture coordinate index
     * @return computed hash value
     */
    private static int getIndexHash(int ip, int it, int in) {
        return ip ^ (in << 16) ^ (it << 8);
    }

    /**
     * Parses the specified number of floats from line. line is a line read from the obj file and
     * contains an type prefix which is ignored. The parsed float values are stored in the
     * destination buffer.
     * 
     * @param line
     *            line to parse the values from
     * @param parseCnt
     *            number of values to parse
     * @param dstBuf
     *            destination buffer
     */
    private static void parseFloats(String line, int parseCnt, GrowingFloatArray dstBuf) {
        StringTokenizer tok = new StringTokenizer(line, " ");
        // skip first token (line prefix)
        tok.nextToken();
        // parse specified number of floats
        for (int i = 0; i < parseCnt; i++) {
            String s = tok.nextToken();
            dstBuf.add(Float.parseFloat(s));
        }
    }

    /**
     * Parses position, normal and texture coordinate indices from the given face index line. Only
     * triangle faces are supported.
     * 
     * @param line
     *            line to parse indices from
     * @param dstBuf
     *            destination buffer
     * @throws GlException
     *             if the face has more than 3 vertices
     */
    private static void parseFaceIndices(String line, GrowingIntArray dstBuf) throws GlException {
        StringTokenizer tok = new StringTokenizer(line, " ");
        // skip first token (line prefix)
        tok.nextToken();
        // parse face indices
        for (int i = 0; i < 3; i++) {
            // retrieve vertex index string can have following formats:
            // [v], [v]/[vt], [v]/[vt]/[vn] or [v]//[vn]
            String vi = tok.nextToken();
            // parse element indices
            StringTokenizer tok2 = new StringTokenizer(vi, "/");
            while (tok2.hasMoreTokens()) {
                // add element index, OBJ element indices count from 1
                dstBuf.add(Integer.parseInt(tok2.nextToken()) - 1);
            }
        }
        if (tok.hasMoreTokens()) {
            throw new GlException("No support for more than 3 vertex indices per face");
        }
    }

}
