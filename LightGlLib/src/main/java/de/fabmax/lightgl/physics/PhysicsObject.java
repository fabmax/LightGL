package de.fabmax.lightgl.physics;

import android.util.Log;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;

import javax.vecmath.Vector3f;

import de.fabmax.lightgl.GfxState;
import de.fabmax.lightgl.scene.Mesh;
import de.fabmax.lightgl.scene.Node;
import de.fabmax.lightgl.scene.TransformGroup;

/**
 * A PhysicsObject is a geometrical object which is simulated by
 * {@link de.fabmax.lightgl.physics.PhysicsEngine} and rendered by
 * {@link de.fabmax.lightgl.GfxEngine}. The simulated object and the rendered object are different
 * objects; however their states are synchronized such that all motions computed by the physics
 * engine are reflected by the rendered object. Since two different objects are used the object
 * simulated by the physics engine does not need to have the same shape as the rendered object. By
 * using a simplified shape for physics computations performance can be improved.
 *
 * @author fabmax
 */
public class PhysicsObject extends TransformGroup {

    protected RigidBody mPhysicsBody;
    protected final Transform mPhysicsTransform = new Transform();

    protected Mesh mGfxMesh;

    /**
     * Creates a new static physics object from the specified {@link de.fabmax.lightgl.scene.Mesh}
     * and {@link com.bulletphysics.collision.shapes.CollisionShape}. Static objects do not react
     * on gravity or applied forces but other bodies can collide with them.
     *
     * @param gfxMesh         {@link de.fabmax.lightgl.scene.Mesh} to render
     * @param physicsShape    Shape of the physics body
     */
    public PhysicsObject(Mesh gfxMesh, CollisionShape physicsShape) {
        this(gfxMesh, physicsShape, 0);
    }

    /**
     * Creates a new physics object from the specified {@link de.fabmax.lightgl.scene.Mesh} and
     * {@link com.bulletphysics.collision.shapes.CollisionShape} with the specified mass.
     *
     * @param gfxMesh         {@link de.fabmax.lightgl.scene.Mesh} to render
     * @param physicsShape    Shape of the physics body
     * @param mass            Mass of the physics body
     */
    public PhysicsObject(Mesh gfxMesh, CollisionShape physicsShape, float mass) {
        if (gfxMesh != null) {
            mGfxMesh = gfxMesh;
            addChild(mGfxMesh);
        }

        // create body for physics simulation
        setCollisionShape(physicsShape, mass);
    }

    /**
     * Default constructor is only available to sub-classes. Sub-class must call
     * {@link #setCollisionShape(com.bulletphysics.collision.shapes.CollisionShape, float)} in their
     * constructor to set the shape of the physics body. Moreover,
     * {@link #setMesh(de.fabmax.lightgl.scene.Mesh)} has to be called to set the mesh that is to
     * be rendered.
     *
     */
    protected PhysicsObject() {
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
     * Returns the mesh, which is rendered for this physics object.
     *
     * @return The mesh, which is rendered for this physics object
     */
    public Mesh getMesh() {
        return mGfxMesh;
    }

    /**
     * Replaces the mesh, which is rendered for this physics object.
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
            mPhysicsTransform.origin.set(x, y, z);
            mPhysicsBody.setCenterOfMassTransform(mPhysicsTransform);
        }
    }

    /**
     * Returns the center position of this body in world coordinates.
     * .
     * @param outPosition     3 component array used to store the position
     */
    public void getPosition(float[] outPosition) {
        synchronized (mPhysicsTransform) {
            mPhysicsTransform.origin.get(outPosition);
        }
    }

    /**
     * Returns the {@link com.bulletphysics.dynamics.RigidBody} which is simulated by
     * {@link de.fabmax.lightgl.physics.PhysicsEngine}.
     *
     * @return the {@link com.bulletphysics.dynamics.RigidBody} which is simulated by
     *             {@link de.fabmax.lightgl.physics.PhysicsEngine}
     */
    public RigidBody getPhysicsBody() {
        return mPhysicsBody;
    }

    /**
     * Called by the physics thread after every simulation step.
     */
    protected void postSimulateStep(float deltaT) {
        synchronized (mPhysicsTransform) {
            mPhysicsBody.getCenterOfMassTransform(mPhysicsTransform);
        }
    }

    /**
     * Applies the transformation computed by the physics simulation and renders this physics
     * object.
     */
    @Override
    public void render(GfxState state) {
        // apply transformation from physics
        synchronized (mPhysicsTransform) {
            mPhysicsTransform.getOpenGLMatrix(mTransformationM);
        }

        // render body
        super.render(state);
    }
}
