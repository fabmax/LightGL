package de.fabmax.lightgl;

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
    public void onRender(LightGlContext glContext) {
        // render scene
        Node scene = glContext.getEngine().getScene();
        if(scene != null) {
            scene.render(glContext);
        }
    }

}
