package de.fabmax.lightgl.physics;

import android.util.Log;

import com.bulletphysics.BulletStats;
import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.AxisSweep3_32;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
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

    // time step for physics simulation in milliseconds (~60 steps per second)
    private static final int SIM_TIME_STEP_MS = 17;

    private PhysicsThread mPhysicsThread;
    private DiscreteDynamicsWorld mWorld;

    /**
     * Initializes the JBullet physics engine. {@link #onResume()}, {@link #onPause()} and
     * {@link #onDestroy()} have to be called from the corresponding life cycle methods. By default
     * this is all handled by {@link de.fabmax.lightgl.GfxEngine} after enabling physics simulation
     * with {@link de.fabmax.lightgl.GfxEngine#setPhysicsEnabled(boolean)}.
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

        mPhysicsThread = new PhysicsThread();
        mPhysicsThread.start();
    }

    /**
     * Adds a {@link de.fabmax.lightgl.physics.PhysicsObject} to the physices engine.
     *
     * @param object    the object to add
     */
    public void addObject(PhysicsObject object) {
        synchronized (mPhysicsThread) {
            mPhysicsThread.mAddObjects.add(object);
        }
    }

    /**
     * Starts / resumes physics simulation.
     */
    public void onResume() {
        mPhysicsThread.setPaused(false);
    }

    /**
     * Pauses physics simulation.
     */
    public void onPause() {
        mPhysicsThread.setPaused(false);
    }

    /**
     * Stops physics simulation and destroys the physics computation thread.
     */
    public void onDestroy() {
        mPhysicsThread.terminate();
    }

    /*
     * PhysicsThread runs the physics simulation. The physics simulation runs in fixed timesteps of
     * 1/60 seconds.
     */
    private class PhysicsThread extends Thread {
        private static final String TAG = "PhysicsThread";

        private ArrayList<PhysicsObject> mAddObjects = new ArrayList<PhysicsObject>();
        private ArrayList<PhysicsObject> mObjects = new ArrayList<PhysicsObject>();

        private boolean mPaused = true;
        private boolean mTerminate = false;
        private long mNextStep = 0;

        PhysicsThread() {
            setName(TAG);
        }

        synchronized void setPaused(boolean paused) {
            mPaused = paused;
            if (!paused) {
                notify();
            }
        }

        synchronized void terminate() {
            mTerminate = true;
            notify();
        }

        @Override
        public void run() {
            Log.d(TAG, "Thread started");

            while (!mTerminate) {
                synchronized (this) {
                    if (mPaused) {
                        Log.d(TAG, "Thread paused");
                        try {
                            // physics thread is paused wait for resume
                            wait();
                            // physics thread was resumed
                            mNextStep = System.currentTimeMillis();
                        } catch(InterruptedException e) {
                            // should not happen and doesn't matter anyway
                        }
                        Log.d(TAG, "Thread resumed");
                    }
                    if (mTerminate) {
                        break;
                    }
                }

                long t = System.currentTimeMillis();
                if (t < mNextStep) {
                    // sleep remaining time if computation is faster than real time
                    try {
                        Thread.sleep(mNextStep - t);
                    } catch(InterruptedException e) {
                        // should not happen and there's nothing we can do about it
                    }
                } else if (t > mNextStep + 100) {
                    // simulation is too slow!
                    mNextStep = t;
                }
                // set time for next simulation step
                mNextStep += SIM_TIME_STEP_MS;

                //long ns = System.nanoTime();

                // add new objects
                synchronized (this) {
                    for (PhysicsObject po : mAddObjects) {
                        mWorld.addRigidBody(po.getPhysicsBody());
                        mObjects.add(po);
                    }
                    mAddObjects.clear();
                }

                // simulate physics step
                mWorld.applyGravity();
                mWorld.stepSimulation(SIM_TIME_STEP_MS / 1000.0f, 0, SIM_TIME_STEP_MS / 1000.0f);

                // synchronize rendered objects with simulated objects
                for (PhysicsObject po : mObjects) {
                    po.postSimulateStep();
                }

                //ns = System.nanoTime() - ns;
                //Log.d(TAG, "comp time: " + ns / 1e6);
            }

            Log.d(TAG, "Thread terminated");
        }
    }
}
