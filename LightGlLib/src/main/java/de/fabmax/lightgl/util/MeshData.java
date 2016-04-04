package de.fabmax.lightgl.util;


/**
 * MeshConstructionInfo holds all information needed to create a
 * {@link de.fabmax.lightgl.scene.Mesh}. Only {@link #indices} and {@link #positions} are
 * required to create a mesh. All other information is optional. Moreover the
 * {@link de.fabmax.lightgl.shading.Shader}, used to render the mesh, must support texturing or coloring
 * in order to reflect additional mesh information like texture coordinates or vertex colors.
 *
 * @author fth
 */
public class MeshData {
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

    /**
     * Returns the number of vertices.
     *
     * @return the number of vertices
     */
    public int getVertexCount() {
        if (!isEmpty()) {
            return positions.length / 3;
        } else {
            return 0;
        }
    }

    /**
     * Tests whether this MeshConstructionInfo contains valid mesh data or not.
     *
     * @return true if this MEshConstructionInfo contains no valid data.
     */
    public boolean isEmpty() {
        return (indices == null || indices.length == 0);
    }

    /**
     * Returns true if normal data is available.
     *
     * @return true if normal data is available
     */
    public boolean hasNormals() {
        return normals != null;
    }

    /**
     * Returns true if texture data is available.
     *
     * @return true if texture data is available
     */
    public boolean hasTextureCoordinates() {
        return texCoords != null;
    }

    /**
     * Returns true if color data is available.
     *
     * @return true if color data is available
     */
    public boolean hasColors() {
        return colors != null;
    }
}
