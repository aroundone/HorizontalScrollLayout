package com.example.library;

import android.animation.Animator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;

/**
 * Created by guoziliang on 2017/7/29.
 * <p>
 * 只负责控制ChildView和DragView的运动，DragView的具体实现由DragView实现，这里只负责回调接口{@link #setOnDragCallBack(OnDragCallBack)}
 */

public class HorizontalScrollLayout extends FrameLayout implements NestedScrollingParent {

    private final String TAG = "HorizontalLayout";

    //手指拖拽回滚动画时间
    private int dragBackAniDuration = 500;
    //拖动到最后自动出现动画时间
    private int dragEndAniDuration = 300;
    //是否开始做动画
    private boolean isBackAniDoing;
    //可以拖拽出来的宽度
    private float mPullWidth = 200;
    //触发变色的宽度
    private float mDragCallBackWidth = 100;
    //NestScroll中检测手指滑动Dx之和，因为每次只会监听相对值移动，所以需要累加和重置
    private float scrollTotalDx = 0;
    //子View，一般都是RecyclerView
    private View mChildView;
    //拖拽View，类似于Header
    private View dragView;
    //监听滚动Scroller
    private Scroller mScroller;
    //回滚动画
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
        DecelerateInterpolator interpolator = new DecelerateInterpolator();
        mScroller = new Scroller(context, interpolator);
        initBackAni();
        this.post(new Runnable() {
            @Override
            public void run() {
                mChildView = getChildAt(0);
            }
        });
    }

    private void initBackAni() {
        backAni = new ValueAnimator();
        backAni.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (float) animation.getAnimatedValue();
                requestNestedView(val);
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
    }

    private void setAttrs(AttributeSet attrs) {

    }

    /**
     * 添加DragView
     *
     * @param child
     */
    public void addDragView(@NonNull View child) {
        super.addView(child);
        dragView = child;
    }

    /**
     * 执行抽屉拉出后回滚动画
     */
    void doDragBackAnimation() {
        PropertyValuesHolder holder = PropertyValuesHolder.ofFloat(TAG, mChildView.getTranslationX(), 0);
        doBackAnimation(holder, dragBackAniDuration);
    }

    /**
     * 执行拖拽手指Up时回滚动画
     */
    private void doBackAnimation(PropertyValuesHolder holder, int duration) {
        if (backAni == null || isBackAniDoing) {
            return;
        }
        backAni.setValues(holder);
        backAni.setDuration(duration);
        backAni.start();
    }

    /**
     * Scroller到最后的时候滚动出抽屉效果动效，次方法是执行拉出动效，待拉出结束之后会回调{@link #doBackAnimation(PropertyValuesHolder, int)}
     */
    void doInertiaAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(0, -mDragCallBackWidth);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                float val = (float) animation.getAnimatedValue();
                requestNestedView(val);
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                doDragBackAnimation();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animator.setDuration(dragEndAniDuration);
        animator.start();

    }


    /**
     * 刷新DragView，回调{@link #onDragCallBack}事件
     *
     * @param val 拖拽x值
     */
    private void requestDragView(float val) {
        if (dragView == null) {
            return;
        }
        int width = (int) Math.abs(val);
        doDragCallBack(val);
        MarginLayoutParams layoutParams = (MarginLayoutParams) dragView.getLayoutParams();
        if (layoutParams != null) {
            layoutParams.width = width;
            dragView.requestLayout();
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            if (!canChildScrollRight()) {
                //抽屉未拉出的时候不做动画
                if (!isDragViewIsShow()) {
                    doInertiaAnimation();
                }
                mScroller.forceFinished(true);
            } else {
                invalidate();
            }
        }
        super.computeScroll();
    }

    /**
     * 联动DragView和子View
     * <p>
     * 做动画和手指拖拽的时候都需要回调此方法更新UI
     *
     * @param dx
     */
    void requestNestedView(float dx) {
        //动子View
        if (mChildView != null) {
            mChildView.setTranslationX(dx);
        }
        //动DragView
        requestDragView((int) dx);
        postInvalidate();
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

    /**
     * 限制滚动dx不会超过临界值
     *
     * @param dx 输入dx值
     * @return -mPullWidth < dx < 0
     */
    float operateDx(float dx) {
        dx = Math.max(-mPullWidth, dx);
        dx = Math.min(dx, 0);
        return dx;
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

    /**
     * @return 如果抽屉未拉开返回false，否则返回true
     */
    private boolean isDragViewIsShow() {
        return mChildView != null && mChildView.getTranslationX() != 0;
    }

    public void setPullWidth(int pullWidth) {
        mPullWidth = pullWidth;
    }

    public void setDragCallBackWidth(int callBackWidth) {
        mDragCallBackWidth = callBackWidth;
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_HORIZONTAL) != 0;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        if (mScroller != null) {
            mScroller.startScroll(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
            postInvalidate();//通知UI线程的更新
        }
        return super.onNestedPreFling(target, velocityX, velocityY);
    }


    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        super.onNestedPreScroll(target, dx, dy, consumed);


        if (!canChildScrollRight()) {
            //右滑到边界，拉出DragView
            scrollTotalDx += -dx;
            scrollTotalDx = operateDx(scrollTotalDx);
            requestNestedView(scrollTotalDx);
        } else {
            //下面只有当拉出抽屉之后再往另外方向滑动时调用，用于将抽屉拉回去
            if (isDragViewIsShow()) {
                scrollTotalDx -= dx;
                scrollTotalDx = operateDx(scrollTotalDx);
                requestNestedView(scrollTotalDx);
                consumed[0] = dx;
            }
        }

    }

    @Override
    public void stopNestedScroll() {
        super.stopNestedScroll();
        scrollTotalDx = 0;
        if (isDragViewIsShow()) {
            doDragBackAnimation();
        }
    }

    public OnDragCallBack onDragCallBack;

    public void setOnDragCallBack(OnDragCallBack onDragCallBack) {
        this.onDragCallBack = onDragCallBack;
    }

    /**
     * 拉拽CallBack
     */
    public interface OnDragCallBack {

        void onDrag();

        void onRelease();
    }

}
