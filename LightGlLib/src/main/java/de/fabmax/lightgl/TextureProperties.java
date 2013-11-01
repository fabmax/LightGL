package de.fabmax.lightgl;

import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_LINEAR_MIPMAP_LINEAR;
import static android.opengl.GLES20.GL_MIRRORED_REPEAT;
import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.GL_REPEAT;

public class TextureProperties {
    
    /**
     * Filter method for texture mapping on areas that are smaller than the original texture area.
     */
    public MinFilterMethod minFilter = MinFilterMethod.TRILINEAR;
    
    /**
     * Filter method for texture mapping on areas that are larger than the original texture area.
     */
    public MagFilterMethod magFilter = MagFilterMethod.LINEAR;
    
    /**
     * Texture wrapping in X direction.
     */
    public WrappingMethod xWrapping = WrappingMethod.REPEAT;

    /**
     * Texture wrapping in Y direction.
     */
    public WrappingMethod yWrapping = WrappingMethod.REPEAT;
    
    /**
     * Enumeration of texture filter methods for minification filters.
     */
    public enum MinFilterMethod {
        NEAREST(GL_NEAREST),
        LINEAR(GL_LINEAR),
        TRILINEAR(GL_LINEAR_MIPMAP_LINEAR);
        
        private final int mMethod;
        
        MinFilterMethod(int method) {
            mMethod = method;
        }
        
        public int getGlMethod() {
            return mMethod;
        }
    }

    /**
     * Enumeration of texture filter methods for magnifying filters.
     */
    public enum MagFilterMethod {
        NEAREST(GL_NEAREST),
        LINEAR(GL_LINEAR);
        
        private final int mMethod;
        
        MagFilterMethod(int method) {
            mMethod = method;
        }
        
        public int getGlMethod() {
            return mMethod;
        }
    }
    
    /**
     * Enumeration for texture clamping methods.
     */
    public enum WrappingMethod {
        CLAMP(GL_CLAMP_TO_EDGE),
        REPEAT(GL_REPEAT),
        MIRRORED_REPEAT(GL_MIRRORED_REPEAT);
        
        private final int mMethod;
        
        WrappingMethod(int method) {
            mMethod = method;
        }
        
        public int getGlMethod() {
            return mMethod;
        }
    }
}
