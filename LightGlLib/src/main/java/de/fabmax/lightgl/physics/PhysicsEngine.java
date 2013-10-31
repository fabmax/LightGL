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
    private static final float SIM_TIME_STEP = 0.0167f;

    private final DiscreteDynamicsWorld mWorld;

    private ArrayList<PhysicsBody> mAddObjects = new ArrayList<PhysicsBody>();
    private ArrayList<PhysicsBody> mRemoveObjects = new ArrayList<PhysicsBody>();
    private ArrayList<PhysicsBody> mObjects = new ArrayList<PhysicsBody>();

    private PhysicsThread mPhysicsThread;
    private PhysicsListener mPhysicsListener;

    private long mSimulationStartTime = 0;
    private float mSimulationTime = 0;
    private float mSimulationMissTime = 0;
    private boolean mActive = false;

    /**
     * Initializes the JBullet physics engine. {@link #onResume()}, {@link #onPause()} and
     * {@link #onDestroy()} have to be called from the corresponding life cycle methods. By default
     * this is all handled by {@link de.fabmax.lightgl.GfxEngine} if physics simulation was enabled
     * on engine creation.
     *
     * @see de.fabmax.lightgl.GfxEngine#GfxEngine(android.content.Context, boolean)
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
     * Initializes the physics simulation. If threaded is true a separate thread is started for
     * physics simulation and the simulation runs asynchronously. If threaded is false
     * {@link #simulateBlocking()} has to be called in regular intervals (e.g. from the render loop)
     * in order to update the simulation.
     *
     * @param threaded    true to use a separate thread for simulation
     */
    public synchronized void initSimulation(boolean threaded) {
        mSimulationStartTime = System.currentTimeMillis();

        if (threaded) {
            mPhysicsThread = new PhysicsThread();
            if (mActive) {
                mPhysicsThread.setPaused(false);
            }
            mPhysicsThread.start();
        }
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
        mSimulationMissTime = 0;
        mSimulationStartTime = System.currentTimeMillis();
    }

    /**
     * Adds a {@link PhysicsBody} to the physices simulation.
     *
     * @param object    the object to add
     */
    public void addObject(PhysicsBody object) {
        synchronized (mWorld) {
            mAddObjects.add(object);
        }
    }

    /**
     * Removes a {@link PhysicsBody} from the physics simulation.
     *
     * @param object    the object to remove
     */
    public void removeObject(PhysicsBody object) {
        synchronized (mWorld) {
            mRemoveObjects.add(object);
        }
    }

    /**
     * Runs the physics simulation. This method simulates at most 0.1s simulation time. If the last
     * call of this method is longer ago, simulation time is skipped.
     */
    public void simulateBlocking() {
        if (mPhysicsThread != null) {
            throw new IllegalStateException("Do not call this method, while simulation runs asynchronously");
        }

        float simT = (System.currentTimeMillis() - mSimulationStartTime) / 1000.0f;
        float dt = simT - mSimulationTime - mSimulationMissTime;
        if (dt > 0.1f) {
            // time step is to large, we have to skip time
            mSimulationMissTime = dt - 0.1f;
            dt = 0.1f;
        }
        float targetTime = mSimulationTime + dt;
        while (Math.abs(mSimulationTime - targetTime) > 0.01f) {
            simulateSingleStep();
        }
    }

    /**
     * Performs a single simulation step.
     */
    private void simulateSingleStep() {
        if (mPhysicsListener != null) {
            mPhysicsListener.preSimulateStep(SIM_TIME_STEP);
        }

        if (mAddObjects.size() > 0 || mRemoveObjects.size() > 0) {
            synchronized (mWorld) {
                // add new objects
                for (int i = 0; i < mAddObjects.size(); i++) {
                    PhysicsBody body = mAddObjects.get(i);
                    body.buildCollisionShape();
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
        if (mPhysicsListener != null) {
            mPhysicsListener.postSimulateStep(SIM_TIME_STEP);
        }
    }

    /**
     * Called by the GL thread in order to synchronize the simulated configurations of all bodies to
     * their rendered meshes.
     */
    public void synchronizeBodyConfigurations() {
        synchronized (mWorld) {
            for (int i = 0; i < mObjects.size(); i++) {
                mObjects.get(i).synchronizeBodyConfig();
            }
        }
    }

    /**
     * Starts / resumes physics simulation.
     */
    public synchronized void onResume() {
        mActive = true;
        if (mPhysicsThread != null) {
            mPhysicsThread.setPaused(false);
        }
    }

    /**
     * Pauses physics simulation.
     */
    public synchronized void onPause() {
        mActive = false;
        if (mPhysicsThread != null) {
            mPhysicsThread.setPaused(true);
        }
    }

    /**
     * Stops physics simulation and destroys the physics computation thread.
     */
    public synchronized void onDestroy() {
        if (mPhysicsThread != null) {
            mPhysicsThread.terminate();
        }
    }

    /**
     * Sets a {@link de.fabmax.lightgl.physics.PhysicsEngine.PhysicsListener}, which is called
     * by the physics thread after every simulation step.
     */
    public void setPhysicsListener(PhysicsListener physicsListener) {
        mPhysicsListener = physicsListener;
    }

    /**
     * The PhysicsListener is called by the physics thread on every simulation step.
     */
    public interface PhysicsListener {

        /**
         * Called by the physics thread before every simulation step.
         *
         * @param deltaT    simulation time step
         */
        public void preSimulateStep(float deltaT);

        /**
         * Called by the physics thread after every simulation step.
         *
         * @param deltaT    simulation time step
         */
        public void postSimulateStep(float deltaT);
    }

    /*
     * PhysicsThread asynchronously runs the physics simulation.
     */
    private class PhysicsThread extends Thread {
        private static final String TAG = "PhysicsThread";

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

                simulateSingleStep();

                //ns = System.nanoTime() - ns;
                //Log.d(TAG, "comp time: " + ns / 1e6);
            }

            Log.d(TAG, "Thread terminated");
        }
    }
}
