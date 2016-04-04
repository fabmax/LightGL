package de.fabmax.lightgl.util;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;

import de.fabmax.lightgl.LightGlContext;
import de.fabmax.lightgl.Texture;
import de.fabmax.lightgl.TextureProperties;


public class GlFont {

    private static final int PADDING = 3;
	private static final HashMap<FontProps, GlFont> fonts = new HashMap<>();

    private static float sCummulatedCreateTime = 0;

    private final float fontSize;
    private final float lineSpace;
    private final float charSpace;
	
    private Texture fontTexture;
    private int texWidth;
    private int texHeight;

    private int minCharX;
    private int maxCharX;
    private int minCharY;
    private int maxCharY;
    private int maxCharWidth;
    private int maxCharHeight;
    
    private final float[] coordBuf = new float[5];

    private final HashMap<Character, Integer> charMap = new HashMap<>();
	private final Rect[] charBounds;
    private final float[] charAdvance;

    static void onContextCreated() {
        fonts.clear();
    }

	public static GlFont createFont(LightGlContext context, FontConfig font, Color color) {
		FontProps props = new FontProps(font, color);
		GlFont f = fonts.get(props);
		if (f == null) {
			// This font was not yet created
			f = new GlFont(context, props);
			fonts.put(props, f);
		}
		return f;
	}

	private GlFont(LightGlContext context, FontProps props) {
        fontSize = props.font.mSize;
        lineSpace = fontSize * 1.2f;
        charSpace = Math.max(1, fontSize / 15);

        int n = 0;
        for (Character key : props.font.mChars.getIndexMap().keySet()) {
            charMap.put(key, n++);
        }
        charBounds = new Rect[n];
        charAdvance = new float[n];

        long t = System.nanoTime();

		Bitmap fontImg = renderFontImage(props);
        long t0 = System.nanoTime() - t;
        Log.d("GlFont", String.format(Locale.ENGLISH, "Bitmap generation took %.3f ms", t0 / 1e6));

        TextureProperties texProps = new TextureProperties();
        texProps.minFilter = TextureProperties.MinFilterMethod.LINEAR;
        texProps.magFilter = TextureProperties.MagFilterMethod.LINEAR;
        fontTexture = context.getTextureManager().createTextureFromBitmap(fontImg, texProps);
        fontImg.recycle();

        t = System.nanoTime() - t;
        sCummulatedCreateTime += (float) (t / 1e6);
        Log.d("GlFont", String.format(Locale.ENGLISH, "Font creation took %.3f ms [%.3f ms]", t / 1e6, sCummulatedCreateTime));
	}

    private int charIdx(char c) {
        Integer idx = charMap.get(c);
        return idx == null ? -1 : idx;
    }

	public void delete() {
		if (fontTexture != null) {
            fontTexture.delete();
            fontTexture = null;
        }
	}

    public Texture getFontTexture() {
        return fontTexture;
    }

    public float getFontSize() {
        return fontSize;
    }

    public float getLineSpace() {
        return lineSpace;
    }

