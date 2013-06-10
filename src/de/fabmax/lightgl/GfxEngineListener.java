package de.fabmax.lightgl;

/**
 * Graphics engine interface.
 * 
 * @author fabmax
 *
 */
public interface GfxEngineListener {

    /**
     * Is called on Engine initialization. This method is called from the GLThread after the GL
     * context is (re-)created. Because of the possibility of GL context recreation this callback
     * might be called more than once, so be sure to drop all your GL related objects and reload
     * them if this method is called another time.
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

    /**
     * Is called every time before the main-pass is rendered. That is after the camera matrices in
     * the engine state are updated and after the pre-pass was rendered.
     * 
     * @param engine
     *            the graphics engine
     */
    public void onRenderMainPass(GfxEngine engine);

}
