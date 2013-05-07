package com.github.fabmax.lightgl.scene;

import java.util.ArrayList;

import com.github.fabmax.lightgl.GfxState;

/**
 * A scene element that groups multiple child nodes.
 * 
 * @author fabmax
 *
 */
public class Group extends Node {

    private ArrayList<Node> mChildren;

    /**
     * Standard constructor that creates a new Group. Nothing special here.
     */
    public Group() {
        mChildren = new ArrayList<Node>();
    }
    
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
        for (Node child : mChildren) {
            child.render(state);
        }
    }

}
