package com.github.fabmax.lightgl.demo;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;

import com.github.fabmax.lightgl.GfxEngine;
import com.github.fabmax.lightgl.GfxEngineListener;
import com.github.fabmax.lightgl.GlException;
import com.github.fabmax.lightgl.Light;
import com.github.fabmax.lightgl.R;
import com.github.fabmax.lightgl.ScaledScreenRenderPass;
import com.github.fabmax.lightgl.ShadowRenderPass;
import com.github.fabmax.lightgl.ShadowShader;
import com.github.fabmax.lightgl.Texture;
import com.github.fabmax.lightgl.TextureProperties;
import com.github.fabmax.lightgl.scene.Mesh;
import com.github.fabmax.lightgl.scene.TransformGroup;
import com.github.fabmax.lightgl.util.ObjLoader;

/**
 * A demo Activity that shows a spinning color cube with Phong lighting.
 * 
 * @author fabmax
 *
 */
public class GlDemoActivity extends Activity implements GfxEngineListener {

    // main graphics engine object
    private GfxEngine mEngine;
    
    // the scene contains all objects that should be displayed
    private TransformGroup mScene;
    
    // frame rate calculation
    private long mLastFpsOut = 0;
    private int mFrames = 0;
    private float mFps = 0;
    
    private BlockAnimator mBlocks;
    private long mStartTime = System.currentTimeMillis();
    
    /**
     * Called on App startup.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize graphics engine
        mEngine = new GfxEngine(getApplicationContext());
        mEngine.setEngineListener(this);

        // set layout
        setContentView(R.layout.activity_gl_demo);
        GLSurfaceView glView = (GLSurfaceView) findViewById(R.id.gl_view);
        // enable GLES 2.0
        glView.setEGLContextClientVersion(2);
        // register graphics engine as GL renderer
        glView.setRenderer(mEngine);
    }

    /**
     * Called before every time before a frame is rendered.
     * 
     * @see GfxEngineListener#onRenderFrame(GfxEngine)
     */
    @Override
    public void onRenderFrame(GfxEngine engine) {
        long t = System.currentTimeMillis();
        float s = (t - mStartTime) / 1e3f;
        
        // spin the scene wildly
        //mScene.rotate(0.5f, 0.9f, 0.5f, 0.0f);
        
        // spin the scene around the Y-axis
        float a = s * 10.0f;
        mScene.resetTransform();
        mScene.rotate(-a, 0, 1, 0);
        
        // slowly rotate camera
        float x = (float) Math.sin(a / 200) * 12;
        float z = (float) Math.cos(a / 200) * 12;
        engine.getCamera().setPosition(x, 20, z);

        // interpolate block heights
        mBlocks.interpolateHeights(engine.getState());
        
        // calculate frames per second and print them every second
        mFrames++;
        if(t > mLastFpsOut + 1000) {
            mFps = mFrames / ((t - mLastFpsOut) / 1000.0f);
            mLastFpsOut = t;
            mFrames = 0;
            Log.d("Activity", "Fps: " + mFps);
        }
    }

    /**
     * Called on startup after the GL context is created.
     * 
     * @see GfxEngineListener#onLoadScene(GfxEngine)
     */
    @Override
    public void onLoadScene(GfxEngine engine) {
        setCubeScene(engine);
        //setObjModelScene(engine);
    }
    
    /**
     * Loads a demo scene with a simple color cube.
     */
    public void setCubeScene(GfxEngine engine) {
        // use reduced render resolution
        ScaledScreenRenderPass pass = new ScaledScreenRenderPass(engine);
        pass.setViewportScale(0.5f);
        engine.setMainRenderPass(pass);
        
        // set camera position
        engine.getState().setBackgroundColor(0.067f, 0.235f, 0.298f);
        engine.getState().setBackgroundColor(0.8f, 0.8f, 0.8f);
        engine.getCamera().setPosition(0, 20, 12);
        
        // add a directional light
        Light light = new Light();
        light.colorR = 0.7f; light.colorG = 0.7f; light.colorB = 0.7f;
        light.posX = 0.8f;      light.posY = 1;      light.posZ = 1;
        engine.addLight(light);
        
        // create scene
        mScene = new TransformGroup();
        //mScene.translate(0, 0, -3);
        engine.setScene(mScene);

        // enable shadow rendering
        ShadowRenderPass shadow = new ShadowRenderPass(engine);
        engine.setPreRenderPass(shadow);
        
        // add block mesh to scene
        mBlocks = new BlockAnimator(engine, shadow, 16, 16);
        mScene.addChild(mBlocks.getMesh());
    }
    
    /**
     * Loads a demo scene with a loaded model.
     */
    public void setObjModelScene(GfxEngine engine) {
        // set camera position
        engine.getState().setBackgroundColor(0, 0, 0.2f);
        engine.getCamera().setPosition(0, 12, 18);
        
        // add a directional light
        Light light = new Light();
        light.colorR = 0.7f; light.colorG = 0.7f; light.colorB = 0.7f;
        light.posX = 1.0f;   light.posY = 1.0f;   light.posZ = 1.0f;
        engine.addLight(light);

        // enable shadow rendering
        ShadowRenderPass shadow = new ShadowRenderPass(engine);
        engine.setPreRenderPass(shadow);
        
        try {
            // create scene
            mScene = new TransformGroup();
            //mScene.rotate(30, 1, 0, 0);
            engine.setScene(mScene);
            
            // load model and add it to the scene
            Mesh scene = ObjLoader.loadObj(this, "models/room_thickwalls.obj");
            mScene.addChild(scene);
            // set model material
            //Texture tex = engine.getTextureManager().loadTexture(R.drawable.gray, new TextureProperties());
            Texture tex = engine.getTextureManager().loadTexture(R.drawable.stone_wall, new TextureProperties());
            scene.setShader(new ShadowShader(engine.getShaderManager(), tex, shadow));
        } catch (GlException e) {
            e.printStackTrace();
        }
    }
}
