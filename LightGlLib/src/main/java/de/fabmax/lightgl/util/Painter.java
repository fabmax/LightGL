package de.fabmax.lightgl.util;


import android.graphics.Typeface;
import android.opengl.Matrix;

import de.fabmax.lightgl.ColorShader;
import de.fabmax.lightgl.LightGlContext;
import de.fabmax.lightgl.RenderPass;
import de.fabmax.lightgl.ShadowRenderPass;
import de.fabmax.lightgl.ShadowShader;
import de.fabmax.lightgl.Texture;
import de.fabmax.lightgl.TextureShader;
import de.fabmax.lightgl.scene.DynamicMesh;

public class Painter {

    private float alpha = 1;
    private float lineThickness = 1;
    private GlFont defaultFont;
    private GlFont font;

    private final LightGlContext glContext;

    private final DynamicMesh mesh;
    private final MeshBuilder builder;

    private final DynamicMesh fontMesh;
    private final TextureShader fontShader;
    private final MeshBuilder fontBuilder;

    private final float[] pos = new float[8];
	private final float[] color = new float[4];

    private float[][] mSoftTranslation = new float[20][3];
    private int mTranslationIdx = 0;

    public Painter(LightGlContext glContext) {
        GlFont.onContextCreated();
        GlFont.FontConfig fontConfig = new GlFont.FontConfig(Typeface.DEFAULT, 72);

        this.glContext = glContext;
        this.defaultFont = GlFont.createFont(glContext, fontConfig, Color.BLACK);
        this.font = this.defaultFont;

        builder = new MeshBuilder(true, false, true);
        mesh = new DynamicMesh(10000, 10000, true, false, true);

        pos[5] = 0;
        pos[6] = 0;
        pos[7] = 1;

        RenderPass prePass = glContext.getEngine().getPreRenderPass();
        if (prePass != null && prePass instanceof ShadowRenderPass) {
            mesh.setShader(ShadowShader.createNoLightingShadowShader(
                    glContext.getShaderManager(), (ShadowRenderPass) prePass));
        } else {
            mesh.setShader(new ColorShader(glContext.getShaderManager()));
        }

        fontShader = new TextureShader(glContext.getShaderManager());
        fontShader.setTexture(glContext, font.getFontTexture());
        fontBuilder = new MeshBuilder(false, true, false);
        fontMesh = new DynamicMesh(10000, 10000, false, true, false);
        fontMesh.setShader(fontShader);

        setColor(Color.BLACK);
    }

    public void setDefaultConfig() {
        setAlpha(1);
        setLineThickness(1);
        setDefaultFont();

    }

    public void setDefaultFont() {
        font = defaultFont;
        fontShader.setTexture(glContext, font.getFontTexture());
    }

    public void setFont(GlFont font) {
        commit();
        this.font = font;
        fontShader.setTexture(glContext, this.font.getFontTexture());
    }

    public GlFont getFont() {
        return font;
    }

    public MeshBuilder getMeshBuilder() {
        return builder;
    }

    public void drawMeshData(MeshData data) {
        commit();
        builder.addMeshData(data);
    }

    public void setAlpha(float alpha) {
        if (alpha != this.alpha) {
            if (fontBuilder.getVertexCount() > 0) {
                commit();
            }
            this.alpha = alpha;
            color[3] *= alpha;
            fontShader.setAlpha(glContext, alpha);
        }
    }

    public LightGlContext getGlContext() {
        return glContext;
    }

    public void commit() {
        if (builder.getVertexCount() > 0) {
            mesh.updateMeshData(builder);
            mesh.render(glContext);
            builder.clear();
        }
        if (fontBuilder.getVertexCount() > 0) {
            fontMesh.updateMeshData(fontBuilder);
            fontMesh.render(glContext);
            fontBuilder.clear();
        }
    }

    public void reset() {
        setFont(defaultFont);
        setColor(Color.YELLOW);
    }

    public void drawTexture(Texture texture, float x, float y, float width, float height) {
        if (!fontBuilder.isEmpty()) {
            fontMesh.updateMeshData(fontBuilder);
            fontMesh.render(glContext);
            fontBuilder.clear();
        }

        fontShader.setTexture(glContext, texture);
        addTexQuad(x, y, width, height);
        fontMesh.updateMeshData(fontBuilder);
        fontMesh.render(glContext);
        fontBuilder.clear();
        fontShader.setTexture(glContext, font.getFontTexture());
    }

    public void pushTransform() {
        commit();
        glContext.getState().pushModelMatrix();
    }

    public void popTransform() {
        commit();
        glContext.getState().popModelMatrix();
    }

    public void rotate(float degrees) {
        commit();
        Matrix.rotateM(glContext.getState().getModelMatrix(), 0, degrees, 0, 0, 1);
        glContext.getState().matrixUpdate();
    }

    public void scale(float sX, float sY, float sZ) {
        commit();
        Matrix.scaleM(glContext.getState().getModelMatrix(), 0, sX, sY, sZ);
        glContext.getState().matrixUpdate();
    }

