package com.github.fabmax.lightgl;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_CULL_FACE;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glEnable;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;

import com.github.fabmax.lightgl.scene.Node;

/**
 * The central engine management class.
 * 
 * @author fabmax
 *
 */
public class GfxEngine implements Renderer {

    private ShaderManager mShaderManager;
    private GfxState mState;
    
    private ArrayList<Light> mLights = new ArrayList<Light>();
    private Node mScene;
    
    private Camera mCamera;
    private int mViewportW;
    private int mViewportH;
    
    private GfxEngineListener mEngineListener;
    
    /**
     * Creates a new GfxEngine object.
     * 
     * @param context the application context
     */
    public GfxEngine(Context context) {
        // ensure that we have the application context
        context = context.getApplicationContext();
        
        mShaderManager = new ShaderManager(context);
        mState = new GfxState(this, mShaderManager);
        
        // by default a standard perspective camera is used
        mCamera = new PerspectiveCamera();
    }
    
    /**
     * Sets the specified {@link GfxEngineListener} as engine listener. The engine listener is
     * called on certain GL events.
     * 
     * @param listener
     *            the engine listener to set
     */
    public void setEngineListener(GfxEngineListener listener) {
        mEngineListener = listener;
    }

    /**
     * Returns the {@link ShaderManager} of this GfxEngine.
     * 
     * @return the {@link ShaderManager} of this GfxEngine
     */
    public ShaderManager getShaderManager() {
        return mShaderManager;
    }

    /**
     * Returns the {@link GfxState} of this GfxEngine.
     * 
     * @return the {@link GfxState} of this GfxEngine
     */
    public GfxState getEngineState() {
        return mState;
    }

    /**
     * Adds a light to the scene.
     * 
     * @param light
     *            the light to add to the scene
     */
    public void addLight(Light light) {
        mLights.add(light);
    }

    /**
     * Removes a light from the scene.
     * 
     * @param light
     *            the light to remove from the scene
     */
    public void removeLight(Light light) {
        mLights.remove(light);
    }

    /**
     * Returns a list with all lights in the scene.
     * 
     * @return the list with all lights in the scene.
     */
    public ArrayList<Light> getLights() {
        return mLights;
    }
    
    /**
     * Returns the scene Node.
     * 
     * @return the scene node
     */
    public Node getScene() {
        return mScene;
    }

    /**
     * Sets the scene Node.
     * 
     * @param scene
     *            the scene Node to set
     */
    public void setScene(Node scene) {
        this.mScene = scene;
    }

    /**
     * Sets the scene background color.
     */
    public void setBackgroundColor(float red, float green, float blue) {
        glClearColor(red, green, blue, 0);
    }
    
    /**
     * Returns the active camera.
     * 
     * @return the active camera
     */
    public Camera getCamera() {
        return mCamera;
    }
    
    /**
     * Sets the specified camera.
     * 
     * @param cam the camera to set
     */
    public void setCamera(Camera cam) {
        mCamera = cam;
        if (cam != null) {
            cam.setViewport(mViewportW, mViewportH);
        }
    }

    /**
     * Is called to render the scene.
     * 
     * @see Renderer#onDrawFrame(GL10)
     */
    @Override
    public void onDrawFrame(GL10 unused) {
        // clear screen
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        if(mCamera == null) {
            // we need a valid camera to draw the scene
            return;
        }
        mState.initNextFrame(mCamera);

        if(mEngineListener != null) {
            mEngineListener.onRenderFrame(this);
        }
        
        if (mScene != null) {
            mScene.render(mState);
        }
    }

    /**
     * Is called on change of widget size.
     * 
     * @see Renderer#onSurfaceChanged(GL10, int, int)
     */
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        mViewportW = width;
        mViewportH = height;
        
        if (mCamera != null) {
            mCamera.setViewport(width, height);
        }
    }

    /**
     * Is called on startup and sets the default GL configuration.
     * 
     * @see Renderer#onSurfaceCreated(GL10, EGLConfig)
     */
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // setup GL stuff
        glClearColor(0, 0, 0, 1);
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        
        // notify registered listener that now is the right time to load scene data
        if(mEngineListener != null) {
            mEngineListener.onLoadScene(this);
        }
    }
}
