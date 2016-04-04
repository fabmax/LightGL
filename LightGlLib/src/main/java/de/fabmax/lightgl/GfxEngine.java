package de.fabmax.lightgl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.util.Log;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import de.fabmax.lightgl.physics.PhysicsEngine;
import de.fabmax.lightgl.scene.Node;

import static android.opengl.GLES20.*;

/**
 * The central graphics engine management class.
 *
 * @author fabmax
 *
 */
public class GfxEngine implements Renderer {

    private static final String TAG = "GfxEngine";

    private final ShaderManager mShaderManager;
    private final TextureManager mTextureManager;
    private final GfxState mState;
    private LightGlContext mGlContext;

    private final ArrayList<Light> mLights = new ArrayList<>();

    private Camera mCamera;
    private Node mScene;

    private GfxEngineListener mEngineListener;
    private RenderPass mPrePass;
    private RenderPass mMainPass;
    private boolean mClearScreen = true;

    private PhysicsEngine mPhysics;

    private long mLastFrameTime = System.currentTimeMillis();
    private long mMaxFrameInterval = 0;
    private float mFps = 60;

    /**
     * Creates a new GfxEngine object.
     *
     * @param context       the application context
     * @param usePhysics    true to create a {@link de.fabmax.lightgl.physics.PhysicsEngine}
     */
    public GfxEngine(Context context, boolean usePhysics) {
        // ensure that we have the application context
        context = context.getApplicationContext();

        mShaderManager = new ShaderManager(context);
        mTextureManager = new TextureManager(context);
        mState = new GfxState(mShaderManager);

        // by default the scene is directly rendered to the screen
        mMainPass = new ScreenRenderPass();

        // by default a standard perspective camera is used
        mCamera = new PerspectiveCamera();

        setContext(new LightGlContext(this));

        if (usePhysics) {
            mPhysics = new PhysicsEngine();
        }
    }

    /**
     * Sets the context that is passed to nodes during rendering. However, the given context must have been created for
     * this GfxEngine instance.
     *
     * @param context    the context used during rendering
     */
    public void setContext(LightGlContext context) {
        if (context.getEngine() != this) {
            throw new IllegalArgumentException("Context was created for another instance of GfxEngine");
        }
        mGlContext = context;
    }

    /**
     * Returns the used {@link de.fabmax.lightgl.physics.PhysicsEngine}. This method only returns an
     * non-null value if this GfxEngine was created with usePhysics = true.
     *
     * @return the used {@link de.fabmax.lightgl.physics.PhysicsEngine}
     */
    public PhysicsEngine getPhysicsEngine() {
        return mPhysics;
    }

    /**
     * Is called by a {@link android.opengl.GLSurfaceView} to render the scene.
     *
     * @see Renderer#onDrawFrame(GL10)
     */
    @Override
    public void onDrawFrame(GL10 unused) {
        doTimeControl();

        mState.reset(mGlContext);
        if (mPhysics != null) {
            mPhysics.synchronizeBodyConfigurations();
        }

        if (mEngineListener != null) {
            mEngineListener.onRenderFrame(mGlContext);
        }

        if (mPrePass != null) {
            mPrePass.onRender(mGlContext);
        }

        if (mClearScreen) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        }

        if (mMainPass != null) {
            if (mCamera != null) {
                mCamera.animate(1.0f / mFps);
                mCamera.setup(mState);
            }

            if (mEngineListener != null) {
                mEngineListener.onRenderMainPass(mGlContext);
            }
            mMainPass.onRender(mGlContext);
        }

