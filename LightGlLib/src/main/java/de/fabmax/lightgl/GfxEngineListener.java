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
     * @param glContext    the graphics engine context
     */
    public void onLoadScene(LightGlContext glContext);
    
    /**
     * Is called when the GL viewport changes its size.
     * 
     * @param width new viewport width in pixels
     * @param height new viewport height in pixels
     */
    public void onViewportChange(int width, int height);
    
    /**
     * Is called every time before a new frame is rendered.
     *
     * @param glContext    the graphics engine context
     */
    public void onRenderFrame(LightGlContext glContext);

    /**
     * Is called every time before the main-pass is rendered. That is after the camera matrices in
     * the engine state are updated and after the pre-pass was rendered.
     *
     * @param glContext    the graphics engine context
     */
    public void onRenderMainPass(LightGlContext glContext);

}