    public float getStringWidth(String str) {
        float w = 0;
        float wMax = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int idx = charIdx(c);
            if (idx >= 0) {
                w += charAdvance[idx];
            } else  if (c == '\n') {
                w = 0;
            }
            if (w > wMax) wMax = w;
        }
        return wMax;
    }

	public float drawString(String str, float x, float y, float z, MeshBuilder target) {
        float xBase = x;
        float yBase = y;

        if (!target.hasTextureCoordinates()) {
            throw new IllegalArgumentException("Target MeshBuilder must have texture coordinates enabled");
        }

        coordBuf[2] = z;

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int idx = charIdx(c);
            if (idx >= 0) {
                Rect b = charBounds[idx];
                int cw = b.right - b.left;
                int ch = b.bottom - b.top;
                int cx = (idx) % 16 * maxCharWidth - minCharX + b.left;
                int cy = (idx) / 16 * maxCharHeight - minCharY + b.top;

                coordBuf[0] = x + b.left;                         coordBuf[1] = y + b.bottom;
                coordBuf[3] = (float) cx / texWidth;              coordBuf[4] = (float) (cy + ch) / texHeight;
                int idx0 = target.addVertex(coordBuf, 0, null, 0, coordBuf, 3, null, 0);

                coordBuf[0] = x + b.left;                         coordBuf[1] = y + b.top;
                coordBuf[3] = (float) cx / texWidth;              coordBuf[4] = (float) cy / texHeight;
                int idx1 = target.addVertex(coordBuf, 0, null, 0, coordBuf, 3, null, 0);

                coordBuf[0] = x + b.right;                        coordBuf[1] = y + b.top;
                coordBuf[3] = (float) (cx + cw) / texWidth;       coordBuf[4] = (float) cy / texHeight;
                int idx2 = target.addVertex(coordBuf, 0, null, 0, coordBuf, 3, null, 0);

                coordBuf[0] = x + b.right;                        coordBuf[1] = y + b.bottom;
                coordBuf[3] = (float) (cx + cw) / texWidth;       coordBuf[4] = (float) (cy + ch) / texHeight;
                int idx3 = target.addVertex(coordBuf, 0, null, 0, coordBuf, 3, null, 0);

                target.addTriangle(idx0, idx2, idx1);
                target.addTriangle(idx0, idx3, idx2);

                x += charAdvance[idx];

            } else  if (c == '\n') {
                x = xBase;
                y += lineSpace;
            }
        }
        return y + lineSpace - yBase;
	}

	private Bitmap renderFontImage(FontProps props) {
        Paint paint = new Paint();
        paint.setTypeface(props.font.mTypeface);
        paint.setTextSize(props.font.mSize);
        paint.setColor(props.getIntColor());
        paint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);

        minCharX = Integer.MAX_VALUE;
        minCharY = Integer.MAX_VALUE;
        maxCharX = Integer.MIN_VALUE;
        maxCharY = Integer.MIN_VALUE;

        // determine character bounds
        float[] width = new float[1];
        char[] str = new char[1];
        for (char c : charMap.keySet()) {
            int i = charIdx(c);
            str[0] = c;
            Rect r = new Rect();
            charBounds[i] = r;
            paint.getTextBounds(str, 0, 1, r);
            paint.getTextWidths(str, 0, 1, width);
            charAdvance[i] = width[0];

            r.left -= PADDING;
            //r.top -= PADDING;
            r.right += PADDING;
            //r.bottom += PADDING;

            if (r.left < minCharX) minCharX = r.left;
            if (r.top < minCharY) minCharY = r.top;
            if (r.right > maxCharX) maxCharX = r.right;
            if (r.bottom > maxCharY) maxCharY = r.bottom;
            //Log.d("GlFont", "'" + str[0] + "': " + r);
        }

        // compute needed texture size
        int cntW = charMap.size() > 16 ? 16 : charMap.size();
        int cntH = (charMap.size() - 1) / 16 + 1;
        int cW = maxCharX - minCharX;
        int cH = maxCharY - minCharY;
        maxCharWidth = cW;
        maxCharHeight = cH;
        int tW = cW * cntW;
        int tH = cH * cntH;

        // because some platforms (e.g. F***ING Tegra3...) have problems with non pow 2 texture
        // sizes we take the next best (larger) texture size
        texWidth = getNextPow2(tW);
        texHeight = getNextPow2(tH);
        Log.d("GlFont", String.format("Rendering font texture, texture size: %d x %d px (used: %d x %d)", texWidth, texHeight, tW, tH));

        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap fontImg = Bitmap.createBitmap(texWidth, texHeight, conf);
        Canvas canvas = new Canvas(fontImg);

        // render character map
        for (char c : charMap.keySet()) {
            int i = charIdx(c);
            int ix = i % 16;
            int iy = i / 16;
            int x = ix * cW;
            int y = iy * cH;
            canvas.drawText("" + c, x - minCharX, y - minCharY, paint);
        }

        // special character width for space and digits
        int iSpace = charIdx(' ');
        if (iSpace >= 0) {
            charBounds[iSpace].right = Math.round(props.font.mSize / 4 + PADDING * 2);
        }
        int iZero = charIdx('0');
        for (int i = 1; i <= 9 && iZero >= 0; i++) {
            int iDigit = charIdx((char) ('0' + i));
            charBounds[iDigit].left = charBounds[iZero].left;
            charBounds[iDigit].right = charBounds[iZero].right;
        }

        return fontImg;
	}

    private int getNextPow2(int size) {
        int pow2 = 128;
        while (pow2 < size) {
            pow2 <<= 1;
        }
        return pow2;
    }

	private static class FontProps {
		public final FontConfig font;
        public final Color color;

		public FontProps(FontConfig font, Color color) {
			this.font = font;
			this.color = color;
		}

        public int getIntColor() {
            return ((int) (color.a * 255) << 24) |
                   ((int) (color.r * 255) << 16) |
                   ((int) (color.g * 255) << 8)  |
                   ((int) (color.b * 255));
        }

        @Override
        public int hashCode() {
            int result = font.hashCode();
            result = 31 * result + color.hashCode();
            return result;
        }

        @Override
		public boolean equals(Object obj) {
            return obj instanceof FontProps && hashCode() == obj.hashCode();
		}
	}

    public static class FontConfig {
        private final String mTtfAssetPath;
        private final float mSize;
        private final Typeface mTypeface;
        private final CharMap mChars;

        public FontConfig(AssetManager assetMgr, String assetPath, CharMap chars, float size) {
            this(Typeface.createFromAsset(assetMgr, assetPath), chars, size);
        }

        public FontConfig(Typeface typeface, float size) {
            this(typeface, CharMap.ASCII_MAP, size);
        }

        public FontConfig(Typeface typeface, CharMap chars, float size) {
            mTypeface = typeface;
            mSize = size;
            mTtfAssetPath = "" + typeface.hashCode();
            mChars = chars;
        }

        @Override
        public int hashCode() {
            int result = mTtfAssetPath.hashCode();
            result = 31 * result + Float.floatToIntBits(mSize);
            result = 31 * result + mChars.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (o.getClass() != getClass()) {
                return false;
            }
            return o.hashCode() == hashCode();
        }
    }
}
