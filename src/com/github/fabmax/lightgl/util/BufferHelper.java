package com.github.fabmax.lightgl.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * Helper class for creating various kinds of nio buffers. All buffers created by this class are
 * based on ByteBuffers with native byte order.
 * 
 * @author fabmax
 * 
 */
public class BufferHelper {

    /**
     * Creates a ByteBuffer with the specified capacity.
     * 
     * @param capacity
     *            buffer capacity
     * @return ByteBuffer with the specified capacity
     */
    public static ByteBuffer createByteBuffer(int capacity) {
        ByteBuffer b = ByteBuffer.allocateDirect(capacity);
        b.order(ByteOrder.nativeOrder());
        return b;
    }

    /**
     * Creates a ByteBuffer from the specified byte array.
     * 
     * @param data
     *            byte array to convert to a ByteBuffer
     * @return ByteBuffer with the same content as data
     */
    public static ByteBuffer createByteBuffer(byte[] data) {
        ByteBuffer b = createByteBuffer(data.length);
        b.put(data);
        b.rewind();
        return b;
    }

    /**
     * Creates a FloatBuffer with the specified capacity.
     * 
     * @param capacity
     *            buffer capacity
     * @return FloatBuffer with the specified capacity
     */
    public static FloatBuffer createFloatBuffer(int capacity) {
        return createByteBuffer(capacity * 4).asFloatBuffer();
    }

    /**
     * Creates a FloatBuffer from the specified float array.
     * 
     * @param data
     *            float array to convert to a FloatBuffer
     * @return FloatBuffer with the same content as data
     */
    public static FloatBuffer createFloatBuffer(float[] data) {
        FloatBuffer b = createFloatBuffer(data.length);
        b.put(data);
        b.rewind();
        return b;
    }

    /**
     * Creates a IntBuffer with the specified capacity.
     * 
     * @param capacity
     *            buffer capacity
     * @return IntBuffer with the specified capacity
     */
    public static IntBuffer createIntBuffer(int capacity) {
        return createByteBuffer(capacity * 4).asIntBuffer();
    }

    /**
     * Creates a IntBuffer from the specified int array.
     * 
     * @param data
     *            int array to convert to a IntBuffer
     * @return IntBuffer with the same content as data
     */
    public static IntBuffer createIntBuffer(int[] data) {
        IntBuffer b = createIntBuffer(data.length);
        b.put(data);
        b.rewind();
        return b;
    }

    /**
     * Creates a ShortBuffer with the specified capacity.
     * 
     * @param capacity
     *            buffer capacity
     * @return ShortBuffer with the specified capacity
     */
    public static ShortBuffer createShortBuffer(int capacity) {
        return createByteBuffer(capacity * 2).asShortBuffer();
    }

    /**
     * Creates a ShortBuffer from the specified short array.
     * 
     * @param data
     *            short array to convert to a ShortBuffer
     * @return ShortBuffer with the same content as data
     */
    public static ShortBuffer createShortBuffer(short[] data) {
        ShortBuffer b = createShortBuffer(data.length);
        b.put(data);
        b.rewind();
        return b;
    }
}
