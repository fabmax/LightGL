package de.fabmax.lightgl;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.glClear;

import de.fabmax.lightgl.scene.Node;

/**
 * The ScreenRenderPass is the default render pass to render a scene directly to the screen.
 * 
 * @author fabmax
 *
 */
public class ScreenRenderPass implements RenderPass {

    /**
     * Renders the scene directly to the screen.
     */
    @Override
    public void onRender(GfxEngine engine) {
        // clear screen
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        // render scene
        Node scene = engine.getScene();
        if(scene != null) {
            scene.render(engine.getState());
        }
    }

}
