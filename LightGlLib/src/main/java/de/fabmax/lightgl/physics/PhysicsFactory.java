package de.fabmax.lightgl.physics;

import com.bulletphysics.collision.shapes.BoxShape;

import javax.vecmath.Vector3f;

import de.fabmax.lightgl.scene.Mesh;

/**
 * PhysicsFactory provides methods for easy construction of physics objects.
 *
 * @author fabmax
 */
public class PhysicsFactory {

    /**
     * Creates a physics enabled box with the specified {@link de.fabmax.lightgl.scene.Mesh},
     * size and mass.
     *
     * @param sizeX    width of the box
     * @param sizeY    height of the box
     * @param sizeZ    depth of the box
     * @param mass     mass of the box
     * @return a physics enabled box
     */
    public static PhysicsObject createBox(Mesh boxMesh, float sizeX, float sizeY, float sizeZ, float mass) {
        Vector3f size = new Vector3f(sizeX / 2.0f, sizeY / 2.0f, sizeZ / 2.0f);
        BoxShape box = new BoxShape(size);
        return new PhysicsObject(boxMesh, box, mass);
    }

}
