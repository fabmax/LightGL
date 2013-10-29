package de.fabmax.lightgl.demo;

import android.os.Bundle;

import de.fabmax.lightgl.Camera;
import de.fabmax.lightgl.GfxEngine;
import de.fabmax.lightgl.Light;
import de.fabmax.lightgl.LightGlException;
import de.fabmax.lightgl.LigthtGlActivity;
import de.fabmax.lightgl.ShadowRenderPass;
import de.fabmax.lightgl.ShadowShader;
import de.fabmax.lightgl.Texture;
import de.fabmax.lightgl.scene.Mesh;
import de.fabmax.lightgl.scene.TransformGroup;
import de.fabmax.lightgl.util.BufferedTouchListener;
import de.fabmax.lightgl.util.ObjLoader;

/**
 * A simple demo scene with a loaded obj model touch interaction, texture mapping and dynamic
 * shadows.
 * 
 * @author fabmax
 *
 */
public class SimpleScene extends LigthtGlActivity {

    private static final String STATE_ROT_X = "state_rot_x";
    private static final String STATE_ROT_Y = "state_rot_y";

    // the scene contains all objects that are rendered
    private TransformGroup mScene;
    private float mRotationX = 180;
    private float mRotationY = 0;

    private BufferedTouchListener mTouchHandler = new BufferedTouchListener();
    
    /**
     * Called on Activity startup.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initializes the GLSurfaceView and the GfxEngine inside parent class LightGlActivity
        createEngine();

        if (savedInstanceState != null) {
            mRotationX = savedInstanceState.getFloat(STATE_ROT_X);
            mRotationY = savedInstanceState.getFloat(STATE_ROT_Y);
        }

        // register a touch listener for touch input
        mGlView.setOnTouchListener(mTouchHandler);
        // enable FPS log output
        setLogFramesPerSecond(true);
    }

    /**
     * Called before a frame is rendered.
     */
    @Override
    public void onRenderFrame(GfxEngine engine) {
        super.onRenderFrame(engine);

        // rotate the camera
        BufferedTouchListener.Pointer pt = mTouchHandler.getPointers()[0];
        if (pt.isValid()) {
            // some simple touch response
            mRotationX += pt.getDX() / 10.0f;
            mRotationY -= pt.getDY() / 10.0f;
            pt.recycle();
        }
        mScene.resetTransform();
        mScene.rotate(mRotationX, 0, 1, 0);
        mScene.rotate(mRotationY, 1, 0, 0);
    }

    /**
     * Called on startup after the GL context is created.
     */
    @Override
    public void onLoadScene(GfxEngine engine) {
        engine.getState().setBackgroundColor(0, 0, 0.2f);
        Camera cam = engine.getCamera();
        cam.setPosition(0, 120, 180);
        cam.animatePositionTo(0, 12, 18);
        
        // add a directional light
        Light light = Light.createDirectionalLight(1, 1, 1, 0.7f, 0.7f, 0.7f);
        engine.addLight(light);

        // enable shadow rendering
        ShadowRenderPass shadow = new ShadowRenderPass();
        engine.setPreRenderPass(shadow);
        
        try {
            // create scene
            mScene = new TransformGroup();
            engine.setScene(mScene);
            
            // load model and add it to the scene
            Mesh scene = ObjLoader.loadObj(this, "models/room_thickwalls.obj");
            mScene.addChild(scene);
            
            // set model material
            Texture tex = engine.getTextureManager().createTextureFromAsset("textures/stone_wall.png");
            scene.setShader(ShadowShader.createPhongShadowShader(engine.getShaderManager(), tex, shadow));
        } catch (LightGlException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the rotation state of the scene on runtime configuration change.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat(STATE_ROT_X, mRotationX);
        outState.putFloat(STATE_ROT_Y, mRotationY);
    }
}
