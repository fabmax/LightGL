package de.fabmax.lightgl.util;

import de.fabmax.lightgl.compat.Matrix;

/**
 * MeshBuilder can be used to iteratively build a mesh.
 * 
 * @author fth
 */
public class MeshBuilder {

    private final IntList mIndices = new IntList();
    private final FloatList mPositions = new FloatList();
    private final FloatList mNormals;
    private final FloatList mTexCoords;
    private final FloatList mColors;

    private boolean mHasNormals;
    private boolean mHasTexCoords;
    private boolean mHasColors;
    
    private final float[] mTransform = new float[16];
    
    //private PolygonTesselator mTesselator;
    
    /**
     * Creates a new MeshBuilder instance. The mesh to be build can optionally have normals,
     * texture coordinates and colors as additional vertex attributes.
     * 
     * @param hasNormals    true, if the mesh should have normal vertex attributes
     * @param hasTexCoords  true, if the mesh should have texture coordinate vertex attributes
     * @param hasColors     true, if the mesh should have color vertex attributes
     */
    public MeshBuilder(boolean hasNormals, boolean hasTexCoords, boolean hasColors) {
        mHasNormals = hasNormals;
        mHasTexCoords = hasTexCoords;
        mHasColors = hasColors;

        if (mHasNormals) {
            mNormals = new FloatList();
        } else {
            mNormals = null;
        }
        if (mHasTexCoords) {
            mTexCoords = new FloatList();
        } else {
            mTexCoords = null;
        }
        if (mHasColors) {
            mColors = new FloatList();
        } else {
            mColors = null;
        }
        
        Matrix.setIdentityM(mTransform, 0);
    }
    
    /**
     * Returns a {@link PolygonTesselator} that can be used to add arbitrary polygons to this
     * MeshBuilder.
     * 
     * @return a {@link PolygonTesselator} that can be used to add arbitrary polygons to this
     *         MeshBuilder
     */
//    public PolygonTesselator getTesselator() {
//        if (mTesselator == null) {
//            mTesselator = new PolygonTesselator(this);
//        }
//        return mTesselator;
//    }
    
    /**
     * Clears all data in this MeshBuilder and resets the transform matrix to identity.
     */
    public void clear() {
        mIndices.clear();
        mPositions.clear();
        if (mNormals != null) {
            mNormals.clear();
        }
        if (mTexCoords != null) {
            mTexCoords.clear();
        }
        if (mColors != null) {
            mColors.clear();
        }
        Matrix.setIdentityM(mTransform, 0);
    }
    
    /**
     * Returns the number of vertices currently added to this MeshBuilder.
     * 
     * @return the number of vertices currently added to this MeshBuilder
     */
    public int getVertexCount() {
        return mPositions.size() / 3;
    }
    
    /**
     * Tests whether this MeshBuilder is empty.
     * 
     * @return true if this MeshBuilder is empty, false otherwise.
     */
    public boolean isEmpty() {
        return mPositions.size() == 0;
    }
    
    /**
     * Sets a transform matrix, which is used to transform positions of vertices added with
     * {@link #addVertex(float[], int, float[], int, float[], int, float[], int)}.
     * 
     * @param transform 4x4 transform matrix
     */
    public void setTransform(float[] transform) {
        System.arraycopy(transform, 0, mTransform, 0, 16);
    }
    
    /**
     * Adds a vertex to this MeshBuilder. Optional vertex attributes, which were selected on
     * creation of this MeshBuilder, have to be passed. Unused vertex attributes can be null.
     * 
     * @param position  Position of this vertex (x, y, z)
     * @param posOff    Position array offset
     * @param normal    Normal of this vertex (x, y, z) - can be null, if normals are not used
     * @param normOff   Normal array offset
     * @param texCoord  Texture coordinates of this vertex (u, v) - can be null, if not used
     * @param texOff    Texture coordinates array offset
     * @param color     Color of this vertex (r, g, b, a)
     * @param colOff    Color array offset
     * @return index of the added vertex
     */
    public int addVertex(float[] position, int posOff, float[] normal, int normOff,
                         float[] texCoord, int texOff, float[] color, int colOff) {
        
        // transform vertex position according to current transform
        float x = mTransform[0]  * position[posOff]     + mTransform[4] * position[posOff + 1] +
                  mTransform[8]  * position[posOff + 2] + mTransform[12];
        float y = mTransform[1]  * position[posOff]     + mTransform[5] * position[posOff + 1] +
                  mTransform[9]  * position[posOff + 2] + mTransform[13];
        float z = mTransform[2]  * position[posOff]     + mTransform[6] * position[posOff + 1] +
                  mTransform[10] * position[posOff + 2] + mTransform[14];

        // add vertex position
        mPositions.add(x);
        mPositions.add(y);
        mPositions.add(z);
        
        // add optional vertex attributes
        if (mHasNormals) {
            float nx = mTransform[0]  * normal[normOff]     + mTransform[4] * normal[normOff + 1] +
                    mTransform[8]  * normal[normOff + 2];
            float ny = mTransform[1]  * normal[normOff]     + mTransform[5] * normal[normOff + 1] +
                    mTransform[9]  * normal[normOff + 2];
            float nz = mTransform[2]  * normal[normOff]     + mTransform[6] * normal[normOff + 1] +
                    mTransform[10] * normal[normOff + 2];

            mNormals.add(nx);
            mNormals.add(ny);
            mNormals.add(nz);
        }
        if (mHasTexCoords) {
            mTexCoords.add(texCoord[texOff]);
            mTexCoords.add(texCoord[texOff + 1]);
        }
        if (mHasColors) {
            mColors.add(color[colOff]);
            mColors.add(color[colOff + 1]);
            mColors.add(color[colOff + 2]);
            mColors.add(color[colOff + 3]);
        }
        
        // return the index of the added vertex
        return getVertexCount() - 1;
    }
    
