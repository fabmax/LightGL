package com.github.fabmax.lightgl.demo;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.github.fabmax.lightgl.GfxEngine;
import com.github.fabmax.lightgl.GfxEngineListener;
import com.github.fabmax.lightgl.Light;
import com.github.fabmax.lightgl.PhongShader;
import com.github.fabmax.lightgl.R;
import com.github.fabmax.lightgl.scene.Mesh;
import com.github.fabmax.lightgl.scene.TransformGroup;
import com.github.fabmax.lightgl.util.MeshFactory;

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
        mScene.rotate(0.5f, 0.9f, 0.5f, 0.0f);
    }

    /**
     * Called on startup after the GL context is created.
     * 
     * @see GfxEngineListener#onLoadScene(GfxEngine)
     */
    @Override
    public void onLoadScene(GfxEngine engine) {
        engine.getCamera().setPosition(0, 3, 5);
        
        Light light = new Light();
        light.colorR = 1;   light.colorG = 1;   light.colorB = 1;
        light.posX = 1;     light.posY = 1;     light.posZ = 1;
        engine.addLight(light);
        
        mScene = new TransformGroup();
        engine.setScene(mScene);
        
        Mesh colorCube = MeshFactory.createColorCube();
        colorCube.setShader(new PhongShader(engine.getShaderManager()));
        mScene.addChild(colorCube);
    }
}
