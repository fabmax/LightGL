package com.github.fabmax.lightgl.demo;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;

import com.github.fabmax.lightgl.DepthShader;
import com.github.fabmax.lightgl.GfxEngine;
import com.github.fabmax.lightgl.GfxEngineListener;
import com.github.fabmax.lightgl.GlException;
import com.github.fabmax.lightgl.Light;
import com.github.fabmax.lightgl.R;
import com.github.fabmax.lightgl.ShadowPass;
import com.github.fabmax.lightgl.ShadowShader;
import com.github.fabmax.lightgl.Texture;
import com.github.fabmax.lightgl.TextureProperties;
import com.github.fabmax.lightgl.scene.Mesh;
import com.github.fabmax.lightgl.scene.TransformGroup;
import com.github.fabmax.lightgl.util.MeshFactory;
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
        // spin the scene wildly
        //mScene.rotate(0.5f, 0.9f, 0.5f, 0.0f);
        
        // spin the scene around the Y-axis
        mScene.rotate(1f, 0, 1, 0);
        
        // calculate frames per second and print them every second
        mFrames++;
        long t = System.currentTimeMillis();
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
        //setCubeScene(engine);
        setObjModelScene(engine);
    }
    
    /**
     * Loads a demo scene with a simple color cube.
     */
    public void setCubeScene(GfxEngine engine) {
        // set camera position
        engine.getState().setBackgroundColor(1, 0, 0);
        engine.getCamera().setPosition(0, 3, 5);
        
        // add a directional light
        Light light = new Light();
        light.colorR = 0.7f; light.colorG = 0.7f; light.colorB = 0.7f;
        light.posX = 1;      light.posY = 1;      light.posZ = 1;
        engine.addLight(light);
        
        // create scene
        mScene = new TransformGroup();
        mScene.rotate(180, 1, 0, 0);
        engine.setScene(mScene);
        
        //Mesh roundCube = MeshFactory.createRoundCube();
        //mScene.addChild(roundCube);
        //roundCube.setShader(new PhongShader(engine.getShaderManager()));
        //Texture tex = engine.getTextureManager().loadTexture(R.drawable.gray, new TextureProperties());
        //roundCube.setShader(new PhongShader(engine.getShaderManager(), tex));

        // enable shadow rendering
        ShadowPass shadow = new ShadowPass(engine);
        engine.setPreRenderPass(shadow);
        
        // add a color cube
        Mesh colorCube = MeshFactory.createColorCube();
        mScene.addChild(colorCube);
        //Texture tex = engine.getTextureManager().loadTexture(R.drawable.stone_wall, new TextureProperties());
        //colorCube.setShader(new PhongShader(engine.getShaderManager(), tex));
        //colorCube.setShader(new PhongShader(engine.getShaderManager(), shadow.getTexture()));
        colorCube.setShader(new DepthShader(engine.getShaderManager()));
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
        ShadowPass shadow = new ShadowPass(engine);
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
