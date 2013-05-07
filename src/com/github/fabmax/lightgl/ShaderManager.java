package com.github.fabmax.lightgl;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_TRUE;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glUseProgram;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.util.Log;
import android.util.SparseIntArray;

/**
 * The ShaderManager handles loading and binding of shaders.
 * 
 * @author fabmax
 *
 */
public class ShaderManager {
    private static final String TAG = "ShaderManager";

    // context is needed to load assets
    private Context mContext;
    
    // map that holds all generated shader handles
    private SparseIntArray mShaderHandles = new SparseIntArray();
    // currently bound shader
    private Shader mBoundShader;
    
    /**
     * Creates a new ShaderManager object.
     * 
     * @param context
     *            Application context, is needed to load files from assets directory
     */
    protected ShaderManager(Context context) {
        mContext = context;
    }
    
    /**
     * Binds the specified shader. If the shader is not already bound its onBind() method is called.
     * 
     * @param state
     *            current graphics engine state
     * @param shader
     *            shader to be bound
     */
    public void bindShader(GfxState state, Shader shader) {
        if (shader != mBoundShader) {
            mBoundShader = shader;
            
            if(shader != null) {
                // bind shader program
                glUseProgram(shader.getShaderHandle());
                // notify shader that it was bound
                shader.onBind(state);
            } else {
                // clear used shader
                glUseProgram(0);
            }
        }
    }

    /**
     * Returns the currently bound shader.
     * 
     * @return the currently bound shader
     */
    public Shader getBoundShader() {
        return mBoundShader;
    }
    
    /**
     * Loads the shader from the App's assets directory. The shader consists of 2 source files for
     * the vertex and the fragment shader. The source files need to be named name + "_vert.glsl" for
     * the vertex shader and name + "_frag.glsl" for the fragment shader.
     * 
     * @param name
     *            Shader name used to load the source files. The source files must be named name +
     *            "_vert.glsl" for the vertex shader and name + "_frag.glsl" for the fragment
     *            shader.
     * @return the GL shader handle; is 0 if there was an error while loading the shader
     */
    public int loadShader(String name) {
        // load vertex shader source code from assets
        String vertShaderSrc = loadSource(name + "_vert.glsl", mContext);
        String fragShaderSrc = loadSource(name + "_frag.glsl", mContext);
        
        // load shader from sources
        return loadShader(vertShaderSrc, fragShaderSrc);
    }
    
    /**
     * Compiles the shader from the specified source code strings.
     * 
     * @param vertexShaderSrc
     *            source code for the vertex shader
     * @param fragmentShaderSrc
     *            source code for the fragment shader
     * @return the GL shader handle; is 0 if there was an error while loading the shader
     */
    public int loadShader(String vertexShaderSrc, String fragmentShaderSrc) {
        // check if this shader code was already loaded
        int hashcode = (vertexShaderSrc + fragmentShaderSrc).hashCode();
        int shaderHandle = mShaderHandles.get(hashcode);
        if (shaderHandle != 0) {
            // this shader was already loaded, just return its handle
            return shaderHandle;
        }
        
        int shaderResult[] = new int[1];

        // create vertex shader object
        int vertShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertShader, vertexShaderSrc);
        glCompileShader(vertShader);

        // check compilation result
        glGetShaderiv(vertShader, GL_COMPILE_STATUS, shaderResult, 0);
        if (shaderResult[0] != GL_TRUE) {
            // shader compilation failed
            String log = glGetShaderInfoLog(vertShader);
            Log.e(TAG, "Error compiling vertex shader: " + log);
            glDeleteShader(vertShader);
            return 0;
        }

        // create fragment shader object
        int fragShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragShader, fragmentShaderSrc);
        glCompileShader(fragShader);

        // check compilation result
        glGetShaderiv(fragShader, GL_COMPILE_STATUS, shaderResult, 0);
        if (shaderResult[0] != GL_TRUE) {
            // shader compilation failed
            String log = glGetShaderInfoLog(fragShader);
            Log.e(TAG, "Error compiling fragment shader: " + log);
            glDeleteShader(vertShader);
            glDeleteShader(fragShader);
            return 0;
        }

        // link shader program
        shaderHandle = glCreateProgram();
        glAttachShader(shaderHandle, vertShader);
        glAttachShader(shaderHandle, fragShader);
        glLinkProgram(shaderHandle);

        // check linker result
        glGetProgramiv(shaderHandle, GL_LINK_STATUS, shaderResult, 0);
        if (shaderResult[0] != GL_TRUE) {
            // shader linkage failed
            String log = glGetProgramInfoLog(shaderHandle);
            Log.e(TAG, "Error linking shader: " + log);
            glDeleteProgram(shaderHandle);
            shaderHandle = 0;
        }

        // after linkage fragment and vertex shader are no longer needed
        glDeleteShader(vertShader);
        glDeleteShader(fragShader);

        // if everything went well put the shader handle into the handle map
        if(shaderHandle != 0) {
            mShaderHandles.put(hashcode, shaderHandle);
        }
        
        // return shader program handle
        return shaderHandle;
    }

    /*
     * Helper method that reads a asset file with shader source code into a String.
     */
    private static String loadSource(String assetName, Context context) {
        StringBuffer sBuf = new StringBuffer();
        
        try {
            // open asset
            InputStream in = context.getAssets().open(assetName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            
            // read shader source line by line
            String line = reader.readLine();
            while (line != null) {
                sBuf.append(line);
                sBuf.append('\n');
                line = reader.readLine();
            }           
        } catch (IOException e) {
            Log.e(TAG, "Error reading shader source " + assetName + ": " + e.getMessage());
        }
        
        // return read source
        return sBuf.toString();
    }
}
