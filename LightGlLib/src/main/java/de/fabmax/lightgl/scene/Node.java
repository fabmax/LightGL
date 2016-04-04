package de.fabmax.lightgl.scene;

import de.fabmax.lightgl.GfxState;
import de.fabmax.lightgl.LightGlContext;

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
     * @param context    the current graphics engine context
     */
    public abstract void render(LightGlContext context);

    /**
     * Frees all resources occupied by this Node.
     *
     * @param context    the current graphics engine context
     */
    public abstract void delete(LightGlContext context);

}
