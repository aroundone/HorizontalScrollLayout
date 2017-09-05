package com.example.library;

import android.animation.Animator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by guoziliang on 2017/7/29.
 *
 * 只负责控制ChildView和DragView的运动，DragView的具体实现由DragView实现，通过接口传递
 */

public class HorizontalScrollLayout extends FrameLayout implements NestedScrollingParent{

    private final String TAG =  "HorizontalLayout";

    //可以拖拽出来的宽度
    private float mPullWidth = 200;
    //触发变色的宽度
    private float mDragCallBackWidth = 150;
    //子View，一般都是RecyclerView
    private View mChildView;
    //拖拽View，类似于Header
    private View dragView;

    private float mTouchStartX;

    private float mTouchCurX;

    private boolean isBackAniDoing;

    private NestedScrollingParentHelper mParentHelper;

    ValueAnimator backAni;

    public HorizontalScrollLayout(@NonNull Context context) {
        this(context, null, 0);
    }

    public HorizontalScrollLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalScrollLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        if (getChildCount() > 1) {
            throw new RuntimeException("you can only attach one child");
        }
        setAttrs(attrs);
        mParentHelper = new NestedScrollingParentHelper(this);
        backAni = new ValueAnimator();
        this.post(new Runnable() {
            @Override
            public void run() {
                mChildView = getChildAt(0);
            }
        });
    }

    private void setAttrs(AttributeSet attrs) {

    }

    public void addDragView(@NonNull View child) {
        super.addView(child);
        dragView = child;
    }

    void doDragAnimation() {
        PropertyValuesHolder holder = PropertyValuesHolder.ofFloat(TAG, mChildView.getTranslationX(), 0);
        doBackAnimation(holder, 500);
    }

    void doInertiaAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(0, -150);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mChildView != null) {
                    float val = (float) animation.getAnimatedValue();
                    mChildView.setTranslationX(val);
                    requestDragView((int)val);
                }
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                doDragAnimation();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animator.setDuration(300);
        animator.start();

    }

    private void doBackAnimation(PropertyValuesHolder holder, int duration) {
        if (mChildView == null || backAni == null) {
            return;
        }
        backAni.setValues(holder);
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

        backAni.setDuration(duration);
        backAni.start();
    }

    public void requestDragView(float val) {
        if (dragView == null) {
            return;
        }
        int width = (int)Math.abs(val);
        doDragCallBack(val);
        MarginLayoutParams layoutParams = (MarginLayoutParams) dragView.getLayoutParams();
        if (layoutParams != null) {
            layoutParams.width = width;
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
                    return true;
                }

                if (dx < 0 && !canChildScrollRight()) {
                    return true;
                }

                break;
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

                doHorizontalDx(operateDx(dx));
                if (onDragWidthChange != null) {
                    onDragWidthChange.onWidthChange((int)Math.abs(dx));
                }
                return true;
            case MotionEvent.ACTION_UP:

            case MotionEvent.ACTION_CANCEL:
                if (mChildView != null) {
                    if (Math.abs(mChildView.getTranslationX()) >= 0) {
                        doDragAnimation();
                    }
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }

    }

    void doHorizontalDx(float dx) {
        requestNestedView(dx);

        doDragCallBack(dx);
    }

    void requestNestedView(float dx) {
        if (mChildView != null) {
            mChildView.setTranslationX(dx);
        }
        requestDragView((int)dx);
    }

    void inertiaScroll() {
        doInertiaAnimation();
    }

    float operateDx(float dx) {
        dx = Math.max(-mPullWidth, dx);
        dx = Math.min(dx, 0);
        return dx;
    }

    private void doDragCallBack(float dx) {
        if (onDragCallBack != null) {
            if (Math.abs(dx) > mDragCallBackWidth) {
                onDragCallBack.onDrag();
            } else {
                onDragCallBack.onRelease();
            }
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

    public void setPullWidth(int pullWidth) {
        mPullWidth = pullWidth;
    }

    public void setDragCallBackWidth(int callBackWidth) {
        mDragCallBackWidth = callBackWidth;
    }

    public OnDragCallBack onDragCallBack;

    public void setOnDragCallBack(OnDragCallBack onDragCallBack) {
        this.onDragCallBack = onDragCallBack;
    }

    public interface OnDragCallBack{

        void onDrag();

        void onRelease();
    }

    public OnDragWidthChange onDragWidthChange;

    public void setOnDragWidthChange(OnDragWidthChange onDragWidthChange) {
        this.onDragWidthChange = onDragWidthChange;
    }

    public interface OnDragWidthChange{
        void onWidthChange(int dx);
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {

        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_HORIZONTAL) != 0;
    }
    @SuppressLint("NewApi")
    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {

        mParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
    }

    @Override
    public void onStopNestedScroll(View target) {
        mParentHelper.onStopNestedScroll(target);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {

        if (consumed && velocityX > 0 && !target.canScrollHorizontally(1)) {
            inertiaScroll();
        }
        return true;
    }
    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY)  {
        return false;
    }
    @Override
    public int getNestedScrollAxes() {
        return mParentHelper.getNestedScrollAxes();
    }

}