    public void translate(float tX, float tY, float tZ) {
        commit();
        Matrix.translateM(glContext.getState().getModelMatrix(), 0, tX, tY, tZ);
        glContext.getState().matrixUpdate();
    }

    public void softTranslate(float tX, float tY, float tZ) {
        mSoftTranslation[mTranslationIdx][0] += tX;
        mSoftTranslation[mTranslationIdx][1] += tY;
        mSoftTranslation[mTranslationIdx][2] += tZ;
    }

    public void pushSoftTransform() {
        mTranslationIdx++;
    }

    public void popSoftTransform() {
        mTranslationIdx--;
    }

    public void setColor(Color color) {
        setColor(color.r, color.g, color.b, color.a);
    }

    public void setColor(Color color, float alpha) {
        setColor(color.r, color.g, color.b, alpha);
    }

    public void setColor(float[] argb) {
        setColor(argb[0], argb[1], argb[2], argb[3]);
    }

	public void setColor(float r, float g, float b, float a) {
        color[0] = r;
        color[1] = g;
        color[2] = b;
        color[3] = a * alpha;
	}

    public void setLineThickness(float thickness) {
        lineThickness = thickness;
    }

    public void drawArc(float x, float y, float radius, float start, float sweep) {
        int degPerStep = (int) (45 / (radius / 5));
        if (degPerStep < 5) {
            degPerStep = 5;
        } else if (degPerStep > 45) {
            degPerStep = 45;
        }
        int steps = (int) Math.abs(sweep / degPerStep);
        drawArc(x, y, radius, start, sweep, steps);
    }

    private void drawArc(float x, float y, float radius, float start, float sweep, int steps) {
        if (Math.abs(sweep) < 0.001 || Float.isNaN(sweep)) {
            return;
        }

        if (sweep < 0) {
            sweep = -sweep;
            start -= sweep;
        }
        if (sweep > 360) {
            sweep = 360;
        }

        float s = sweep / steps;
        for (int i = 0; i < steps; i++) {
            float as = (float) Math.toRadians(start + i * s);
            float ae = (float) Math.toRadians(start + (i + 1) * s);

            float cos0 = (float) Math.cos(as);
            float cos1 = (float) Math.cos(ae);
            float sin0 = (float) Math.sin(as);
            float sin1 = (float) Math.sin(ae);

            drawLine(x + cos0 * radius, y - sin0 * radius, x + cos1 * radius, y - sin1 * radius);
        }
    }

    public void drawCircle(float x, float y, float r) {
        drawArc(x, y, r, 0, 360);
    }

    public void drawLine(float x0, float y0, float x1, float y1) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float len = (float) Math.sqrt(dx * dx + dy * dy);

        float addX = lineThickness * .25f * dx / len;
        float addY = lineThickness * .25f * dy / len;
        x1 += addX;
        x0 -= addX;
        y1 += addY;
        y0 -= addY;
        dx += addX + addX;
        dy += addY + addY;
        len += lineThickness;

        float dxu = dx / len * lineThickness / 2;
        float dyu = dy / len * lineThickness / 2;

        float qx0 = x0 - dyu;
        float qy0 = y0 + dxu;

        float qx1 = x1 - dyu;
        float qy1 = y1 + dxu;

        float qx2 = x1 + dyu;
        float qy2 = y1 - dxu;

        float qx3 = x0 + dyu;
        float qy3 = y0 - dxu;

