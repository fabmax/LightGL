package de.fabmax.lightgl.util;

import java.nio.FloatBuffer;
import java.util.Iterator;

/**
 * FloatList is a float array that dynamically increases its size while more elements are
 * added.
 * 
 * @author fabmax
 * 
 */
public class FloatList implements Iterable<Float> {

    private float[] mBuffer;
    private int mSize = 0;

    /**
     * Creates a FloatList with an initial capacity of 1000 elements.
     */
    public FloatList() {
        this(1000);
    }

    /**
     * Creates a FloatList with the specified initial capacity.
     * 
     * @param initialCapacity
     *            initial capacity of the underlying array
     */
    public FloatList(int initialCapacity) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException("initialCapacity must be >= 1");
        }
        mBuffer = new float[initialCapacity];
    }

    /**
     * Returns the list value at the specified index.
     * 
     * @param index
     *            index of the value to return
     * @return the list value at the specified index
     */
    public float get(int index) {
        if (index < 0 || index >= mSize) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return mBuffer[index];
    }

    /**
     * Creates a new float array and copies the content of this list to it.
     *
     * @return a newly created float array with the same content as this list
     */
    public float[] asArray() {
        float[] arr = new float[size()];
        copyToArray(arr);
        return arr;
    }

    /**
     * Copies the contents of the list to the specified float array. The array must have a
     * sufficient size.
     *
     * @param arr   target float array
     */
    public void copyToArray(float[] arr) {
        System.arraycopy(mBuffer, 0, arr, 0, size());
    }
    
    /**
     * Creates a new FloatBuffer and copies the content of this list to it.
     * 
     * @return a newly created FloatBuffer with the same content as this list
     */
    public FloatBuffer asBuffer() {
        FloatBuffer buf = BufferHelper.createFloatBuffer(size());
        copyToBuffer(buf);
        return buf;
    }

    /**
     * Copies the contents of the list to the specified FloatBuffer.
     *
     * @param fb
     *            target FloatBuffer
     */
    public void copyToBuffer(FloatBuffer fb) {
        fb.put(mBuffer, 0, mSize);
        fb.rewind();
    }

    /**
     * Adds a value to the end of the list.
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
     * Adds all values of the specified array to the end of the list.
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
     * Returns the number of elements in this list.
     * 
     * @return the number of elements in this list
     */
    public int size() {
        return mSize;
    }

    /**
     * Returns whether this list is empty or not.
     * 
     * @return true if this list is empty, false otherwise
     */
    public boolean isEmpty() {
        return mSize == 0;
    }

    /**
     * Removes all elements from this list. The underlying buffer is not deleted.
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
     * Returns a Iterator that iterates over this FloatList.
     */
    @Override
    public Iterator<Float> iterator() {
        return new ArrayIterator();
    }

    /**
     * Iterator class for FloatList.
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
         * Returns the next list element.
         * 
         * @return the next list element
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
            FloatList.this.remove(--mIndex);
        }
    }
}
