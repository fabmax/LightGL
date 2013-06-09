package com.github.fabmax.lightgl.util;

import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * BufferedTouchListener implements an {@link OnTouchListener} that buffers all (multi-touch)
 * pointer positions of received {@link MotionEvent}. The buffer is not synchronized, hence pointer
 * positions retrieved via {@link #getPointers()} may change if a new {@link MotionEvent} is
 * received.
 * 
 * @author fabmax
 * 
 */
public class BufferedTouchListener implements OnTouchListener {
    public static final int MAX_POINTERS = 10;

    private static final int POINTER_ID_UNUSED = -1;

    private Pointer[] mPointers = new Pointer[MAX_POINTERS];

    /**
     * Creates a new BufferedTouchListener.
     */
    public BufferedTouchListener() {
        for (int i = 0; i < MAX_POINTERS; i++) {
            mPointers[i] = new Pointer();
        }
    }

    /**
     * Returns the {@link Pointer} buffer. Notice that the buffer has always a size of
     * {@link #MAX_POINTERS}. Only Pointers that return true on {@link Pointer#isActive()} contain
     * valid Pointer coordinates. The buffer returned by this method is used directly by the
     * {@link OnTouchListener} and is not synchronized. Hence the {@link Pointer} may change at any
     * time.
     * 
     * @return the {@link Pointer} buffer
     */
    public Pointer[] getPointers() {
        return mPointers;
    }

    /**
     * {@link OnTouchListener} implementation.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        long t = System.currentTimeMillis();
        
        for (Pointer pt : mPointers) {
            pt.mUpdated = false;
        }

        // update all pointers
        int pointerCnt = event.getPointerCount();
        for (int i = 0; i < pointerCnt && i < MAX_POINTERS; i++) {
            int id = event.getPointerId(i);

            // get the pointer for this pointer ID or a free one if the ID is not yet known
            Pointer pt = getPointerById(id);
            if (pt != null) {
                // update pointer
                pt.mUpdated = true;
                pt.mCurrentTime = t;
                pt.swapCoords();
                event.getPointerCoords(i, pt.mCoords);
                if (pt.mId != id) {
                    // this is a new pointer, swap again for getDX() and getDY() to return 0
                    pt.swapCoords();
                    pt.mId = id;
                    pt.mDownTime = t;
                }
            }
        }

        if (pointerCnt == 1 && event.getActionMasked() == MotionEvent.ACTION_UP) {
            // the last finger was lifted, all Pointers are invalid now
            for (Pointer pt : mPointers) {
                pt.mId = POINTER_ID_UNUSED;
            }
        } else {
            // mark pointers as unused that where not updated
            for (Pointer pt : mPointers) {
                if (!pt.mUpdated) {
                    pt.mId = POINTER_ID_UNUSED;
                }
            }
        }

        return true;
    }

    /**
     * Returns the {@link Pointer} with the specified ID or an unused {@link Pointer} if this ID is
     * not yet assigned.
     * 
     * @param id
     *            ID of the {@link Pointer} to return
     * @return The {@link Pointer} with the specified ID or an unused one.
     */
    private Pointer getPointerById(int id) {
        Pointer freePt = null;
        for (Pointer pt : mPointers) {
            if (pt.mId == id) {
                return pt;
            } else if (pt.mId == POINTER_ID_UNUSED) {
                freePt = pt;
            }
        }
        return freePt;
    }

    /**
     * A Pointer represents a detected finger or tool on the screen. If {@link #isActive()} returns
     * false the Pointer is not valid.
     * 
     * @author fabmax
     * 
     */
    public static class Pointer {
        private int mId = POINTER_ID_UNUSED;
        private boolean mUpdated;

        private long mDownTime;
        private long mLastTime;
        private long mCurrentTime;
        
        private PointerCoords mCoords = new PointerCoords();
        private PointerCoords mLastCoords = new PointerCoords();

        /**
         * Returns true if this Pointer holds a valid finger screen position.
         * 
         * @return true if this Pointer holds a valid finger screen position
         */
        public boolean isActive() {
            return mId != POINTER_ID_UNUSED;
        }

        /**
         * Returns the X coordinate of the current screen position.
         * 
         * @return the X coordinate of the current screen position
         */
        public float getX() {
            return mCoords.x;
        }

        /**
         * Returns the Y coordinate of the current screen position.
         * 
         * @return the Y coordinate of the current screen position
         */
        public float getY() {
            return mCoords.y;
        }

        /**
         * Returns the difference between the current and the previous X screen position.
         * 
         * @return the difference between the current and the previous X screen position
         */
        public float getDX() {
            return mCoords.x - mLastCoords.x;
        }

        /**
         * Returns the difference between the current and the previous Y screen position.
         * 
         * @return the difference between the current and the previous Y screen position
         */
        public float getDY() {
            return mCoords.y - mLastCoords.y;
        }
        
        /**
         * Returns the time between the last and the current position update.
         * 
         * @return the time between the last and the current position update
         */
        public int getDT() {
            return (int) (mCurrentTime - mLastTime);
        }
        
        /**
         * Returns the time between the first and the current position update.
         * 
         * @return the time between the first and the current position update
         */
        public int getActiveTime() {
            return (int) (mCurrentTime - mDownTime);
        }

        /**
         * Returns the {@link PointerCoords} of the current position.
         * 
         * @return the {@link PointerCoords} of the current position
         */
        public PointerCoords getCoords() {
            return mCoords;
        }

        /**
         * Returns the {@link PointerCoords} of the previous position.
         * 
         * @return the {@link PointerCoords} of the previous position
         */
        public PointerCoords getLastCoords() {
            return mLastCoords;
        }

        /**
         * Copies the current to the last position.
         */
        private void swapCoords() {
            mLastTime = mCurrentTime;
            
            mLastCoords.orientation = mCoords.orientation;
            mLastCoords.pressure = mCoords.pressure;
            mLastCoords.size = mCoords.size;
            mLastCoords.toolMajor = mCoords.toolMajor;
            mLastCoords.toolMinor = mCoords.toolMinor;
            mLastCoords.touchMajor = mCoords.touchMajor;
            mLastCoords.touchMinor = mCoords.touchMinor;
            mLastCoords.x = mCoords.x;
            mLastCoords.y = mCoords.y;
        }

    }
}