        addQuad(qx0, qy0, qx1, qy1, qx2, qy2, qx3, qy3);
    }

    public void drawRect(float x, float y, float width, float height) {
        drawLine(x, y, x, y + height);
        drawLine(x, y + height, x + width, y + height);
        drawLine(x + width, y + height, x + width, y);
        drawLine(x + width, y, x, y);
    }

    public float drawString(float x, float y, String str) {
        x += mSoftTranslation[mTranslationIdx][0];
        y += mSoftTranslation[mTranslationIdx][1];
        return font.drawString(str, x, y, 0, fontBuilder);
    }

    public void fillArc(float x, float y, float rInner, float rOuter, float start, float sweep) {
        int degPerStep = (int) (45 / (rOuter / 5));
        if (degPerStep < 5) {
            degPerStep = 5;
        } else if (degPerStep > 45) {
            degPerStep = 45;
        }
        int steps = (int) Math.abs(sweep / degPerStep);
        fillArc(x, y, rInner, rOuter, start, sweep, steps);
    }

    private void fillArc(float x, float y, float rInner, float rOuter, float start, float sweep, int steps) {
        if (Math.abs(sweep) < 0.001 || Float.isNaN(sweep)) {
            return;
        }

        if (sweep < 0) {
            sweep = -sweep;
            start -= sweep;
        }
        if (sweep > 360) {
            sweep = 360;
        }

        float s = sweep / steps;
        for (int i = 0; i < steps; i++) {
            float as = (float) Math.toRadians(start + i * s);
            float ae = (float) Math.toRadians(start + (i + 1) * s);

            float cos0 = (float) Math.cos(as);
            float cos1 = (float) Math.cos(ae);
            float sin0 = (float) Math.sin(as);
            float sin1 = (float) Math.sin(ae);

            float x0 = x + cos0 * rInner;
            float y0 = y - sin0 * rInner;

            float x1 = x + cos0 * rOuter;
            float y1 = y - sin0 * rOuter;

            float x2 = x + cos1 * rOuter;
            float y2 = y - sin1 * rOuter;

            float x3 = x + cos1 * rInner;
            float y3 = y - sin1 * rInner;

            addQuad(x0, y0, x1, y1, x2, y2, x3, y3);
        }
    }

    public void fillCircle(float x, float y, float radius) {
        fillPie(x, y, radius, 0, 360);
    }

    public void fillPie(float x, float y, float radius, float start, float sweep) {
        fillPie(x, y, radius, start, sweep, 36);
    }

    private void fillPie(float x, float y, float radius, float start, float sweep, int steps) {
        float s = sweep / steps;
        float a = (float) Math.toRadians(start);

        x += mSoftTranslation[mTranslationIdx][0];
        y += mSoftTranslation[mTranslationIdx][1];

        pos[0] = x;
        pos[1] = y;
        pos[2] = mSoftTranslation[mTranslationIdx][2];
        int idx0 = builder.addVertex(pos, 0, pos, 5, null, 0, color, 0);
        pos[0] = x + (float) Math.cos(a);
        pos[1] = y - (float) Math.sin(a);
        int idx1 = builder.addVertex(pos, 0, pos, 5, null, 0, color, 0);

        for (int i = 0; i <= steps; i++) {
            a = (float) Math.toRadians(start + i * s);
            pos[0] = x + (float) Math.cos(a) * radius;
            pos[1] = y - (float) Math.sin(a) * radius;
            int idx2 = builder.addVertex(pos, 0, pos, 5, null, 0, color, 0);
            builder.addTriangle(idx0, idx1, idx2);
            idx1 = idx2;
        }
    }

    public void fillRect(float x, float y, float width, float height) {
        addQuad(x, y, x, y + height, x + width, y + height, x + width, y);
    }

    public void fillRoundRect(float x, float y, float width, float height, float radius) {
        addQuad(x, y + radius, x, y + height - radius, x + width, y + height - radius, x + width, y + radius);
        addQuad(x + radius, y, x + radius, y + radius, x + width - radius, y + radius, x + width - radius, y);
        addQuad(x + radius, y + height - radius, x + radius, y + height, x + width - radius, y + height, x + width - radius, y + height - radius);
        fillPie(x + radius, y + radius, radius, 90, 90);
        fillPie(x + radius, y + height - radius, radius, 180, 90);
        fillPie(x + width - radius, y + height - radius, radius, 270, 90);
        fillPie(x + width - radius, y + radius, radius, 0, 90);
    }

    private void addQuad(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3) {
        float tx = mSoftTranslation[mTranslationIdx][0];
        float ty = mSoftTranslation[mTranslationIdx][1];
        float tz = mSoftTranslation[mTranslationIdx][2];

        pos[0] = x0 + tx;
        pos[1] = y0 + ty;
        pos[2] = tz;
        int idx0 = builder.addVertex(pos, 0, pos, 5, null, 0, color, 0);
        pos[0] = x1 + tx;
        pos[1] = y1 + ty;
        int idx1 = builder.addVertex(pos, 0, pos, 5, null, 0, color, 0);
        pos[0] = x2 + tx;
        pos[1] = y2 + ty;
        int idx2 = builder.addVertex(pos, 0, pos, 5, null, 0, color, 0);
        pos[0] = x3 + tx;
        pos[1] = y3 + ty;
        int idx3 = builder.addVertex(pos, 0, pos, 5, null, 0, color, 0);

        builder.addTriangle(idx0, idx1, idx2);
        builder.addTriangle(idx0, idx2, idx3);
    }

    private void addTexQuad(float x, float y, float width, float height) {
        float tx = mSoftTranslation[mTranslationIdx][0];
        float ty = mSoftTranslation[mTranslationIdx][1];
        float tz = mSoftTranslation[mTranslationIdx][2];

        pos[0] = x + tx;
        pos[1] = y + ty;
        pos[2] = tz;
        pos[3] = 0;
        pos[4] = 0;
        int idx0 = fontBuilder.addVertex(pos, 0, pos, 5, pos, 3, null, 0);
        pos[0] = x + tx;
        pos[1] = y + ty + height;
        pos[3] = 0;
        pos[4] = 1;
        int idx1 = fontBuilder.addVertex(pos, 0, pos, 5, pos, 3, null, 0);
        pos[0] = x + tx + width;
        pos[1] = y + ty + height;
        pos[3] = 1;
        pos[4] = 1;
        int idx2 = fontBuilder.addVertex(pos, 0, pos, 5, pos, 3, null, 0);
        pos[0] = x + tx + width;
        pos[1] = y + ty;
        pos[3] = 1;
        pos[4] = 0;
        int idx3 = fontBuilder.addVertex(pos, 0, pos, 5, pos, 3, null, 0);

        fontBuilder.addTriangle(idx0, idx1, idx2);
        fontBuilder.addTriangle(idx0, idx2, idx3);
    }
}
