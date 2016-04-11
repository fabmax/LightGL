package de.fabmax.lightgl.scene;

import de.fabmax.lightgl.ColorShader;
import de.fabmax.lightgl.LightGlContext;
import de.fabmax.lightgl.util.Color;

import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.glLineWidth;

/**
 * Created by fth on 24.02.14.
 */
public class DynamicLineMesh extends DynamicMesh {

    private float mLineWidth = 1.0f;
    private final float[] vertexBuf = new float[7];

    public DynamicLineMesh(int maxLines, LightGlContext glContext) {
        super(maxLines * 2, maxLines * 2, false, false, true, GL_LINES);

        setShader(new ColorShader(glContext.getShaderManager()));
    }

    public float getLineWidth() {
        return mLineWidth;
    }

    public void setLineWidth(float lineWidth) {
        this.mLineWidth = lineWidth;
    }

    public void addLine(int vertIdx0, int vertIdx1) {
        addElementIndex(vertIdx0);
        addElementIndex(vertIdx1);
    }

    public void addLine(float x0, float y0, float z0, Color c0,
    					float x1, float y1, float z1, Color c1) {
    	int idx0 = addVertex(x0, y0, z0, c0);
    	int idx1 = addVertex(x1, y1, z1, c1);
        addLine(idx0, idx1);
    }

    public void addLine(float[] pos0, int pos0Off, float[] col0, int col0Off,
                        float[] pos1, int pos1Off, float[] col1, int col1Off) {
        int idx0 = addVertex(pos0, pos0Off, col0, col0Off);
        int idx1 = addVertex(pos1, pos1Off, col1, col1Off);
        addLine(idx0, idx1);
    }

    public void extendLine(float x, float y, float z, Color c) {
    	int idx = addVertex(x, y, z, c);
        if (idx > 0) {
            addLine(idx - 1, idx);
        }
    }

    public void extendLine(float[] position, int posOff, float[] color, int colOff) {
        int idx = addVertex(position, posOff, color, colOff);
        if (idx > 0) {
            addLine(idx - 1, idx);
        }
    }
    
    public int addVertex(float x, float y, float z, Color color) {
    	vertexBuf[0] = x;
    	vertexBuf[1] = y;
    	vertexBuf[2] = z;
    	vertexBuf[3] = color.r;
    	vertexBuf[4] = color.g;
    	vertexBuf[5] = color.b;
    	vertexBuf[6] = color.a;
    	return super.addVertex(vertexBuf, 0, vertexBuf, 3);
    }

    @Override
    protected void drawElements(LightGlContext context) {
        if (mLineWidth != 1.0f) {
            glLineWidth(mLineWidth);
        }
        super.drawElements(context);
        if (mLineWidth != 1.0f) {
            glLineWidth(1.0f);
        }
    }
}
