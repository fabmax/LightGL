package de.fabmax.lightgl.physics;

import android.util.Log;

import com.bulletphysics.collision.broadphase.AxisSweep3;
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

    /** Gravity in m/s^2 on earth */
    public static final float G = 9.81f;

    // time step for physics simulation in seconds (~60 steps per second)
    private static final float SIM_TIME_STEP = 0.0166f;

    private final PhysicsThread mPhysicsThread;
    private final DiscreteDynamicsWorld mWorld;

    private float mSimulationTime = 0;

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
     * Returns the passed simulation time in seconds.
     *
     * @return the passed simulation time in seconds.
     */
    public float getSimulationTime() {
        return mSimulationTime;
    }

    /**
     * Resets the passed simulation time to 0.
     */
    public void resetSimulationTime() {
        mSimulationTime = 0;
    }

    /**
     * Adds a {@link PhysicsBody} to the physices simulation.
     *
     * @param object    the object to add
     */
    public void addObject(PhysicsBody object) {
        synchronized (mPhysicsThread) {
            mPhysicsThread.mAddObjects.add(object);
        }
    }

    /**
     * Removes a {@link PhysicsBody} from the physics simulation.
     *
     * @param object    the object to remove
     */
    public void removeObject(PhysicsBody object) {
        synchronized (mPhysicsThread) {
            mPhysicsThread.mRemoveObjects.add(object);
        }
    }

    /**
     * Called by the GL thread in order to synchronize the simulated configurations of all bodies to
     * their rendered meshes.
     */
    public void synchronizeBodyConfigurations() {
        synchronized (mPhysicsThread) {
            for (int i = 0; i < mPhysicsThread.mObjects.size(); i++) {
                mPhysicsThread.mObjects.get(i).synchronizeBodyConfig();
            }
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
        mPhysicsThread.setPaused(true);
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

        private ArrayList<PhysicsBody> mAddObjects = new ArrayList<PhysicsBody>();
        private ArrayList<PhysicsBody> mRemoveObjects = new ArrayList<PhysicsBody>();
        private ArrayList<PhysicsBody> mObjects = new ArrayList<PhysicsBody>();

        private volatile boolean mPaused = true;
        private boolean mTerminate = false;
        private long mStartTime = 0;
        private int mNextStep = 0;
        private float mRunTime = 0;

        PhysicsThread() {
            setName(TAG);
        }

        synchronized void setPaused(boolean paused) {
            mPaused = paused;
            if (!paused) {
                mStartTime = System.currentTimeMillis();
                mRunTime = 0;
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
                if (mPaused) {
                    synchronized (this) {
                        Log.d(TAG, "Thread paused");
                        try {
                            // physics thread is paused wait for resume
                            wait();
                            if (mTerminate) {
                                // thread was resumed to terminate
                                break;
                            }
                            // physics thread was resumed
                            mNextStep = 0;
                        } catch(InterruptedException e) {
                            // should not happen and doesn't matter anyway
                        }
                        Log.d(TAG, "Thread resumed");
                    }
                }

                int t = (int) (System.currentTimeMillis() - mStartTime);
                if (t < mNextStep) {
                    // sleep remaining time if computation is faster than real time
                    try {
                        Thread.sleep(mNextStep - t);
                    } catch(InterruptedException e) {
                        // should not happen and there's nothing we can do about it
                    }
                } else if (t > mNextStep + 100) {
                    // simulation time is more than 100ms behind real time, skip time
                    mNextStep = t;
                    mRunTime = t / 1000.0f;
                }
                // set time for next simulation step
                mRunTime += SIM_TIME_STEP;
                mNextStep = (int) (mRunTime * 1000.0f);

                //long ns = System.nanoTime();

                if (mAddObjects.size() > 0 || mRemoveObjects.size() > 0) {
                    synchronized (this) {
                        // add new objects
                        for (int i = 0; i < mAddObjects.size(); i++) {
                            PhysicsBody body = mAddObjects.get(i);
                            mWorld.addRigidBody(body.getPhysicsBody());
                            mObjects.add(body);
                        }
                        mAddObjects.clear();
                        // remove deleted objects
                        for (int i = 0; i < mRemoveObjects.size(); i++) {
                            PhysicsBody body = mRemoveObjects.get(i);
                            mWorld.removeRigidBody(body.getPhysicsBody());
                            mObjects.remove(body);
                        }
                        mRemoveObjects.clear();
                    }
                }

                // simulate physics step
                mWorld.stepSimulation(SIM_TIME_STEP, 0, SIM_TIME_STEP);
                mSimulationTime += SIM_TIME_STEP;

                // synchronize rendered objects with simulated objects
                for (int i = 0; i < mObjects.size(); i++) {
                    mObjects.get(i).postSimulateStep(SIM_TIME_STEP);
                }

                //ns = System.nanoTime() - ns;
                //Log.d(TAG, "comp time: " + ns / 1e6);
            }

            Log.d(TAG, "Thread terminated");
        }
    }
}
