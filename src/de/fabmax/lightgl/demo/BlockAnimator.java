package de.fabmax.lightgl.demo;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_RGBA;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glTexImage2D;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import de.fabmax.lightgl.GfxEngine;
import de.fabmax.lightgl.GfxEngineListener;
import de.fabmax.lightgl.GfxState;
import de.fabmax.lightgl.SimpleShader;
import de.fabmax.lightgl.Ray;
import de.fabmax.lightgl.ShaderAttributeBinder;
import de.fabmax.lightgl.ShadowRenderPass;
import de.fabmax.lightgl.ShadowShader;
import de.fabmax.lightgl.Texture;
import de.fabmax.lightgl.TextureProperties;
import de.fabmax.lightgl.TextureProperties.MagFilterMethod;
import de.fabmax.lightgl.TextureProperties.MinFilterMethod;
import de.fabmax.lightgl.scene.Mesh;
import de.fabmax.lightgl.util.BufferHelper;
import de.fabmax.lightgl.util.FloatList;
import de.fabmax.lightgl.util.GlMath;
import de.fabmax.lightgl.util.IntList;

/**
 * The BlockAnimator creates a grid of blocks that are animated in height and color over time.
 * 
 * @author fabmax
 * 
 */
public class BlockAnimator {
    private static final int MAX_SIZE_X = 32;
    private static final int MAX_SIZE_Z = 32;

    private int mSizeX;
    private int mSizeZ;
    private Block[] mBlocks;
    private FloatBuffer mPositionBuffer;
    private Mesh mBlockMesh;
    
    private Texture mTexture;
    private IntBuffer mTextureData;
    
