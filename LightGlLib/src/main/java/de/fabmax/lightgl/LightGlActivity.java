package de.fabmax.lightgl;

import android.annotation.TargetApi;
import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.Log;

import de.fabmax.lightgl.util.GlConfiguration;

/**
 * Base class for Activites that use LightGl.
 *
 * @author fabmax
 */
public abstract class LightGlActivity extends Activity implements GfxEngineListener {

    private static final String TAG = "LightGlActivity";

    protected GLSurfaceView mGlView;
    // main graphics engine object
    protected GfxEngine mEngine;
    // number fo samples > 1 for anti aliasing
    private int mNumSamples = 0;

    private boolean mCreated = false;

    private boolean mLogFps = false;
    private long mLastFpsOut = 0;

    /**
     * Same as {@link #createEngine(android.opengl.GLSurfaceView, boolean)} but creates a new
     * GLSurfaceView and sets it as this Activity's content view.
     *
     * @param usePhysics    true to enable physics simulation
     */
    protected void createEngine(boolean usePhysics) {
        // create and initialize the GLSurfaceView
        GLSurfaceView glView = new GLSurfaceView(this);
        createEngine(glView, usePhysics);

        // set the GL view as Activity's only content
        setContentView(glView);
    }

    /**
     * Call this method from your Activity's onCreate() method and pass the GLSurfaceView from your
     * layout.
     *
     * @param glView        GLSurfaceView to use for graphics output
     * @param usePhysics    true to enable physics simulation
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected void createEngine(GLSurfaceView glView, boolean usePhysics) {
        // initialize graphics engine
        mEngine = new GfxEngine(getApplicationContext(), usePhysics);
        mEngine.setEngineListener(this);

        mGlView = glView;

        // enable GLES 2.0
        mGlView.setEGLContextClientVersion(2);
        // set desired GL config
        GlConfiguration config = new GlConfiguration();
        config.setNumSamples(mNumSamples);
        mGlView.setEGLConfigChooser(config);
        // register graphics engine as GL renderer
        mGlView.setRenderer(mEngine);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // if available preserve the GL context for faster visibility changes
            mGlView.setPreserveEGLContextOnPause(true);
        }

        mCreated = true;
    }

    /**
     * Sets the number of samples to request for the GL configuration. A value greater than 1
     * enables anti aliasing. This method must be called before {@link #createEngine(boolean)}.
     *
     * @param numSamples Number of samples to use for rendering
     */
    public void setNumSamples(int numSamples) {
        if (mCreated) {
            throw new RuntimeException("setNumSamples must be called before " +
                    "LightGlActivity.createEngine()");
        }
        mNumSamples = numSamples;
    }

    /**
     * Call this method to en-/disable fps logging. If enabled the rendered frames per second are
     * logged to logcat once a second.
     *
     * @param enabled true to enable fps logging
     */
    public void setLogFramesPerSecond(boolean enabled) {
        mLogFps = enabled;
    }

    /**
     * Returns the underlying {@link android.opengl.GLSurfaceView}.
     *
     * @return the underlying {@link android.opengl.GLSurfaceView}
     */
    public GLSurfaceView getGlSurfaceView() {
        return mGlView;
    }

    /**
     * Returns the {@link de.fabmax.lightgl.GfxEngine}.
     *
     * @return the {@link de.fabmax.lightgl.GfxEngine}
     */
    public GfxEngine getGfxEngine() {
        return mEngine;
    }

    /**
     * Called every time before a frame is rendered. Override this method to animate your camera or
     * scene or do other dynamic stuff. However, your should call super.onRenderFrame() when
     * overriding this method.
     *
     * @see #onRenderMainPass(LightGlContext)
     * @see de.fabmax.lightgl.GfxEngineListener#onRenderFrame(LightGlContext)
     *
     * @param glContext    the graphics engine context
     */
    @Override
    public void onRenderFrame(LightGlContext glContext) {
        // calculate frames per second and log them every second
        if (mLogFps) {
            long t = System.currentTimeMillis();
            if(t > mLastFpsOut + 1000) {
                mLastFpsOut = t;
                Log.d(TAG, "Fps: " + glContext.getEngine().getFps());
            }
        }
    }

    /**
     * Called every time before the main-pass is rendered. The default implementation does nothing.
     * In contrast to {@link #onRenderFrame(LightGlContext)} this method is called after the pre render
     * pass is run (e.g. {@link de.fabmax.lightgl.ShadowRenderPass} and the camera is set up.
     *
     * @see de.fabmax.lightgl.GfxEngineListener#onRenderMainPass(LightGlContext)
     *
     * @param glContext    the graphics engine
     */
    @Override
    public void onRenderMainPass(LightGlContext glContext) {
        // default implementation does nothing
    }

    /**
     * Called if the viewport size (size of the {@link android.opengl.GLSurfaceView} changes.
     * The default implementation does nothing.
     *
     * @param width new viewport width in pixels
     * @param height new viewport height in pixels
     */
    @Override
    public void onViewportChange(int width, int height) {
        // default implementation does nothing
    }

    /**
     * Called if the Activity is paused. Pauses the GL thread.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mEngine.onPause();
        mGlView.onPause();
    }

    /**
     * Called if the Activity is resumed. Resumes the GL thread.
     */
    @Override
    protected void onResume() {
        super.onResume();
        mEngine.onResume();
        mGlView.onResume();
    }

    /**
     * Called if the Activity is destroyed. Does some cleanup.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mEngine.onDestroy();
    }
}
