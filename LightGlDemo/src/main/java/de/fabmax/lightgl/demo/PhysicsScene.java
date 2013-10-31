package de.fabmax.lightgl.demo;

import android.os.Bundle;
import android.util.Log;

import de.fabmax.lightgl.GfxEngine;
import de.fabmax.lightgl.Light;
import de.fabmax.lightgl.LigthtGlActivity;
import de.fabmax.lightgl.Shader;
import de.fabmax.lightgl.SimpleShader;
import de.fabmax.lightgl.Texture;
import de.fabmax.lightgl.physics.PhysicsBody;
import de.fabmax.lightgl.physics.PhysicsFactory;
import de.fabmax.lightgl.scene.Group;
import de.fabmax.lightgl.scene.Mesh;
import de.fabmax.lightgl.util.BufferedTouchListener;
import de.fabmax.lightgl.util.GlMath;
import de.fabmax.lightgl.util.MeshFactory;

/**
 * A demo scene with physics simulation.
 *
 * @author fabmax
 *
 */
public class PhysicsScene extends LigthtGlActivity {

    private static final String TAG = "PhysicsScene";

    private Shader mCubeMaterial;
    private Group mScene;

    private BufferedTouchListener mTouchHandler = new BufferedTouchListener();
    private float mRadius = 20, mPhi = 0, mTheta = GlMath.PI / 2;

    private int mCubeCount = 0;

    /**
     * Called on Activity startup.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initializes the GLSurfaceView and the GfxEngine inside parent class LightGlActivity
        setNumSamples(4);
        // enable physics simulation
        createEngine(true);

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

        BufferedTouchListener.Pointer pt = mTouchHandler.getFirstPointer();
        if (pt.isValid()) {
            // rotate camera according to touch
            mPhi -= pt.getDX() / 200;
            mTheta -= pt.getDY() / 200;
            mTheta = GlMath.clamp(mTheta, 0.1f, GlMath.PI / 2.0f);

            if (!pt.isDown() && Math.abs(pt.getOverallDX()) < 10 && Math.abs(pt.getOverallDY()) < 10) {
                // spawn a new cube on tap
                spawnCube(engine);
            }
            pt.recycle();
        }

        // update camera position
        float x = mRadius * (float) Math.sin(mTheta) * (float) Math.sin(mPhi);
        float z = mRadius * (float) Math.sin(mTheta) * (float) Math.cos(mPhi);
        float y = mRadius * (float) Math.cos(mTheta);
        engine.getCamera().setPosition(x, y, z);
    }

    private void spawnCube(GfxEngine engine) {
        // add a color cube
        Mesh boxMesh = MeshFactory.createStaticMesh(MeshFactory.createColorCube(1, 1, 1, null));
        //Mesh boxMesh = MeshFactory.createCylinder(0.5f, 1, 20);
        boxMesh.setShader(mCubeMaterial);
        PhysicsBody cube = PhysicsFactory.createBox(boxMesh, 1, 1, 1, 1);
        cube.setPosition((float)Math.random(), 10, (float)Math.random());
        mScene.addChild(cube);
        engine.getPhysicsEngine().addObject(cube);

        Log.d(TAG, "Cube count: " + ++mCubeCount);
    }

    /**
     * Called on startup after the GL context is created.
     */
    @Override
    public void onLoadScene(GfxEngine engine) {
        engine.getState().setBackgroundColor(0, 0, 0.2f);
        engine.getPhysicsEngine().initSimulation(true);

        // add a directional light
        Light light = Light.createDirectionalLight(1, 1, 1, 0.7f, 0.7f, 0.7f);
        engine.addLight(light);

        mCubeMaterial = SimpleShader.createPhongColorShader(engine.getShaderManager());
        mScene = new Group();
        engine.setScene(mScene);

        // add a color cube as floor
        float floorX = 100;
        float floorY = 1;
        float floorZ = 100;
        Texture tex = engine.getTextureManager().createTextureFromAsset("textures/gray.png");
        Mesh boxMesh = MeshFactory.createStaticMesh(MeshFactory.createColorCube(floorX, floorY, floorZ, null));
        boxMesh.setShader(SimpleShader.createPhongTextureShader(engine.getShaderManager(), tex));
        PhysicsBody floor = PhysicsFactory.createBox(boxMesh, floorX, floorY, floorZ, 0);
        floor.setPosition(0, -5, 0);
        mScene.addChild(floor);
        engine.getPhysicsEngine().addObject(floor);
    }
}
