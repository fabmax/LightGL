package de.fabmax.lightgl.physics;

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
public class PhysicsObject {

    private CollisionShape mPhysicsShape;
    private RigidBody mPhysicsBody;
    private Vector3f mTempV;
    private Transform mTransform;

    private PhysicsTransformGroup mGfxNode;
    private Mesh mGfxMesh;

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
        mGfxMesh = gfxMesh;
        mPhysicsShape = physicsShape;

        // create initial dynamics transform
        mTransform = new Transform();
        mTransform.setIdentity();

        // compute inertia (only for non static bodies)
        mTempV = new Vector3f(0, 0, 0);
        if (mass != 0) {
            mPhysicsShape.calculateLocalInertia(mass, mTempV);
        }

        // create rigid body for physics simulation
        DefaultMotionState motionState = new DefaultMotionState(mTransform);
        RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, motionState,
                mPhysicsShape, mTempV);
        mPhysicsBody = new RigidBody(rbInfo);
        mPhysicsBody.setFriction(1.0f);

        // create node for rendering
        mGfxNode = new PhysicsTransformGroup();
        mGfxNode.addChild(mGfxMesh);
    }

    public void setPosition(float x, float y, float z) {
        mPhysicsBody.getCenterOfMassTransform(mTransform);
        mTransform.origin.x = x;
        mTransform.origin.y = y;
        mTransform.origin.z = z;
        mPhysicsBody.setCenterOfMassTransform(mTransform);
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
     * Returns the {@link de.fabmax.lightgl.scene.Node} which is rendered by
     * {@link de.fabmax.lightgl.GfxEngine}.
     *
     * @return the {@link de.fabmax.lightgl.scene.Node} which is rendered by
     *         {@link de.fabmax.lightgl.GfxEngine}
     */
    public Node getGfxNode() {
        return mGfxNode;
    }

    /*
     * PhysicsTransformGroup takes the transform information from the physics object and applies it
     * to the rendered object.
     */
    private class PhysicsTransformGroup extends TransformGroup {
        @Override
        public void render(GfxState state) {
            // apply transformation from physics
            mPhysicsBody.getCenterOfMassTransform(mTransform);
            mTransform.getOpenGLMatrix(mTransformationM);

            // render body
            super.render(state);
        }
    }
}
