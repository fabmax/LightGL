package de.fabmax.lightgl.demo;

import android.os.Bundle;
import android.util.Log;

import de.fabmax.lightgl.Light;
import de.fabmax.lightgl.LightGlActivity;
import de.fabmax.lightgl.LightGlContext;
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
public class PhysicsScene extends LightGlActivity {

    private static final String TAG = "PhysicsScene";

    private Shader mCubeMaterial;
    private Group mScene;

    private final BufferedTouchListener mTouchHandler = new BufferedTouchListener();
    private float mPhi = 0, mTheta = GlMath.PI / 2;

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
    public void onRenderFrame(LightGlContext glContext) {
        super.onRenderFrame(glContext);

        BufferedTouchListener.Pointer pt = mTouchHandler.getFirstPointer();
        if (pt.isValid()) {
            // rotate camera according to touch
            mPhi -= pt.getDX() / 200;
            mTheta -= pt.getDY() / 200;
            mTheta = GlMath.clamp(mTheta, 0.1f, GlMath.PI / 2.0f);

            if (!pt.isDown() && Math.abs(pt.getOverallDX()) < 10 && Math.abs(pt.getOverallDY()) < 10) {
                // spawn a new cube on tap
                spawnCube(glContext);
            }
            pt.recycle();
        }

        // update camera position
        float x = 20 * (float) Math.sin(mTheta) * (float) Math.sin(mPhi);
        float z = 20 * (float) Math.sin(mTheta) * (float) Math.cos(mPhi);
        float y = 20 * (float) Math.cos(mTheta);
        glContext.getEngine().getCamera().setPosition(x, y, z);
    }

    private void spawnCube(LightGlContext glContext) {
        // add a color cube
        Mesh boxMesh = MeshFactory.createStaticMesh(MeshFactory.createColorCube(1, 1, 1, null));
        //Mesh boxMesh = MeshFactory.createCylinder(0.5f, 1, 20);
        boxMesh.setShader(mCubeMaterial);
        PhysicsBody cube = PhysicsFactory.createBox(boxMesh, 1, 1, 1, 1);
        cube.setPosition((float)Math.random(), 10, (float)Math.random());
        mScene.addChild(cube);
        glContext.getEngine().getPhysicsEngine().addObject(cube);

        Log.d(TAG, "Cube count: " + ++mCubeCount);
    }

    /**
     * Called on startup after the GL context is created.
     */
    @Override
    public void onLoadScene(LightGlContext glContext) {
        glContext.getState().setBackgroundColor(0, 0, 0.2f);
        glContext.getEngine().getPhysicsEngine().initSimulation(true);

        // add a directional light
        Light light = Light.createDirectionalLight(1, 1, 1, 0.7f, 0.7f, 0.7f);
        glContext.getEngine().addLight(light);

        mCubeMaterial = SimpleShader.createPhongColorShader(glContext.getShaderManager());
        mScene = new Group();
        glContext.getEngine().setScene(mScene);

        // add a color cube as floor
        float floorX = 100;
        float floorY = 1;
        float floorZ = 100;
        Texture tex = glContext.getTextureManager().createTextureFromAsset("textures/gray.png");
        Mesh boxMesh = MeshFactory.createStaticMesh(MeshFactory.createColorCube(floorX, floorY, floorZ, null));
        boxMesh.setShader(SimpleShader.createPhongTextureShader(glContext.getShaderManager(), tex));
        PhysicsBody floor = PhysicsFactory.createBox(boxMesh, floorX, floorY, floorZ, 0);
        floor.setPosition(0, -5, 0);
        mScene.addChild(floor);
        glContext.getEngine().getPhysicsEngine().addObject(floor);
    }
}
