package de.fabmax.lightgl;

/**
 * LightGlContext is used as context on rendering.
 */
public class LightGlContext {

    protected final GfxEngine mEngine;
    protected final GfxState mState;
    protected final ShaderManager mShaderManager;
    protected final TextureManager mTextureManager;

    /**
     * Creates a new LightGlContext for the given {@link GfxEngine}.
     *
     * @param engine    the engine to use with this context
     */
    public LightGlContext(GfxEngine engine) {
        mEngine = engine;
        mState = engine.getState();
        mShaderManager = engine.getShaderManager();
        mTextureManager = engine.getTextureManager();
    }

    /**
     * Returns the {@link GfxEngine}
     *
     * @return the {@link GfxEngine}
     */
    public final GfxEngine getEngine() {
        return mEngine;
    }

    /**
     * Returns the engine's {@link GfxState}, which contains the current transformation
     * matrices, etc.
     *
     * @return the engine's {@link GfxState}
     */
    public final GfxState getState() {
        return mState;
    }

    /**
     * Returns the engine's {@link TextureManager}, which is used to load and bind
     * textures.
     *
     * @return the engine's {@link TextureManager}
     */
    public final TextureManager getTextureManager() {
        return mTextureManager;
    }

    /**
     * Returns the engine's {@link ShaderManager}, which is used to load and bind
     * shaders.
     *
     * @return the engine's {@link ShaderManager}
     */
    public final ShaderManager getShaderManager() {
        return mShaderManager;
    }

}
