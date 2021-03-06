package de.fabmax.lightgl.util;

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

    private final Pointer[] mPointers = new Pointer[MAX_POINTERS];

    /**
     * Creates a new BufferedTouchListener.
     */
    public BufferedTouchListener() {
        for (int i = 0; i < MAX_POINTERS; i++) {
            mPointers[i] = new Pointer();
        }
    }

    /**
     * Convenience method that returns the first pointer. This method can be used if no multi touch
     * gestures are to be considered.
     *
     * @return the first touch pointer
     */
    public Pointer getFirstPointer() {
        return mPointers[0];
    }

    /**
     * Returns the {@link Pointer} buffer. Notice that the buffer has always a size of
     * {@link #MAX_POINTERS}. Only Pointers that return true on {@link Pointer#isValid()} contain
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

        // clear update flag on all pointers
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
                pt.mId = id;
                pt.mCurrentTime = t;
                pt.swapCoords();
                event.getPointerCoords(i, pt.mCoords);
                if (!pt.mDown) {
                    // this pointer was not down before, swap again for getDX() and getDY() to return 0
                    pt.swapCoords();
                    pt.mId = id;
                    pt.mDownTime = t;
                    event.getPointerCoords(i, pt.mFirstCoords);
                }
                pt.mDown = true;
                pt.mUpdated = true;
            }
        }

        if (pointerCnt == 1 && event.getActionMasked() == MotionEvent.ACTION_UP) {
            // the last finger was lifted, all Pointers are up now
            for (Pointer pt : mPointers) {
                pt.mDown = false;
            }
        }

        for (Pointer pt : mPointers) {
            // pointers that where not updated are not down anymore
            if (!pt.mUpdated) {
                pt.mDown = false;
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
            } else if (pt.mId == POINTER_ID_UNUSED && freePt == null) {
                freePt = pt;
            }
        }
        return freePt;
    }

    /**
     * A Pointer represents a detected finger or tool on the screen. If {@link #isValid()} returns
     * false the Pointer is not valid.
     * 
     * @author fabmax
     * 
     */
    public static class Pointer {
        private int mId = POINTER_ID_UNUSED;
        private boolean mDown;
        private boolean mUpdated;

        private long mDownTime;
        private long mLastTime;
        private long mCurrentTime;
        
        private final PointerCoords mCoords = new PointerCoords();
        private final PointerCoords mLastCoords = new PointerCoords();
        private final PointerCoords mFirstCoords = new PointerCoords();
        
        /**
         * Recycles this Pointer. You should call this method after evaluating this Pointer.
         */
        public void recycle() {
            if (!mDown) {
                mId = POINTER_ID_UNUSED;
            }
        }
        
        /**
         * Returns the ID of this pointer. The ID will remain the same while the pointer isDown().
         */
        public int getId() {
            return mId;
        }

        /**
         * Returns true if this Pointer holds a valid finger screen position.
         * 
         * @return true if this Pointer holds a valid finger screen position
         */
        public boolean isValid() {
            return mId != POINTER_ID_UNUSED;
        }

        /**
         * Returns true if this Pointer holds a valid finger screen position.
         * 
         * @return true if this Pointer holds a valid finger screen position
         */
        public boolean isDown() {
            return mDown;
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
         * Returns the difference between the current and the first X screen position.
         * 
         * @return the difference between the current and the first X screen position
         */
        public float getOverallDX() {
            return mCoords.x - mFirstCoords.x;
        }

        /**
         * Returns the difference between the current and the first Y screen position.
         * 
         * @return the difference between the current and the first Y screen position
         */
        public float getOverallDY() {
            return mCoords.y - mFirstCoords.y;
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
        public int getDownTime() {
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
