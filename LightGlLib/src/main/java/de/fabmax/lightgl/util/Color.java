package de.fabmax.lightgl.util;

/**
 * Utility class which defines a RGBA color. This class can be used for easier vertex color
 * setting in meshes.
 * 
 * @author fth
 * 
 */
public class Color {

	public static final Color BLACK         = new Color(0.00f, 0.00f, 0.00f, 1.00f);
	public static final Color DARK_GRAY     = new Color(0.25f, 0.25f, 0.25f, 1.00f);
	public static final Color GRAY          = new Color(0.50f, 0.50f, 0.50f, 1.00f);
	public static final Color LIGHT_GRAY    = new Color(0.75f, 0.75f, 0.75f, 1.00f);
	public static final Color WHITE         = new Color(1.00f, 1.00f, 1.00f, 1.00f);
	
	public static final Color RED           = new Color(1.0f, 0.0f, 0.0f, 1.0f);
	public static final Color GREEN         = new Color(0.0f, 1.0f, 0.0f, 1.0f);
	public static final Color BLUE          = new Color(0.0f, 0.0f, 1.0f, 1.0f);
	public static final Color YELLOW        = new Color(1.0f, 1.0f, 0.0f, 1.0f);
	public static final Color CYAN          = new Color(0.0f, 1.0f, 1.0f, 1.0f);
	public static final Color MAGENTA       = new Color(1.0f, 0.0f, 1.0f, 1.0f);
	
	public static final Color LIGHT_RED     = new Color(1.0f, 0.5f, 0.5f, 1.0f);
	public static final Color LIGHT_GREEN   = new Color(0.5f, 1.0f, 0.5f, 1.0f);
	public static final Color LIGHT_BLUE    = new Color(0.5f, 0.5f, 1.0f, 1.0f);
	public static final Color LIGHT_YELLOW  = new Color(1.0f, 1.0f, 0.5f, 1.0f);
	public static final Color LIGHT_CYAN    = new Color(0.5f, 1.0f, 1.0f, 1.0f);
	public static final Color LIGHT_MAGENTA = new Color(1.0f, 0.5f, 1.0f, 1.0f);
	
	public static final Color DARK_RED      = new Color(0.5f, 0.0f, 0.0f, 1.0f);
	public static final Color DARK_GREEN    = new Color(0.0f, 0.5f, 0.0f, 1.0f);
	public static final Color DARK_BLUE     = new Color(0.0f, 0.0f, 0.5f, 1.0f);
	public static final Color DARK_YELLOW   = new Color(0.5f, 0.5f, 0.0f, 1.0f);
	public static final Color DARK_CYAN     = new Color(0.0f, 0.5f, 0.5f, 1.0f);
	public static final Color DARK_MAGENTA  = new Color(0.5f, 0.0f, 0.5f, 1.0f);


	/**
	 * Red color component [0 .. 1]
	 */
	public final float r;

	/**
	 * Green color component [0 .. 1]
	 */
	public final float g;

	/**
	 * Blue color component [0 .. 1]
	 */
	public final float b;

	/**
	 * Alpha color component [0 .. 1]
	 */
	public final float a;

    /**
     * Creates a RGBA color from the specified hex code (#RRGGBB) or (#RRGGBBAA).
     */
    public Color(String code) {
        r = Integer.parseInt(code.substring(1, 3), 16) / 255.0f;
        g = Integer.parseInt(code.substring(3, 5), 16) / 255.0f;
        b = Integer.parseInt(code.substring(5, 7), 16) / 255.0f;
        if (code.length() > 7) {
            a = Integer.parseInt(code.substring(7, 9), 16) / 255.0f;
        } else {
            a = 1;
        }
    }
	
	/**
	 * Creates a RGBA color with the specified color channel values.
	 */
	public Color(float r, float g, float b, float a) {
		this.r = GlMath.clamp(r, 0.0f, 1.0f);
		this.g = GlMath.clamp(g, 0.0f, 1.0f);
		this.b = GlMath.clamp(b, 0.0f, 1.0f);
		this.a = GlMath.clamp(a, 0.0f, 1.0f);
	}

    /**
     * Creates a RGBA color from HSV color model values.
     * 
     * @param h    hue [0 .. 360]
     * @param s    saturation [0 .. 1]
     * @param v    value [0 .. 1]
     * @param a    alpha [0 .. 1]
     * @return RGBA color
     */
	public static Color fromHSV(float h, float s, float v, float a) {
        int hi = (int) (h / 60.0f);
        float f = h / 60.0f - hi;
        float p = v * (1 - s);
        float q = v * (1 - s * f);
        float t = v * (1 - s * (1 - f));
    
        switch (hi) {
        case 1:
            return new Color(q, v, p, a);
        case 2:
            return new Color(p, v, t, a);
        case 3:
            return new Color(p, q, v, a);
        case 4:
            return new Color(t, p, v, a);
        case 5:
            return new Color(v, p, q, a); 
        default:
            return new Color(v, t, p, a);
        }
	}
	
	/**
	 * Sets the given float array to the RGBA value of this color.
	 *   
	 * @param dst    float array to set to this color value
	 * @param off    float array offset where the color is set
	 */
	public void set(float[] dst, int off) {
		dst[off] = r;
		dst[off + 1] = g;
		dst[off + 2] = b;
		dst[off + 3] = a;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Color color = (Color) o;

        if (Float.compare(color.a, a) != 0) return false;
        if (Float.compare(color.b, b) != 0) return false;
        if (Float.compare(color.g, g) != 0) return false;
        if (Float.compare(color.r, r) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (r != +0.0f ? Float.floatToIntBits(r) : 0);
        result = 31 * result + (g != +0.0f ? Float.floatToIntBits(g) : 0);
        result = 31 * result + (b != +0.0f ? Float.floatToIntBits(b) : 0);
        result = 31 * result + (a != +0.0f ? Float.floatToIntBits(a) : 0);
        return result;
    }

    public static void hsv2Float(float h, float s, float v, float[] dst, int off) {
        int hi = (int) (h / 60.0f);
        float f = h / 60.0f - hi;
        float p = v * (1 - s);
        float q = v * (1 - s * f);
        float t = v * (1 - s * (1 - f));

        switch (hi) {
            case 1:
                dst[off] = q; dst[off+1] = v; dst[off+2] = p; break;
            case 2:
                dst[off] = p; dst[off+1] = v; dst[off+2] = t; break;
            case 3:
                dst[off] = p; dst[off+1] = q; dst[off+2] = v; break;
            case 4:
                dst[off] = t; dst[off+1] = p; dst[off+2] = v; break;
            case 5:
                dst[off] = v; dst[off+1] = p; dst[off+2] = q; break;
            default:
                dst[off] = v; dst[off+1] = t; dst[off+2] = p; break;
        }
    }
}
