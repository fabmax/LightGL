package de.fabmax.lightgl.scene;

import de.fabmax.lightgl.GfxState;

/**
 * A scene node. This is the base class for all scene objects.
 * 
 * @author fabmax
 * 
 */
public abstract class Node {

    /**
     * Renders this node using the specified graphics engine state.
     * 
     * @param state
     *            the current graphics engine state
     */
    public abstract void render(GfxState state);

}
