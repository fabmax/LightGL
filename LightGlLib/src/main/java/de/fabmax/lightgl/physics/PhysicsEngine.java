package de.fabmax.lightgl.physics;

import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.AxisSweep3_32;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;

import java.util.ArrayList;

import javax.vecmath.Vector3f;

/**
 * PhysicsEngine is a wrapper class for the JBullet physics engine.
 *
 * @author fabmax
 */
public class PhysicsEngine {

    /** Gravity in m/s on earth */
    public static final float G = 9.81f;

    // fixed time step for physics simulation (60 steps per second)
    private static final float FIXED_TIME_STEP = 0.0167f;
    // maximum number of simulation steps per stepPhysics() call
    private static final int MAX_STEPS = 10;

    private DiscreteDynamicsWorld mWorld;

    private ArrayList<PhysicsObject> mObjects = new ArrayList<PhysicsObject>();

    private boolean mStopped = true;
    private long mLastTime;

    /**
     * Initializes the JBullet physics engine. {@link #stepPhysics()} has to be called in regular
     * intervals in order to update the simulated world. Moreover, {@link #start()} and
     * {@link #stop()} have to be called in order to resume or pause the physics simulation. By
     * default this is all handled by {@link de.fabmax.lightgl.GfxEngine} after enabling physics
     * simulation with {@link de.fabmax.lightgl.GfxEngine#setPhysicsEnabled(boolean)}.
     *
     * @see de.fabmax.lightgl.GfxEngine#setPhysicsEnabled(boolean)
     * @see de.fabmax.lightgl.GfxEngine#getPhysicsEngine()
     */
    public PhysicsEngine() {
        // init collision stuff
        CollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
        CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);

        // maximum size of the collision world.
        Vector3f worldAabbMin = new Vector3f(-100, -100, -100);
        Vector3f worldAabbMax = new Vector3f(100, 100, 100);
        BroadphaseInterface broadphase = new AxisSweep3(worldAabbMin, worldAabbMax);

        SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();

        mWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
        mWorld.setGravity(new Vector3f(0, -G, 0));
    }

    /**
     * Adds a {@link de.fabmax.lightgl.physics.PhysicsObject} to the physices engine.
     *
     * @param object    the object to add
     */
    public void addObject(PhysicsObject object) {
        mObjects.add(object);
        mWorld.addRigidBody(object.getPhysicsBody());
    }

    /**
     * Runs the physics simulation. The physics simulation runs in fixed timesteps of 1/60 seconds.
     * Hence calling this method can result in the computation of multiple physics steps. However,
     * the maximum number of steps is clamped to 10.
     */
    public void stepPhysics() {
        if (!mStopped) {
            long t = System.currentTimeMillis();
            float dt = (t - mLastTime) / 1000.0f;

            mWorld.applyGravity();
            mWorld.stepSimulation(dt, MAX_STEPS, FIXED_TIME_STEP);

            mLastTime = t;
        }
    }

    /**
     * Starts / resumes physics simulation.
     */
    public void start() {
        mLastTime = System.currentTimeMillis();
        mStopped = false;
    }

    /**
     * Pauses physics simulation.
     */
    public void stop() {
        mStopped = true;
    }

}
