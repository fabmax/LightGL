package de.fabmax.lightgl.util;

import java.nio.FloatBuffer;

import de.fabmax.lightgl.platform.Log;

/**
 * A buffer containing vertex data. Vertices always have a position. Moreover they can have normals, texture coordinates
 * and colors as optional attributes.
 *
 * @author fth
*/
public class PackedVertexBuffer {

    public final int vertexCount;
    public final int offsetPositions;
    public final int offsetNormals;
    public final int offsetTexCoords;
    public final int offsetColors;
    public final int strideBytes;

    public final FloatBuffer data;

    /** Optional vertex index array */
    public int[] vertexIndices;

    public PackedVertexBuffer(int vertexCount, boolean hasNormals, boolean hasTexCoords, boolean hasColors) {
        int elems = 3;
        this.vertexCount = vertexCount;
        offsetPositions = 0;

        // include normals if specified
        if (hasNormals) {
            offsetNormals = elems;
            elems += 3;
        } else {
            offsetNormals = -1;
        }

        // include texture coordinates if specified
        if (hasTexCoords) {
            offsetTexCoords = elems;
            elems += 2;
        } else {
            offsetTexCoords = -1;
        }

        // include colors if specified
        if (hasColors) {
            offsetColors = elems;
            elems += 4;
        } else {
            offsetColors = -1;
        }

        // strideBytes is in bytes -> float elements have 4 bytes
        strideBytes = elems * 4;
        data = BufferHelper.createFloatBuffer(elems * vertexCount);
    }

    public PackedVertexBuffer(MeshData meshData) {
        this(meshData.getVertexCount(),
                meshData.hasNormals(),
                meshData.hasTextureCoordinates(),
                meshData.hasColors());

        pack(meshData);
        vertexIndices = meshData.indices;
    }

    public void pack(MeshBuilder builder) {
        if (hasTextureCoordinates() != builder.hasTextureCoordinates() ||
                hasNormals() != builder.hasNormals() ||
                hasColors() != builder.hasColors()) {
            throw new IllegalArgumentException("Mismatching mesh data vertex attributes");
        }

        int vertCnt = builder.getVertexCount();
        if (vertCnt > vertexCount) {
            Log.w("Supplied mesh data does not fit in this buffer, truncating...");
            vertCnt = vertexCount;
        }

        // set buffer limit according to data size
        setVertexLimit(vertCnt);

        // fill vertex buffer
        FloatList positions = builder.getPositions();
        FloatList normals = builder.getNormals();
        FloatList texCoords = builder.getTextureCoords();
        FloatList colors = builder.getColors();
        for (int i = 0, j = 0, k = 0, l = 0; i < vertCnt; i++, j += 3, k += 2, l += 4) {
            // vertex position
            data.put(positions.get(j));
            data.put(positions.get(j + 1));
            data.put(positions.get(j + 2));

            if (normals != null) {
                // vertex normal
                data.put(normals.get(j));
                data.put(normals.get(j + 1));
                data.put(normals.get(j + 2));
            }
            if (texCoords != null) {
                // vertex texture coordinate
                data.put(texCoords.get(k));
                data.put(texCoords.get(k + 1));
            }
            if (colors != null) {
                // vertex color
                data.put(colors.get(l));
                data.put(colors.get(l + 1));
                data.put(colors.get(l + 2));
                data.put(colors.get(l + 3));
            }
        }
        data.rewind();
    }

    public void pack(MeshData meshData) {
        if (hasTextureCoordinates() != meshData.hasTextureCoordinates() ||
                hasNormals() != meshData.hasNormals() ||
                hasColors() != meshData.hasColors()) {
            throw new IllegalArgumentException("Mismatching mesh data vertex attributes");
        }

        int vertCnt = meshData.getVertexCount();
        if (vertCnt > vertexCount) {
            Log.w("Supplied mesh data does not fit in this buffer, truncating...");
            vertCnt = vertexCount;
        }

        // set buffer limit according to data size
        setVertexLimit(vertCnt);

        // fill vertex buffer
        for (int i = 0, j = 0, k = 0, l = 0; i < vertCnt; i++, j += 3, k += 2, l += 4) {
            // vertex position
            data.put(meshData.positions, j, 3);
            if (hasNormals()) {
                // vertex normal
                data.put(meshData.normals, j, 3);
            }
            if (hasTextureCoordinates()) {
                // vertex texture coordinate
                data.put(meshData.texCoords, k, 2);
            }
            if (hasColors()) {
                // vertex color
                data.put(meshData.colors, l, 4);
            }
        }
        data.rewind();
    }

    public void setVertexAt(int index, float[] position, int posOff, float[] normal, int normOff,
                            float[] texCoord, int texOff, float[] color, int colOff) {
        if (!hasTextureCoordinates() && texCoord!= null) {
            Log.w("Normal data supplied although this vertex buffer has no normals, ignoring...");
        }
        if (!hasColors() && color != null) {
            Log.w("Normal data supplied although this vertex buffer has no normals, ignoring...");
        }
        if (index < 0 || index >= vertexCount) {
            throw new ArrayIndexOutOfBoundsException("Invalid vertex index specified: " + index +
                    " (buffer size: " + vertexCount + ")");
        }

        if (index >= size()) {
            setVertexLimit(index + 1);
        }

        data.position(strideBytes / 4 * index);
        data.put(position, posOff, 3);
        if (hasNormals()) {
            data.put(normal, normOff, 3);
        }
        if (hasTextureCoordinates()) {
            data.put(texCoord, texOff, 2);
        }
        if (hasColors()) {
            data.put(color, colOff, 4);
        }
        data.position(0);
    }

    public void clear() {
        setVertexLimit(0);
    }

    public int capacity() {
        return vertexCount;
    }

    public int size() {
        return data.limit() * 4 / strideBytes;
    }

    private void setVertexLimit(int vertexLimit) {
        data.limit(vertexLimit * strideBytes / 4);
    }

    public boolean hasNormals() {
        return offsetNormals >= 0;
    }

    public boolean hasTextureCoordinates() {
        return offsetTexCoords >= 0;
    }

    public boolean hasColors() {
        return offsetColors >= 0;
    }
}
