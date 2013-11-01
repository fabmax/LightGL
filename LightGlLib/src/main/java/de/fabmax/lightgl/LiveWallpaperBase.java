package de.fabmax.lightgl;

import android.annotation.TargetApi;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

import de.fabmax.lightgl.util.GlConfiguration;

/**
 * Base class for implementing live wallpapers. To implement a live wallpaper you have to extend
 * this class and return an implementation of {@link GlWallpaperEngine} to handle all
 * important wallpaper and rendering callbacks.
 * 
 * @author fabmax
 * 
 */
public abstract class LiveWallpaperBase extends WallpaperService {
    
    /**
     * Returns the wallpaper engine.
     */
    @Override
    public Engine onCreateEngine() {
        // return wallpaper engine implementation
        return getEngine();
    }
    
    /**
     * Must be implemented by concrete implementations to handle GfxEngine and Wallpaper callbacks.
     * 
     * @return {@link GlWallpaperEngine} that handles the wallpaper content
     */
    public abstract GlWallpaperEngine getEngine();

    /**
     * Base class for OpenGL ES 2.0 enabled live wallpaper engine.
     */
    public abstract class GlWallpaperEngine extends Engine implements GfxEngineListener {
        private static final String TAG = "LiveWallpaperBase.WallpaperEngine";

        private final GlConfiguration mConfigChooser;
        private final GfxEngine mGfxEngine;
        private WallpaperGlSurfaceView mGlView;
        private boolean mCreated = false;

        /**
         * The default constructor initializes the {@link GfxEngine}; however OpenGL methods can
         * only be called after the Surface is created.
         *
         * @param usePhysics    true to enable physics simulation
         */
        public GlWallpaperEngine(boolean usePhysics) {
            this(null, usePhysics);
        }

        /**
         * The constructor initializes the {@link GfxEngine} and sets a {@link GlConfiguration} as
         * desired OpenGL configuration.
         * 
         * @param configChooser    the desired OpenGL configuration
         * @param usePhysics       true to enable physics simulation
         */
        public GlWallpaperEngine(GlConfiguration configChooser, boolean usePhysics) {
            mConfigChooser = configChooser;
            mGfxEngine = new GfxEngine(LiveWallpaperBase.this, usePhysics);
            mGfxEngine.setEngineListener(this);
        }
        
        /**
         * Called on Engine initialization.
         */
        @Override
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);

            // create the fake GLView that handles the GL context and enable GLES 2.0
            mGlView = new WallpaperGlSurfaceView(LiveWallpaperBase.this);
            mGlView.setEGLContextClientVersion(2);
            if (mConfigChooser != null) {
                mGlView.setEGLConfigChooser(mConfigChooser);
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                // if available preserve the GL context for faster visibility changes
                mGlView.setPreserveEGLContextOnPause(true);
            }
            
            // register graphics engine as GL renderer
            mGlView.setRenderer(mGfxEngine);
            mCreated = true;

            // enable homescreen touch feedback
            setTouchEventsEnabled(true);
        }

        /**
         * Is called if the user changes the homescreen.
         * 
         * @param xOffset         normalized x offset in range 0 .. 1
         * @param yOffset         normalized y offset in range 0 .. 1
         * @param xOffsetStep     step size between two homescreens for normalized x offset
         * @param yOffsetStep     step size between two homescreens for normalized y offset
         * @param xPixelOffset    not used?
         * @param yPixelOffset    not used?
         */
        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep,
                float yOffsetStep, int xPixelOffset, int yPixelOffset) {
            // the default implementation does nothing
        }

        /**
         * Called if the visibility of the wallpaper changes. Pauses / resumes the rendering thread.
         */
        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            Log.d(TAG, "visibility changed: " + visible);
            
            // pause / resume the render thread
            if (mCreated) {
                if (visible) {
                    mGlView.onResume();
                } else {
                    mGlView.onPause();
                }
            }
        }

        /**
         * Forwards the onDestroy event to the internal GLSurfaceView.
         */
        @Override
        public void onDestroy() {
            Log.d(TAG, "Engine: onDestroy");
            super.onDestroy();
            
            if (mGlView != null) {
                mGlView.onDestroy();
            }
        }

        /**
         * Forwards the onSurfaceChanged event to the internal GLSurfaceView.
         */
        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            Log.d(TAG, "Engine: onSurfaceChanged");
            
            if (mGlView != null) {
                mGlView.surfaceChanged(holder, format, width, height);
            }
        }

        /**
         * Forwards the onSurfaceCreated event to the internal GLSurfaceView.
         */
        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            Log.d(TAG, "Engine: onSurfaceCreated");
            
            if (mGlView != null) {
                mGlView.surfaceCreated(holder);
            }
        }

        /**
         * Forwards the onSurfaceDestroyed event to the internal GLSurfaceView.
         */
        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            Log.d(TAG, "Engine: onSurfaceDestroyed");

            if (mGlView != null) {
                mGlView.surfaceDestroyed(holder);
            }
        }

        /**
         * A custom GLSurfaceView that uses the SurfaceHolder of the WallpaperEngine as rendering
         * target.
         */
        private class WallpaperGlSurfaceView extends GLSurfaceView {
            public WallpaperGlSurfaceView(Context context) {
                super(context);
            }

            @Override
            public SurfaceHolder getHolder() {
                // this returns the wallpaper engine's SurfaceHolder instead of the standard one
                return getSurfaceHolder();
            }
            
            public void onDestroy() {
                // GLSurfaceView has no onDestroy() method, this is what comes closest 
                onDetachedFromWindow();
            }
        }
        
        /**
         * Returns the used {@link GfxEngine}.
         * 
         * @return the used {@link GfxEngine}
         */
        public GfxEngine getGfxEngine() {
            return mGfxEngine;
        }
    }
    
}
