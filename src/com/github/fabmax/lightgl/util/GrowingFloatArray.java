package com.github.fabmax.lightgl.util;

import java.nio.FloatBuffer;
import java.util.Iterator;

/**
 * GrowingFloatArray is a float array that dynamically increases its size while more elements are
 * added.
 * 
 * @author fabmax
 * 
 */
public class GrowingFloatArray implements Iterable<Float> {

    private float[] mBuffer;
    private int mSize = 0;

    /**
     * Creates a GrowingFloatArray with an initial capacity of 1000 elements.
     */
    public GrowingFloatArray() {
        this(1000);
    }

    /**
     * Creates a GrowingFloatArray with the specified initial capacity.
     * 
     * @param initialCapacity
     *            initial capacity of the underlying array
     */
    public GrowingFloatArray(int initialCapacity) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException("initialCapacity must be >= 1");
        }
        mBuffer = new float[initialCapacity];
    }

    /**
     * Returns the array value at the specified index.
     * 
     * @param index
     *            index of the value to return
     * @return the array value at the specified index
     */
    public float get(int index) {
        if (index < 0 || index >= mSize) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return mBuffer[index];
    }
    
    /**
     * Copies the contents of the array to the specified FloatBuffer.
     * 
     * @param fb
     *            target FloatBuffer
     */
    public void copyToBuffer(FloatBuffer fb) {
        fb.put(mBuffer, 0, mSize);
        fb.rewind();
    }

    /**
     * Adds a value to the end of the array.
     * 
     * @param f
     *            the value to add
     */
    public void add(float f) {
        if (mSize == mBuffer.length) {
            // existing buffer is full, increase buffer size
            increaseSize();
        }
        mBuffer[mSize++] = f;
    }

    /**
     * Adds all values of the specified array to the end of the array.
     * 
     * @param f
     *            the array to add
     */
    public void add(float[] f) {
        for (int i = 0; i < f.length; i++) {
            add(f[i]);
        }
    }

    /**
     * Removes the element at the specified position.
     * 
     * @param index
     *            position of the element to remove
     */
    public void remove(int index) {
        if (index < 0 || index >= mSize) {
            throw new ArrayIndexOutOfBoundsException(index);
        }

        if (index == mSize - 1) {
            // special case remove last element
            mSize--;
        } else {
            // move elements behind index
            System.arraycopy(mBuffer, index + 1, mBuffer, index, mSize - index - 1);
            mSize--;
        }
    }

    /**
     * Returns the number of elements in this array.
     * 
     * @return the number of elements in this array
     */
    public int size() {
        return mSize;
    }

    /**
     * Returns whether this array is empty or not.
     * 
     * @return true if this array is empty, false otherwise
     */
    public boolean isEmpty() {
        return mSize == 0;
    }

    /**
     * Removes all elements from this array. The underlying buffer is not deleted.
     */
    public void clear() {
        mSize = 0;
    }

    /**
     * Creates a new buffer with doubled size and copies the contents.
     */
    private void increaseSize() {
        // allocate new doubled sized buffer
        float[] newBuffer = new float[mBuffer.length * 2];
        // copy existing buffer
        System.arraycopy(mBuffer, 0, newBuffer, 0, mSize);
        // use new buffer
        mBuffer = newBuffer;
    }

    /**
     * Returns a Iterator that iterates over this GrowingFloatArray.
     */
    @Override
    public Iterator<Float> iterator() {
        return new ArrayIterator();
    }

    /**
     * Iterator class for GrowingFloatArray.
     * 
     * @author fabmax
     * 
     */
    private class ArrayIterator implements Iterator<Float> {
        // iterator position
        private int mIndex = 0;

        /**
         * Checks whether there are more elements to iterate.
         * 
         * @return true if elements are left, false otherwise
         * @see Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return mIndex < mSize;
        }

        /**
         * Returns the next array element.
         * 
         * @return the next array element
         * @see Iterator#next()
         */
        @Override
        public Float next() {
            return mBuffer[mIndex++];
        }

        /**
         * Removes the element that was previously returned on next().
         * 
         * @see Iterator#remove()
         */
        @Override
        public void remove() {
            GrowingFloatArray.this.remove(--mIndex);
        }
    }
}
