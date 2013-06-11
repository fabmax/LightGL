package de.fabmax.lightgl.util;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.EGLConfigChooser;
import android.util.Log;

/**
 * The GlConfiguration determines the basic parameters used by OpenGL for rendering. Currently
 * settings for color depth, depth buffer resolution and multi-sampling / anti-aliasing are
 * supported. The GlConfiguration is set with
 * {@link GLSurfaceView#setEGLConfigChooser(EGLConfigChooser)}. If the desired configuration is
 * not supported by the hardware, a default configuration is used as fallback.
 * 
 * @author fabmax
 * 
 */
public class GlConfiguration implements EGLConfigChooser {

    private static final String TAG = "GlConfiguration";
    
    /**
     * This is the same configuration the Android framework uses by default. It is taken if the
     * desired configuration is not available on the device.
     */
    private static final int[] FALLBACK_CONFIG = {
        EGL10.EGL_RED_SIZE,         5,
        EGL10.EGL_GREEN_SIZE,       6,
        EGL10.EGL_BLUE_SIZE,        5,
        EGL10.EGL_ALPHA_SIZE,       0,
        EGL10.EGL_DEPTH_SIZE,       16,
        EGL10.EGL_RENDERABLE_TYPE,  4,           // EGL_OPENGL_ES2_BIT
        EGL10.EGL_NONE
    };
    
    private int mRed = 8;
    private int mGreen = 8;
    private int mBlue = 8;
    private int mAlpha = 0;
    private int mDepth = 16;
    private int mSamples = 0;
    
    private boolean mFoundMatchingConfig = false;
    
    /**
     * Sets the desired color depth for the individual color channels. Default is (8, 8, 8) for red,
     * green and blue, and 0 for alpha.
     * 
     * @param redBits
     *            color depth for red
     * @param greenBits
     *            color depth for green
     * @param blueBits
     *            color depth for blue
     * @param alphaBits
     *            color depth for alpha
     */
    public void setColorDepth(int redBits, int greenBits, int blueBits, int alphaBits) {
        mRed = redBits;
        mGreen = greenBits;
        mBlue = blueBits;
        mAlpha = alphaBits;
    }
    
    /**
     * Sets the depth buffer resolution. Default is 16 bits.
     * 
     * @param depthBits
     *            desired depth buffer resolution
     */
    public void setDepthBits(int depthBits) {
        mDepth = depthBits;
    }
    
    /**
     * Sets the number of samples per pixel. If value is > 0 multi-sampling is used. Set it to 0 to
     * disable multi-sampling. Default is 0.
     * 
     * @param numSamples
     *            desired number of samples
     */
    public void setNumSamples(int numSamples) {
        mSamples = numSamples;
    }
    
    /**
     * Returns true if this configuration was successfully set; false otherwise. The configuration
     * is set on GL context creation, hence calling this method before the GL context is created
     * will always result in false.
     * 
     * @return true if this configuration was successfully set; false otherwise
     */
    public boolean foundMatchingConfig() {
        return mFoundMatchingConfig;
    }
    
    /**
     * Called by {@link GLSurfaceView} on GL context creation. Tries to set the desired
     * configuration.
     */
    @Override
    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        int[] attribs = new int[] {
                EGL10.EGL_RED_SIZE,         mRed,
                EGL10.EGL_GREEN_SIZE,       mGreen,
                EGL10.EGL_BLUE_SIZE,        mBlue,
                EGL10.EGL_ALPHA_SIZE,       mAlpha,
                EGL10.EGL_DEPTH_SIZE,       mDepth,
                EGL10.EGL_SAMPLE_BUFFERS,   mSamples > 1 ? 1 : 0,
                EGL10.EGL_SAMPLES,          mSamples,
                EGL10.EGL_RENDERABLE_TYPE,  4,           // EGL_OPENGL_ES2_BIT
                EGL10.EGL_NONE
        };
        
        egl.eglChooseConfig(display, attribs, configs, configs.length, numConfigs);
        
        if (numConfigs[0] != 0) {
            Log.d(TAG, "Found valid EGL config");
            mFoundMatchingConfig = true;
            //printConfig(egl, display, configs[0]);
            return configs[0];
        } else {
            Log.w(TAG, "Did not found valid EGL config, using fallback config");
            mFoundMatchingConfig = false;
            egl.eglChooseConfig(display, FALLBACK_CONFIG, configs, configs.length, numConfigs);
            return configs[0];
        }
    }
    
    /**
     * Prints relevant attributes of a GL configuration to logcat.
     */
    protected void printConfig(EGL10 egl, EGLDisplay display, EGLConfig config) {
        int[] val = new int[1];
        egl.eglGetConfigAttrib(display, config, EGL10.EGL_RED_SIZE, val);
        Log.d(TAG, "RED_SIZE: " + val[0]);
        egl.eglGetConfigAttrib(display, config, EGL10.EGL_GREEN_SIZE, val);
        Log.d(TAG, "GREEN_SIZE: " + val[0]);
        egl.eglGetConfigAttrib(display, config, EGL10.EGL_BLUE_SIZE, val);
        Log.d(TAG, "BLUE_SIZE: " + val[0]);
        egl.eglGetConfigAttrib(display, config, EGL10.EGL_ALPHA_SIZE, val);
        Log.d(TAG, "ALPHA_SIZE: " + val[0]);
        egl.eglGetConfigAttrib(display, config, EGL10.EGL_DEPTH_SIZE, val);
        Log.d(TAG, "DEPTH_SIZE: " + val[0]);
        egl.eglGetConfigAttrib(display, config, EGL10.EGL_SAMPLE_BUFFERS, val);
        Log.d(TAG, "SAMPLE_BUFFERS: " + val[0]);
        egl.eglGetConfigAttrib(display, config, EGL10.EGL_SAMPLES, val);
        Log.d(TAG, "SAMPLES: " + val[0]);
    }
}
