package de.fabmax.lightgl.scene;

import java.util.ArrayList;

import de.fabmax.lightgl.GfxState;

/**
 * A scene element that groups multiple child nodes.
 * 
 * @author fabmax
 *
 */
public class Group extends Node {

    private final ArrayList<Node> mChildren = new ArrayList<>();
    
    /**
     * Adds a child node to this group.
     * 
     * @param child
     *            the child to add to this group
     */
    public void addChild(Node child) {
        mChildren.add(child);
    }

    /**
     * Removes a child from this group.
     * 
     * @param child
     *            the child to remove from this group
     */
    public void removeChild(Node child) {
        mChildren.remove(child);
    }

    /**
     * Removes all children from this group.
     */
    public void removeAllChildren() {
        mChildren.clear();
    }

    /**
     * Returns a list with all children in this group.
     * 
     * @return the list with all children in this group
     */
    public ArrayList<Node> getChildren() {
        return mChildren;
    }

    /**
     * Renders this group by calling the render methods of all children.
     * 
     * @see Node#render(GfxState)
     */
    @Override
    public void render(GfxState state) {
        // we save an object allocation by using a classic for-loop instead of foreach
        for (int i=0; i < mChildren.size(); i++) {
            mChildren.get(i).render(state);
        }
    }

    /**
     * Calls delete() on all existing children and removes them from this group.
     *
     * @param state    the current graphics engine state
     */
    @Override
    public void delete(GfxState state) {
        // we save an object allocation by using a classic for-loop instead of foreach
        for (int i=0; i < mChildren.size(); i++) {
            mChildren.get(i).delete(state);
        }
        removeAllChildren();
    }

}
