package org.openthos.compress.common;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by MingWang on 2017/10/6.
 */

public class OnDoubleClickListener implements View.OnTouchListener {

    private View.OnClickListener mSingleClickCallBack, mDoubleClickCallBack;
    private long mLastTapTime = 0;
    private int mTapCount = 0;
    private final int mInterval = 500;

    public OnDoubleClickListener(View.OnClickListener singleClickCallBack,
                                 View.OnClickListener doubleClickCallBack) {
        mSingleClickCallBack = singleClickCallBack;
        mDoubleClickCallBack = doubleClickCallBack;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (MotionEvent.ACTION_DOWN == motionEvent.getAction()) {
            mTapCount++;
            mSingleClickCallBack.onClick(view);
            if (mTapCount == 2) {
                if (System.currentTimeMillis() - mLastTapTime < mInterval) {
                    mDoubleClickCallBack.onClick(view);
                    mLastTapTime = 0;
                    mTapCount = 0;
                } else {
                    mLastTapTime = System.currentTimeMillis();
                    mTapCount = 1;
                }
            } else {
                mLastTapTime = System.currentTimeMillis();
            }
        }
        return true;
    }
}
