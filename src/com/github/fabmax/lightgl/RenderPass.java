package com.github.fabmax.lightgl;

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
     * @param engine
     *            the graphics engine
     */
    public void doRenderPass(GfxEngine engine);

}
