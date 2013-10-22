package de.fabmax.lightgl.physics;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;

import javax.vecmath.Vector3f;

import de.fabmax.lightgl.GfxState;
import de.fabmax.lightgl.scene.Mesh;
import de.fabmax.lightgl.scene.TransformGroup;

/**
 * A PhysicsBody is a geometrical object which is simulated by
 * {@link de.fabmax.lightgl.physics.PhysicsEngine} and rendered by
 * {@link de.fabmax.lightgl.GfxEngine}. Internally, the simulated body and the rendered body are
 * different objects: A {@link de.fabmax.lightgl.scene.Mesh} and a
 * {@link com.bulletphysics.dynamics.RigidBody}. However, their states are synchronized such that all
 * motions computed by the physics engine are reflected by the rendered mesh. Since two different
 * objects are used, the body simulated by the physics engine does not need to have the same shape
 * as the rendered object. By using a simplified shape for physics computations performance can be
 * improved.
 *
 * @author fabmax
 */
public class PhysicsBody extends TransformGroup {

    protected RigidBody mPhysicsBody;
    protected final Transform mPhysicsTransform = new Transform();
    protected final Transform mBufferedTransform = new Transform();

    protected Mesh mGfxMesh;

    /**
     * Creates a new static body from the specified {@link de.fabmax.lightgl.scene.Mesh}
     * and {@link com.bulletphysics.collision.shapes.CollisionShape}. Static bodies do not react
     * on gravity or applied forces but other bodies can collide with them.
     *
     * @param gfxMesh         {@link de.fabmax.lightgl.scene.Mesh} used to render this body
     * @param physicsShape    Shape of the physics body
     */
    public PhysicsBody(Mesh gfxMesh, CollisionShape physicsShape) {
        this(gfxMesh, physicsShape, 0);
    }

    /**
     * Creates a new body from the specified {@link de.fabmax.lightgl.scene.Mesh} and
     * {@link com.bulletphysics.collision.shapes.CollisionShape} with the specified mass.
     *
     * @param gfxMesh         {@link de.fabmax.lightgl.scene.Mesh} used to render this body
     * @param physicsShape    Shape of the body
     * @param mass            Mass of the body
     */
    public PhysicsBody(Mesh gfxMesh, CollisionShape physicsShape, float mass) {
        if (gfxMesh != null) {
            mGfxMesh = gfxMesh;
            addChild(mGfxMesh);
        }
        // create body for physics simulation
        setCollisionShape(physicsShape, mass);
    }

    /**
     * Default constructor is only available to sub-classes. Sub-classes must call
     * {@link #setCollisionShape(com.bulletphysics.collision.shapes.CollisionShape, float)} in their
     * constructor to set the body shape. Moreover, {@link #setMesh(de.fabmax.lightgl.scene.Mesh)}
     * has to be called to set the mesh used to render this body.
     *
     */
    protected PhysicsBody() {
        // your ad here...
    }

    /**
     * Sets the shape and mass of the body simulated by the physics engine.
     *
     * @param physicsShape    Shape of the body
     * @param mass            Mass of the body
     */
    protected void setCollisionShape(CollisionShape physicsShape, float mass) {
        // create initial dynamics transform
        mPhysicsTransform.setIdentity();

        // compute inertia (only for non static bodies)
        Vector3f localInertia = new Vector3f(0, 0, 0);
        if (mass != 0) {
            physicsShape.calculateLocalInertia(mass, localInertia);
        }

        // create rigid body for physics simulation
        DefaultMotionState motionState = new DefaultMotionState(mPhysicsTransform);
        RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, motionState,
                physicsShape, localInertia);
        mPhysicsBody = new RigidBody(rbInfo);
        mPhysicsBody.setFriction(1.0f);
    }

    /**
     * Returns the mesh, which is rendered for this physics body.
     *
     * @return The mesh, which is rendered for this physics body
     */
    public Mesh getMesh() {
        return mGfxMesh;
    }

    /**
     * Replaces the mesh, which is used to rendered this body.
     *
     * @param mesh    New mesh to set
     */
    public void setMesh(Mesh mesh) {
        mGfxMesh = mesh;
        removeAllChildren();
        addChild(mGfxMesh);
    }

    /**
     * Sets the position of the body's center of mass in world coordinates.
     */
    public void setPosition(float x, float y, float z) {
        synchronized (mPhysicsTransform) {
            mBufferedTransform.origin.set(x, y, z);
            mPhysicsTransform.origin.set(x, y, z);
            mPhysicsBody.setCenterOfMassTransform(mPhysicsTransform);
        }
    }

    /**
     * Returns the 3 dimensional center position of this body in world coordinates. The specified
     * array must have at least offset + 3 elements.
     *
     * @param outPosition     array used to store the position
     * @param offset          array offset where the position is stored
     */
    public void getPosition(float[] outPosition, int offset) {
        outPosition[offset]     = mBufferedTransform.origin.x;
        outPosition[offset + 1] = mBufferedTransform.origin.y;
        outPosition[offset + 2] = mBufferedTransform.origin.z;
    }

    /**
     * Returns the {@link com.bulletphysics.dynamics.RigidBody} which is simulated by
     * {@link de.fabmax.lightgl.physics.PhysicsEngine}.
     *
     * @return the {@link com.bulletphysics.dynamics.RigidBody} which is simulated by
     *             {@link de.fabmax.lightgl.physics.PhysicsEngine}
     */
    protected RigidBody getPhysicsBody() {
        return mPhysicsBody;
    }

    /**
     * Called by the physics thread after every simulation step to synchronize the render body's
     * state to the current simulation state. Sub-classes can override this method in order to
     * implement additional simulation functions; however, classes overriding this method
     * must call super.postSimulateStep(deltaT).
     *
     * @param deltaT    Simulation time step in seconds
     */
    protected void postSimulateStep(float deltaT) {
        synchronized (mPhysicsTransform) {
            mPhysicsBody.getCenterOfMassTransform(mPhysicsTransform);
        }
    }

    /**
     * Called by the GL thread before a new frame is rendered. Caches the current state of the body
     * to maintain a constant body configuration throughout the frame rendering process.
     */
    public void synchronizeBodyConfig() {
        synchronized (mPhysicsTransform) {
            mBufferedTransform.set(mPhysicsTransform);
        }
    }

    /**
     * Applies the transformation computed by the physics simulation and renders this physics
     * object.
     */
    @Override
    public void render(GfxState state) {
        // apply current transformation from physics to parent transform group
        mBufferedTransform.getOpenGLMatrix(mTransformationM);

        // render body
        super.render(state);
    }
}