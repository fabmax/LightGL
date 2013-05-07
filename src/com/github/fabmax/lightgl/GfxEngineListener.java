package com.github.fabmax.lightgl;

/**
 * Graphics engine interface.
 * 
 * @author fabmax
 *
 */
public interface GfxEngineListener {

    /**
     * Is called on Engine initialization. When this is called the GL context is created and it is
     * safe to call call kinds of GL functions, load shaders, textures, etc.
     * 
     * @param engine
     *            the graphics engine
     */
    public void onLoadScene(GfxEngine engine);
    
    /**
     * Is called every time before a new frame is rendered.
     * 
     * @param engine
     *            the graphics engine
     */
    public void onRenderFrame(GfxEngine engine);

}
