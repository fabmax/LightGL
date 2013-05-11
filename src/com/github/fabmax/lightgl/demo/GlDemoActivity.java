package com.github.fabmax.lightgl.demo;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.github.fabmax.lightgl.GfxEngine;
import com.github.fabmax.lightgl.GfxEngineListener;
import com.github.fabmax.lightgl.GlException;
import com.github.fabmax.lightgl.Light;
import com.github.fabmax.lightgl.PhongShader;
import com.github.fabmax.lightgl.R;
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
        
        // spin the scene arounf the Y-axis
        mScene.rotate(0.5f, 0, 1, 0);
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
        
        // add a color cube
        Mesh colorCube = MeshFactory.createColorCube();
        mScene.addChild(colorCube);
        Texture tex = engine.getTextureManager().loadTexture(R.drawable.stone_wall, new TextureProperties());
        colorCube.setShader(new PhongShader(engine.getShaderManager(), tex));
    }
    
    /**
     * Loads a demo scene with a loaded model.
     */
    public void setObjModelScene(GfxEngine engine) {
        // set camera position
        engine.getCamera().setPosition(0, 10, 15);
        
        // add a directional light
        Light light = new Light();
        light.colorR = 0.7f; light.colorG = 0.7f; light.colorB = 0.7f;
        light.posX = 1;      light.posY = 1;      light.posZ = 1;
        engine.addLight(light);
        
        try {
            // create scene
            mScene = new TransformGroup();
            engine.setScene(mScene);
            
            // load model and add it to the scene
            Mesh room = ObjLoader.loadObj(this, "models/room_thickwalls.obj");
            mScene.addChild(room);
            // set model material
            Texture tex = engine.getTextureManager().loadTexture(R.drawable.gray, new TextureProperties());
            room.setShader(new PhongShader(engine.getShaderManager(), tex));
        } catch (GlException e) {
            e.printStackTrace();
        }
    }
}
