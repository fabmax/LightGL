package de.fabmax.lightgl.demo;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import de.fabmax.lightgl.GfxEngine;
import de.fabmax.lightgl.GfxEngineListener;
import de.fabmax.lightgl.GlException;
import de.fabmax.lightgl.Light;
import de.fabmax.lightgl.R;
import de.fabmax.lightgl.ShadowRenderPass;
import de.fabmax.lightgl.ShadowShader;
import de.fabmax.lightgl.Texture;
import de.fabmax.lightgl.scene.Mesh;
import de.fabmax.lightgl.scene.TransformGroup;
import de.fabmax.lightgl.util.BufferedTouchListener;
import de.fabmax.lightgl.util.GlConfiguration;
import de.fabmax.lightgl.util.ObjLoader;

/**
 * A demo Activity that shows a spinning color cube with Phong lighting.
 * 
 * @author fabmax
 *
 */
public class GlDemoActivity extends Activity implements GfxEngineListener {
    
    private static final String TAG = "GlDemoActivity";

    private GLSurfaceView mGlView;
    // main graphics engine object
    private GfxEngine mEngine;
    // the scene contains all objects that should be displayed
    private TransformGroup mScene;
    
    private long mStartTime = System.currentTimeMillis();
    
    // frame rate log output
    private long mLastFpsOut = 0;
    
    // touch response
    private BufferedTouchListener mTouchHandler = new BufferedTouchListener();
    
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
        GlConfiguration config = new GlConfiguration();
        config.setNumSamples(0);
        mGlView.setEGLConfigChooser(config);
        // register graphics engine as GL renderer
        mGlView.setRenderer(mEngine);
        
        // register a touch listener for some simple touch response
        mGlView.setOnTouchListener(mTouchHandler);
    }

    /**
     * Called before a frame is rendered.
     * 
     * @see GfxEngineListener#onRenderFrame(GfxEngine)
     */
    @Override
    public void onRenderFrame(GfxEngine engine) {
        long t = System.currentTimeMillis();
        float s = (t - mStartTime) / 1e3f;
        
        // rotate the camera
        float x = (float) Math.sin(s / 10) * 12;
        float z = (float) Math.cos(s / 10) * 12;
        engine.getCamera().setPosition(x, 20, z);
        
        // slowly rotate the light
        Light light = engine.getLights().get(0);
        light.position[0] = (float) Math.cos(s / 5);
        light.position[1] = 1.0f;
        light.position[2] = (float) Math.sin(s / 5);
        
        // calculate frames per second and print them every second
        if(t > mLastFpsOut + 1000) {
            mLastFpsOut = t;
            Log.d(TAG, "Fps: " + engine.getFps());
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
        setObjModelScene(engine);
    }
    
    @Override
    public void onViewportChange(int width, int height) {
        //onLoadScene(mEngine);
    }
    
    /**
     * Loads a demo scene with a loaded model.
     */
    public void setObjModelScene(GfxEngine engine) {
        // set camera position
        engine.getState().setBackgroundColor(0, 0, 0.2f);
        engine.getCamera().setPosition(0, 12, 18);
        
        // add a directional light
        Light light = Light.createDirectionalLight(1, 1, 1, 0.7f, 0.7f, 0.7f);
        engine.addLight(light);

        // enable shadow rendering
        ShadowRenderPass shadow = new ShadowRenderPass();
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
            Texture tex = engine.getTextureManager().createTextureFromResource(R.drawable.stone_wall);
            scene.setShader(ShadowShader.createGouraudShadowShader(engine.getShaderManager(), tex, shadow));
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