    /**
     * Creates a BlockAnimator with the specified grid size. If a ShadowRenderPass is given shadow mapping
     * is enabled.
     * 
     * @param engine
     *            graphics engine
     * @param shadow
     *            shadow map renderer, set to null if shadow mapping should be disabled
     * @param sizeX
     *            grid size in x direction
     * @param sizeZ
     *            grid size in z direction
     */
    public BlockAnimator(GfxEngine engine, ShadowRenderPass shadow, int sizeX, int sizeZ) {
        mSizeX = sizeX;
        mSizeZ = sizeZ;
        
        // limit grid size
        if (mSizeX < 1) {
            mSizeX = 1;
        } else if (mSizeX > MAX_SIZE_X) {
            mSizeX = MAX_SIZE_X;
        }
        if (mSizeZ < 1) {
            mSizeZ = 1;
        } else if (mSizeZ > MAX_SIZE_Z) {
            mSizeZ = MAX_SIZE_Z;
        }
        
        // create texture
        mTexture = engine.getTextureManager().createEmptyTexture();
        TextureProperties props = new TextureProperties();
        props.magFilter = MagFilterMethod.NEAREST;
        props.minFilter = MinFilterMethod.NEAREST;
        mTexture.setTextureProperties(props);
        mTextureData = BufferHelper.createIntBuffer(MAX_SIZE_X * MAX_SIZE_Z);
        updateTexture(engine.getState());
        
        // generate mesh data
        FloatList positions = new FloatList(mSizeX * mSizeZ * 60);
        FloatList normalsUvs = new FloatList(mSizeX * mSizeZ * 100);
        IntList indices = new IntList(mSizeX * mSizeZ * 90);
        for (int z = 0; z < mSizeZ; z++) {
            float v = (z + 0.5f) / (float) MAX_SIZE_Z;
            for (int x = 0; x < mSizeX; x++) {
                float u = (x + 0.5f) / (float) MAX_SIZE_X;
                float px = mSizeX - x * 2 - 1;
                float pz = mSizeZ - z * 2 - 1;
                createBlocks(px, pz, u, v, positions, normalsUvs, indices);
            }
        }
        
        // create attribute binders for mesh data
        // positions are permanently stored in a FloatBuffer for easy modification
        mPositionBuffer = positions.asBuffer();
        ShaderAttributeBinder posBinder = ShaderAttributeBinder.createFloatBufferBinder(mPositionBuffer, 3, 12);
        // normals and texture coordinates are static
        int[] buf = new int[1];
        glGenBuffers(1, buf, 0);
        glBindBuffer(GL_ARRAY_BUFFER, buf[0]);
        glBufferData(GL_ARRAY_BUFFER, normalsUvs.size() * 4, normalsUvs.asBuffer(), GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        ShaderAttributeBinder normalBinder = ShaderAttributeBinder.createVboBufferBinder(buf[0], 3, 20);
        ShaderAttributeBinder uvBinder = ShaderAttributeBinder.createVboBufferBinder(buf[0], 2, 20);
        uvBinder.setOffset(3);
        
        // create index buffer as a ShortBuffer
        ShortBuffer idxBuf = BufferHelper.createShortBuffer(indices.size());
        for (int i = 0; i < indices.size(); i++) {
            idxBuf.put((short) indices.get(i));
        }
        idxBuf.rewind();
        
        // create Mesh
        mBlockMesh = new Mesh(idxBuf, posBinder, normalBinder, uvBinder, null);
        if(shadow != null) {
            mBlockMesh.setShader(ShadowShader.createPhongShadowShader(engine.getShaderManager(), mTexture, shadow));
            //mBlockMesh.setShader(ShadowShader.createGouraudShadowShader(mengine.getShaderManager(), Texture, shadow));
        } else {
            mBlockMesh.setShader(SimpleShader.createPhongTextureShader(engine.getShaderManager(), mTexture));
            //mBlockMesh.setShader(SimpleShader.createGouraudTextureShader(engine.getShaderManager(), mTexture));
        }

        // create block array
        mBlocks = new Block[mSizeX * mSizeZ];
        for (int i = 0; i < mBlocks.length; i++) {
            mBlocks[i] = new Block(this, i * 60);
        }
    }
    
    public Texture getTexture() {
        return mTexture;
    }
    
    /**
     * Returns the animated block grid. Add this to your scene.
     * 
     * @return the animated block grid
     */
    public Mesh getMesh() {
        return mBlockMesh;
    }
    
    /**
     * Returns the block at the specified index position.
     * 
     * @param x
     *            x index of the block
     * @param z
     *            z index of the block
     * @return the block at the specified index
     */
    public Block getBlockAt(int x, int z) {
        return mBlocks[z * mSizeX + x];
    }
    
    /**
     * Returns the {@link Block} hit by the specified {@link Ray} or null if no Block is hit.
     * 
     * @param pickRay
     *            Ray to test
     * @return the hit block or null
     */
    public Block getHitBlock(Ray pickRay) {
        Block hit = null;
        float dist = Float.MAX_VALUE;
        for (Block block : mBlocks) {
            float d = block.computeHitDistanceSqr(pickRay);
            if (d < dist) {
                dist = d;
                hit = block;
            }
        }
        return hit;
    }
    
    /**
     * Returns the {@link FloatBuffer} that contains the vertex positions.
     * 
     * @return the {@link FloatBuffer} that contains the vertex positions
     */
    protected FloatBuffer getVertexBuffer() {
        return mPositionBuffer;
    }
    
    /**
     * Call this from {@link GfxEngineListener#onRenderFrame(GfxEngine)} to animate the block grid.
     * 
     * @param state
     *            current graphics engine state
     */
    public void interpolateHeights(GfxState state) {
        long t = System.currentTimeMillis();

        for (int i = 0; i < mBlocks.length; i++) {
            setBlockColor(i, mBlocks[i].interpolateHeight(t));
        }
        // update block colors
        updateTexture(state);
    }
    
    /**
     * Sets the color of a block.
     * 
     * @see GlMath#packedColor(float, float, float, float)
     * @see GlMath#packedHsvColor(float, float, float, float)
     * 
     * @param blockIdx
     *            block index
     * @param blockColor
     *            color of the block
     */
    private void setBlockColor(int blockIdx, int blockColor) {
        int x = blockIdx % mSizeX;
        int z = blockIdx / mSizeX;
        int texIdx = z * MAX_SIZE_X + x;
        mTextureData.put(texIdx, blockColor);
    }
    
    /**
     * Updates the texture that holds the block colors.
     * 
     * @param state
     *            graphics engine state
     */
    private void updateTexture(GfxState state) {
        state.bindTexture(mTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, MAX_SIZE_X, MAX_SIZE_Z, 0, GL_RGBA, GL_UNSIGNED_BYTE, mTextureData);
    }

    /**
     * Creates a block with the specified parameters.
     */
    private void createBlocks(float x, float z, float u, float v, FloatList vp, FloatList vnt, IntList fi) {
        float[] pos = {
            // front
                x - 0.9f, 0.9f, z + 1.0f,
                x - 0.9f, 0.0f, z + 1.0f,
                x + 0.9f, 0.0f, z + 1.0f,
                x + 0.9f, 0.9f, z + 1.0f,
            // rear 
                x - 0.9f, 0.9f, z - 1.0f,
                x - 0.9f, 0.0f, z - 1.0f,
                x + 0.9f, 0.0f, z - 1.0f,
                x + 0.9f, 0.9f, z - 1.0f,
            // left 
                x - 1.0f, 0.0f, z + 0.9f,
                x - 1.0f, 0.0f, z - 0.9f,
                x - 1.0f, 0.9f, z - 0.9f,
                x - 1.0f, 0.9f, z + 0.9f,
            // right 
                x + 1.0f, 0.0f, z + 0.9f,
                x + 1.0f, 0.0f, z - 0.9f,
                x + 1.0f, 0.9f, z - 0.9f,
                x + 1.0f, 0.9f, z + 0.9f,
            // top
                x - 0.9f, 1.0f, z + 0.9f,
                x - 0.9f, 1.0f, z - 0.9f,
                x + 0.9f, 1.0f, z - 0.9f,
                x + 0.9f, 1.0f, z + 0.9f,
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
            // top
                 0.0f,  1.0f,  0.0f,
                 0.0f,  1.0f,  0.0f,
                 0.0f,  1.0f,  0.0f,
                 0.0f,  1.0f,  0.0f,
        };
        float[] uvs = {
            // front
                 u, v,
                 u, v,
                 u, v,
                 u, v,
            // rear
                 u, v,
                 u, v,
                 u, v,
                 u, v,
            // left
                 u, v,
                 u, v,
                 u, v,
                 u, v,
            // right
                 u, v,
                 u, v,
                 u, v,
                 u, v,
            // top
                 u, v,
                 u, v,
                 u, v,
                 u, v,
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
            // top
                16, 18, 17,
                16, 19, 18,
            // front top
                 0,  3, 16,
                16,  3, 19,
            // front left
                 1,  0, 11,
                 8,  1, 11,
            // front left
                 3,  2, 12,
                 3, 12, 15,
            // rear top
                 7,  4, 17,
                 7, 17, 18,
            // rear left
                 4,  5, 10,
                 5,  9, 10,
            // rear right
                 6,  7, 14,
                13,  6, 14,
            // top left
                16, 17, 11,
                11, 17, 10,
            // top right
                18, 19, 14,
                14, 19, 15,
            // top left front
                16, 11,  0,
            // top right front
                19,  3, 15,
            // top left rear
                17,  4, 10,
            // top right rear
                18, 14,  7
        };

        int iBase = vp.size() / 3;
        
        vp.add(pos);
        for (int i = 0, j = 0; i < norms.length; i += 3, j += 2) {
            vnt.add(norms[i]);
            vnt.add(norms[i+1]);
            vnt.add(norms[i+2]);
            vnt.add(uvs[j]);
            vnt.add(uvs[j+1]);
        }
        for (int i=0; i<indcs.length; i++) {
            fi.add(indcs[i] + iBase);
        }
    }
}
