package com.github.fabmax.lightgl.demo;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.github.fabmax.lightgl.BoundingBox;
import com.github.fabmax.lightgl.Camera;
import com.github.fabmax.lightgl.GfxEngine;
import com.github.fabmax.lightgl.GfxEngineListener;
import com.github.fabmax.lightgl.GfxState;
import com.github.fabmax.lightgl.GlException;
import com.github.fabmax.lightgl.Light;
import com.github.fabmax.lightgl.R;
import com.github.fabmax.lightgl.Ray;
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

    private GLSurfaceView mGlView;
    // main graphics engine object
    private GfxEngine mEngine;
    // the scene contains all objects that should be displayed
    private TransformGroup mScene;
    
    private BlockAnimator mBlocks;
    private long mStartTime = System.currentTimeMillis();
    
    // frame rate log output
    private long mLastFpsOut = 0;
    
    // touch response
    private int mTouchX;
    private int mTouchY;
    private boolean mTouchEvent = false;
    private Ray mTouchRay = new Ray();
    
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
        mGlView = (GLSurfaceView) findViewById(R.id.gl_view);
        // enable GLES 2.0
        mGlView.setEGLContextClientVersion(2);
        // register graphics engine as GL renderer
        mGlView.setRenderer(mEngine);
        
        // register a touch listener for some simple touch response
        mGlView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mTouchX = (int) event.getX();
                mTouchY = (int) event.getY();
                mTouchEvent = true;
                return true;
            }
        });
    }

    /**
     * Called before a frame is rendered.
     * 
     * @see GfxEngineListener#onRenderFrame(GfxEngine)
     */
    @Override
    public void onRenderFrame(GfxEngine engine) {
        GfxState state = engine.getState();
        long t = System.currentTimeMillis();
        float s = (t - mStartTime) / 1e3f;
        
        // rotate the camera
        float a = s * 10.0f;
        float x = (float) Math.sin(a / 100) * 12;
        float z = (float) Math.cos(a / 100) * 12;
        engine.getCamera().setPosition(x, 20, z);
        
        // slowly rotate the light
        Light light = engine.getLights().get(0);
        light.posX = (float) Math.cos(a / 50);
        light.posZ = (float) Math.sin(a / 50);
        
        // handle touch events
        if (mTouchEvent) {
            mTouchEvent = false;
            Camera cam = engine.getCamera();
            cam.getPickRay(state.getViewport(), mTouchX, mTouchY, mTouchRay);
            Block block = mBlocks.getHitBlock(mTouchRay);
            if (block != null) {
                block.animateToHeight(Block.MIN_HEIGHT, 250);
            }
        }

        // interpolate block heights
        mBlocks.interpolateHeights(state);
        
        // calculate frames per second and print them every second
        if(t > mLastFpsOut + 1000) {
            mLastFpsOut = t;
            Log.d("Activity", "Fps: " + engine.getFps());
        }
    }

    /**
     * Is called every time before the main-pass is rendered.
     * 
     * @param engine
     *            the graphics engine
     */
    @Override
    public void onRenderMainPass(GfxEngine engine) {
        // nothing special to do here
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
        int blocksX = 8;
        int blocksZ = 8;
        
        // use reduced render resolution
        ScaledScreenRenderPass pass = new ScaledScreenRenderPass(engine);
        pass.setViewportScale(0.75f);
        engine.setMainRenderPass(pass);
        
        // set camera position
        engine.getState().setBackgroundColor(0.067f, 0.235f, 0.298f);
        engine.getState().setBackgroundColor(0.8f, 0.8f, 0.8f);
        engine.getCamera().setPosition(0, 20, 12);
        
        // add a directional light
        Light light = new Light();
        light.colorR = 0.7f; light.colorG = 0.7f; light.colorB = 0.7f;
        light.posX = 0;      light.posY = 1;      light.posZ = 1;
        engine.addLight(light);
        
        // create scene
        mScene = new TransformGroup();
        //mScene.translate(0, 0, -3);
        engine.setScene(mScene);

        // enable shadow rendering
        BoundingBox bounds = new BoundingBox(-blocksX, blocksX, 0, 6, -blocksZ, blocksZ);
        ShadowRenderPass shadow = new ShadowRenderPass(engine);
        shadow.setSceneBounds(bounds);
        engine.setPreRenderPass(shadow);
        
        // add block mesh to scene
        mBlocks = new BlockAnimator(engine, shadow, blocksX, blocksZ);
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

    /**
     * Called on App pause.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mGlView.onPause();
    }

    /**
     * Called on App resume.
     */
    @Override
    protected void onResume() {
        super.onResume();
        mGlView.onResume();
    }
}