    /**
     * Adds a single vertex index to the mesh. The vertex must have been previously added with
     * {@link #addVertex(float[], int, float[], int, float[], int, float[], int)}.
     * 
     * @param idx  Vertex index to add
     */
    public void addElementIndex(int idx) {
        mIndices.add(idx);
    }
    
    /**
     * Adds a line to the mesh with vertices previously added to this MeshBuilder with
     * {@link #addVertex(float[], int, float[], int, float[], int, float[], int)}.
     * 
     * @param idx0  First vertex index
     * @param idx1  Second vertex index
     */
    public void addLine(int idx0, int idx1) {
        mIndices.add(idx0);
        mIndices.add(idx1);
    }
    
    /**
     * Adds a triangle to the mesh with vertices previously added to this MeshBuilder with
     * {@link #addVertex(float[], int, float[], int, float[], int, float[], int)}
     * 
     * @param idx0  First vertex index
     * @param idx1  Second vertex index
     * @param idx2  Third vertex index
     */
    public void addTriangle(int idx0, int idx1, int idx2) {
        mIndices.add(idx0);
        mIndices.add(idx1);
        mIndices.add(idx2);
    }
    
    /**
     * Adds all data from the given {@link MeshData} to this MeshBuilder.
     * The specified mesh data must have matching vertex attributes (normals, colors, etc.) as the
     * for this MeshBuilder.
     * 
     * @param meshData  the mesh data to add
     */
    public void addMeshData(MeshData meshData) {
        if (!meshData.isEmpty()) {
            for (int i = 0; i < meshData.indices.length; i += 3) {
                int di = meshData.indices[i];
                int v0 = addVertex(meshData.positions, di * 3, meshData.normals, di * 3,
                                   meshData.texCoords, di * 2, meshData.colors,  di * 4);
                di = meshData.indices[i + 1];
                int v1 = addVertex(meshData.positions, di * 3, meshData.normals, di * 3,
                                   meshData.texCoords, di * 2, meshData.colors,  di * 4);
                di = meshData.indices[i + 2];
                int v2 = addVertex(meshData.positions, di * 3, meshData.normals, di * 3,
                                   meshData.texCoords, di * 2, meshData.colors,  di * 4);
                addTriangle(v0, v1, v2);
            }
        }
    }
    
    /**
     * Builds a {@link de.fabmax.lightgl.util.MeshData} object that can be used to construct a mesh (either
     * {@link de.fabmax.lightgl.scene.StaticMesh} or {@link de.fabmax.lightgl.scene.DynamicMesh}.
     *
     * @return the mesh data needed to build the mesh
     */
    public MeshData build() {
        MeshData meshData = new MeshData();
        if (!isEmpty()) {
            meshData.indices = mIndices.asArray();
            meshData.positions = mPositions.asArray();
    
            if (mHasNormals) {
                meshData.normals = mNormals.asArray();
            }
            if (mHasTexCoords) {
                meshData.texCoords = mTexCoords.asArray();
            }
            if (mHasColors) {
                meshData.colors = mColors.asArray();
            }
        }
        return meshData;
    }

    /**
     * Transforms an array of 3 element vectors according to the specified transformation matrix.
     *
     * @param transform    4x4 Transformation matrix
     * @param vectors      Array with 3 element vectors
     * @param w            4th vector component for transformation
     */
    public static void transformVectors(float[] transform, float[] vectors, float w) {
        int n = vectors.length / 3;
        for (int i = 0, j = 0; i < n; i++, j += 3) {
            GlMath.transformVector(vectors, j, w, transform, 0);
        }
    }

    /**
     * Returns the underlying triangle vertex index IntList of this MeshBuilder.
     *
     * @return the underlying triangle vertex index IntList of this MeshBuilder
     */
    public IntList getIndices() {
        return mIndices;
    }

    /**
     * Returns the underlying vertex position FloatList of this MeshBuilder.
     *
     * @return the underlying vertex position FloatList of this MeshBuilder
     */
    public FloatList getPositions() {
        return mPositions;
    }

    /**
     * Returns the underlying vertex normals FloatList of this MeshBuilder.
     *
     * @return the underlying vertex normal FloatList of this MeshBuilder
     */
    public FloatList getNormals() {
        return mNormals;
    }

    /**
     * Returns the underlying vertex texture coordinate FloatList of this MeshBuilder.
     *
     * @return the underlying vertex texture coordinate FloatList of this MeshBuilder
     */
    public FloatList getTextureCoords() {
        return mTexCoords;
    }

    /**
     * Returns the underlying vertex color FloatList of this MeshBuilder.
     *
     * @return the underlying vertex color FloatList of this MeshBuilder
     */
    public FloatList getColors() {
        return mColors;
    }

    /**
     * Returns true if normal data is available.
     *
     * @return true if normal data is available
     */
    public boolean hasNormals() {
        return mHasNormals;
    }

    /**
     * Returns true if texture data is available.
     *
     * @return true if texture data is available
     */
    public boolean hasTextureCoordinates() {
        return mHasTexCoords;
    }

    /**
     * Returns true if color data is available.
     *
     * @return true if color data is available
     */
    public boolean hasColors() {
        return mHasColors;
    }
}
