package com.example.library;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

/**
 * Created by guoziliang on 2017/7/29.
 */

public class HorizontalLayout extends FrameLayout {

    private final String TAG =  "HorizontalLayout";

    private float mPullHeight;
    private float mHeaderHeight;
    private View mChildView;
    private DragView dragView;

    private float mTouchStartX;

    private float mTouchCurX;

    private boolean isBackAniDoing;


    public HorizontalLayout(@NonNull Context context) {
        this(context, null, 0);
    }

    public HorizontalLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        if (getChildCount() > 1) {
            throw new RuntimeException("you can only attach one child");
        }
        setAttrs(attrs);
        mPullHeight = 100;
        mHeaderHeight = 100;

        this.post(new Runnable() {
            @Override
            public void run() {
                mChildView = getChildAt(0);
                addDragView();
            }
        });
    }

    private void setAttrs(AttributeSet attrs) {

    }

    private void addDragView() {
        dragView = new DragView(getContext());
        LayoutParams params = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.RIGHT;
        dragView.setLayoutParams(params);
        addViewInternal(dragView);
    }

    private void addViewInternal(@NonNull View child) {
        super.addView(child);
    }


    private void doBackAnimation(float start, float end) {
        if (mChildView == null) {
            return;
        }
        ValueAnimator backAni = ValueAnimator.ofFloat(start, end);
        backAni.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (float) animation.getAnimatedValue();
                if (mChildView != null) {
                    mChildView.setTranslationX(val);
                    requestDragView((int)val);
                }
            }
        });

        backAni.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isBackAniDoing = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isBackAniDoing = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        backAni.setDuration(500);
        backAni.start();
    }

    public void requestDragView(int val) {
        if (dragView == null) {
            return;
        }
        val = Math.abs(val);
        MarginLayoutParams layoutParams = (MarginLayoutParams) dragView.getLayoutParams();
        if (layoutParams != null) {
            layoutParams.width = val;
            dragView.requestLayout();
        }
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchStartX = ev.getX();
                mTouchCurX = mTouchStartX;

                break;
            case MotionEvent.ACTION_MOVE:

                float curX = ev.getX();
                float dx = curX - mTouchStartX;

                if (dx > 0 && !canChildScrollLeft()) {
                    Log.d(TAG,  "onInterceptTouchEvent event Left");
                    return true;
                }

                if (dx < 0 && !canChildScrollRight()) {
                    Log.d(TAG,  "onInterceptTouchEvent event Right");
                    return true;
                }
        }
        return super.onInterceptTouchEvent(ev);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (isBackAniDoing) {
                    return super.onTouchEvent(event);
                }
                mTouchCurX = event.getX();
                float dx = mTouchCurX - mTouchStartX;

                dx = Math.max(-mPullHeight * 2, dx);
                dx = Math.min(dx, 0);

                if (mChildView != null) {
                    mChildView.setTranslationX(dx);
                    requestDragView((int)dx);
                }

                return true;
            case MotionEvent.ACTION_UP:

                Log.d(TAG,  "onTouchEvent up");
            case MotionEvent.ACTION_CANCEL:
                if (mChildView != null) {
                    if (Math.abs(mChildView.getTranslationX()) >= 0) {
                        doBackAnimation(mChildView.getTranslationX(), 0);
                    }
                }

                Log.d(TAG,  "onTouchEvent cancel");
                return true;
            default:
                return super.onTouchEvent(event);
        }

    }

    private boolean canChildScrollRight() {
        if (mChildView == null) {
            return false;
        }
        return ViewCompat.canScrollHorizontally(mChildView, 1);
    }

    private boolean canChildScrollLeft() {
        if (mChildView == null) {
            return false;
        }
        return ViewCompat.canScrollHorizontally(mChildView, -1);
    }
}
