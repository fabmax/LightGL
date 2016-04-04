package de.fabmax.lightgl;

/**
 * The Renderpass interface is used to implement custom render passes.
 * 
 * @author fabmax
 * 
 */
public interface RenderPass {

    /**
     * Is called from GfxEngine during frame rendering.
     *
     * @param context    the graphics engine context
     */
    public void onRender(LightGlContext context);

}
