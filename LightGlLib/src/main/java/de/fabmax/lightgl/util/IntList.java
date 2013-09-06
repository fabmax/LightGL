package de.fabmax.lightgl.util;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Iterator;

/**
 * IntList is a integer array that dynamically increases its size while more elements are
 * added.
 * 
 * @author fabmax
 * 
 */
public class IntList implements Iterable<Integer> {

    private int[] mBuffer;
    private int mSize = 0;

    /**
     * Creates a IntList with an initial capacity of 1000 elements.
     */
    public IntList() {
        this(1000);
    }

    /**
     * Creates a IntList with the specified initial capacity.
     * 
     * @param initialCapacity
     *            initial capacity of the underlying array
     */
    public IntList(int initialCapacity) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException("initialCapacity must be >= 1");
        }
        mBuffer = new int[initialCapacity];
    }

    /**
     * Returns the list value at the specified index.
     * 
     * @param index
     *            index of the value to return
     * @return the list value at the specified index
     */
    public int get(int index) {
        if (index < 0 || index >= mSize) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return mBuffer[index];
    }
    
    /**
     * Creates a new IntBuffer and copies the content of this list to it.
     * 
     * @return a newly created IntBuffer with the same content as this list
     */
    public IntBuffer asBuffer() {
        IntBuffer buf = BufferHelper.createIntBuffer(size());
        copyToBuffer(buf);
        return buf;
    }
    
    /**
     * Copies the contents of the list to the specified IntBuffer.
     * 
     * @param ib
     *            target IntBuffer
     */
    public void copyToBuffer(IntBuffer ib) {
        ib.put(mBuffer, 0, mSize);
        ib.rewind();
    }
    
    /**
     * Copies the contents of the list to the specified ShortBuffer.
     * 
     * @param sb
     *            target ShortBuffer
     */
    public void copyToBuffer(ShortBuffer sb) {
        for (int i = 0; i < mSize; i++) {
            sb.put((short) mBuffer[i]);
        }
        sb.rewind();
    }

    /**
     * Adds a value to the end of the list.
     * 
     * @param i
     *            the value to add
     */
    public void add(int i) {
        if (mSize == mBuffer.length) {
            // existing buffer is full, increase buffer size
            increaseSize();
        }
        mBuffer[mSize++] = i;
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
        int[] newBuffer = new int[mBuffer.length * 2];
        // copy existing buffer
        System.arraycopy(mBuffer, 0, newBuffer, 0, mSize);
        // use new buffer
        mBuffer = newBuffer;
    }

    /**
     * Returns a Iterator that iterates over this IntList.
     */
    @Override
    public Iterator<Integer> iterator() {
        return new ArrayIterator();
    }

    /**
     * Iterator class for IntList.
     */
    private class ArrayIterator implements Iterator<Integer> {
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
        public Integer next() {
            return mBuffer[mIndex++];
        }

        /**
         * Removes the element that was previously returned on next().
         * 
         * @see Iterator#remove()
         */
        @Override
        public void remove() {
            IntList.this.remove(--mIndex);
        }
    }
}