        int err = glGetError();
        if (err != 0) {
            Log.e(TAG, "glError " + err + ": " + GLU.gluErrorString(err));
        }
    }

    /**
     * Determines current FPS and controls the maximum frame rate if a limit is set.
     */
    private void doTimeControl() {
        long t = System.currentTimeMillis();
        long tLast = mLastFrameTime;

        if (mMaxFrameInterval > 0) {
            long tDif = t - mLastFrameTime;

            // limit framerate
            long s = mMaxFrameInterval - tDif;
            if(s > 0) {
                try {
                    Thread.sleep(s);
                } catch (InterruptedException e) {
                    // mmh whatever
                }
                mLastFrameTime += mMaxFrameInterval;
            } else {
                mLastFrameTime = t;
            }
        } else {
            mLastFrameTime = t;
        }

        // update fps
        float deltaT = (mLastFrameTime - tLast) / 1000.0f;
        if (deltaT > 0) {
            mFps = mFps * 0.85f + 0.15f / deltaT;
        }
    }

    /**
     * Is called on change of widget size.
     *
     * @see Renderer#onSurfaceChanged(GL10, int, int)
     */
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        Log.d(TAG, "onSurfaceChanged: w:" + width + ", h:" + height);

        // update GL viewport size
        mState.setViewport(0, 0, width, height);

        if (mEngineListener != null) {
            mEngineListener.onViewportChange(width, height);
        }
    }

    /**
     * Is called on startup and sets the default GL configuration.
     *
     * @see Renderer#onSurfaceCreated(GL10, EGLConfig)
     */
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated");

        // drop all existing texture and shader handles
        mTextureManager.newGlContext();
        mShaderManager.newGlContext();

        // setup GL stuff
        glClearColor(0, 0, 0, 1);
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // notify registered listener that now is the right time to load scene data
        if (mEngineListener != null) {
            mEngineListener.onLoadScene(mGlContext);
        }
    }

    /**
     * Call this from your {@link android.app.Activity#onPause()}.
     */
    public void onPause() {
        if (mPhysics != null) {
            mPhysics.onPause();
        }
    }

    /**
     * Call this from your {@link android.app.Activity#onResume()}.
     */
    public void onResume() {
        if (mPhysics != null) {
            mPhysics.onResume();
        }
    }

    /**
     * Call this from your {@link android.app.Activity#onDestroy()}.
     */
    public void onDestroy() {
        if (mPhysics != null) {
            mPhysics.onDestroy();
        }
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
     * Sets the pre-pass renderer. The pre-pass is processed before the main-pass.
     *
     * @param prePass
     *            the pre-pass renderer to set
     */
    public void setPreRenderPass(RenderPass prePass) {
        mPrePass = prePass;
    }

    /**
     * Returns the pre-pass renderer
     */
    public RenderPass getPreRenderPass() {
        return mPrePass;
    }

    /**
     * Sets the main-pass renderer.
     *
     * @param mainPass
     *            the main-pass renderer to set
     */
    public void setMainRenderPass(RenderPass mainPass) {
        mMainPass = mainPass;
    }

    /**
     * Returns the main-pass renderer
     */
    public RenderPass getMainRenderPass() {
        return mMainPass;
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
     * Returns the {@link TextureManager} of this GfxEngine.
     *
     * @return the {@link TextureManager} of this GfxEngine
     */
    public TextureManager getTextureManager() {
        return mTextureManager;
    }

    /**
     * Returns the {@link GfxState} of this GfxEngine.
     *
     * @return the {@link GfxState} of this GfxEngine
     */
    public GfxState getState() {
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
     * @param cam
     *            the camera to set
     */
    public void setCamera(Camera cam) {
        mCamera = cam;
    }

    /**
     * Returns the current frame rate. The frame rate is updated on every frame; however it is
     * filtered to get more steady values.
     *
     * @return the current frame rate
     */
    public float getFps() {
        return mFps;
    }

    /**
     * Returns the currently set maximum frame rate. If no maximum frame rate is set 0 is returned.
     *
     * @return the maximum frame rate
     */
    public float getMaximumFps() {
        if (mMaxFrameInterval == 0) {
            return 0.0f;
        } else {
            return 1000.0f / (float) mMaxFrameInterval;
        }
    }

    /**
     * Sets the maximum frame rate. fps = 0 disables the frame rate limit.
     *
     * @param fps
     *            the maximum frame rate
     */
    public void setMaximumFps(float fps) {
        if (fps == 0.0f) {
            mMaxFrameInterval = 0;
        } else {
            mMaxFrameInterval = Math.round(1000.0f / fps);
        }
    }

    /**
     * Sets whether to automatically clear the screen buffer before a new frame is rendered. By default this is enabled.
     *
     * @param enabled    true to enable automatic screen clearing
     */
    public void setClearScreen(boolean enabled) {
        mClearScreen = enabled;
    }
}
