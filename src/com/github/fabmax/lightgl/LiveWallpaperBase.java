package com.github.fabmax.lightgl;

import android.annotation.TargetApi;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

/**
 * Base class for implementing live wallpapers.
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
        // return Wallpaper Engine implementation
        return new WallpaperEngine();
    }
    
    /**
     * Must be implemented by concrete implementations to handle GfxEngine and Wallpaper callbacks.
     * 
     * @return WallpaperEngineListener that handles the wallpaper logic
     */
    public abstract WallpaperEngineListener getEngineListener();
    
    /**
     * WallpaperEngineListener defines all methods that must be implemented by live wallpapers.
     */
    public interface WallpaperEngineListener extends GfxEngineListener {
        
        /**
         * Is called if the user changes the homescreen.
         * 
         * @param xOffset
         *            normalized x offset in range 0 .. 1
         * @param yOffset
         *            normalized y offset in range 0 .. 1
         * @param xOffsetStep
         *            step size between two homescreens for normalized x offset
         * @param yOffsetStep
         *            step size between two homescreens for normalized y offset
         * @param xPixelOffset
         * @param yPixelOffset
         */
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep,
                float yOffsetStep, int xPixelOffset, int yPixelOffset);
        
        /**
         * Is called when the user touches the homescreen.
         * 
         * @param event the MotionEvent
         */
        public void onTouchEvent(MotionEvent event);
    }

    /**
     * Implementation of a Live Wallpaper Engine.
     */
    private class WallpaperEngine extends Engine {
        private static final String TAG = "LiveWallpaperBase.WallpaperEngine";

        private WallpaperEngine.WallpaperGlSurfaceView mGlView;
        private boolean mCreated = false;
        private WallpaperEngineListener mEngineListener;
        private GfxEngine mGfxEngine;

        /**
         * Called on Engine initialization.
         */
        @Override
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);

            mEngineListener = getEngineListener();
            mGfxEngine = new GfxEngine(LiveWallpaperBase.this);
            mGfxEngine.setEngineListener(mEngineListener);

            mGlView = new WallpaperGlSurfaceView(LiveWallpaperBase.this);
            // enable GLES 2.0
            mGlView.setEGLContextClientVersion(2);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                // if the EGL context is not preserved the onPause() / onResume() for visibility changes
                // does not work. However this API call is only available on honeycomb and above...
                mGlView.setPreserveEGLContextOnPause(true);
            }
            
            // register graphics engine as GL renderer
            mGlView.setRenderer(mGfxEngine);
            mCreated = true;

            setTouchEventsEnabled(true);
        }

        /**
         * Called when the user changes the homescreen.
         */
        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep,
                float yOffsetStep, int xPixelOffset, int yPixelOffset) {
            mEngineListener.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep,
                    xPixelOffset, yPixelOffset);
        }

        /**
         * Called when the user touches the homescreen.
         */
        @Override
        public void onTouchEvent(MotionEvent event) {
            mEngineListener.onTouchEvent(event);
        }

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
        
        @Override
        public void onDestroy() {
            Log.d(TAG, "Engine: onDestroy()");
            super.onDestroy();
            // TODO: free GL resources
            
            if (mGlView != null) {
                mGlView.onDestroy();
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            Log.d(TAG, "Engine: onSurfaceChanged");
            
            if (mGlView != null) {
                mGlView.surfaceChanged(holder, format, width, height);
            }
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            Log.d(TAG, "Engine: onSurfaceCreated");
            
            if (mGlView != null) {
                mGlView.surfaceCreated(holder);
            }
        }

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
                // this returns the Engine's SurfaceHolder instead of the standard one
                return getSurfaceHolder();
            }
            
            public void onDestroy() {
                onDetachedFromWindow();
            }
        }
    }
    
}
