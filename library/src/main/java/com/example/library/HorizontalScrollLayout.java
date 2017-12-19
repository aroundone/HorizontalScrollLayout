package com.example.library;

import android.animation.Animator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;

/**
 * Created by guoziliang on 2017/7/29.
 * 只负责控制ChildView和DragView的运动，DragView的具体实现由DragView实现，这里只负责回调接口{@link #setOnDragCallBack(OnDragCallBack)}
 */

public class HorizontalScrollLayout extends FrameLayout implements NestedScrollingParent {

    private final String TAG = "HorizontalLayout";


    /**
     * 手指拖拽回滚动画时间
     */
    private int dragBackAniDuration = 500;

    /**
     * Fling动画时间
     */
    private int dragEndAniDuration = 300;

    /**
     * 是否开始做动画
     */
    private boolean isBackAniDoing;

    /**
     * 是否是加载更多模式，如果是此模式，拉出之后不会立刻回去，需要回调{@link #stopLoading()}方法结束
     */
    private boolean isLoadingType = false;

    /**
     * 是不是正在Loading
     */
    private boolean isLoading = false;

    /**
     * 可以拖拽出来的宽度
     */
    private float mPullWidth = 200;

    /**
     * 触发变色的宽度，临界值
     */
    private float thresholdWidth = 100;


    public static float outThresholdWidth;

    /**
     * NestScroll中检测手指滑动Dx之和，因为每次只会监听相对值移动，所以需要累加和重置
     */
    private float scrollTotalDx = 0;

    /**
     * 子View，一般都是RecyclerView
     */
    private View mChildView;

    /**
     * 拖拽View，类似于Header
     */
    private View dragView;

    /**
     * 监听滚动Scroller
     */
    private Scroller mScroller;

    /**
     * 回滚动画
     */
    private ValueAnimator backAni;

    /**
     * 拖拽回调
     */
    private OnDragCallBack onDragCallBack;

    /**
     * Loading回调
     */
    private OnLoadingCallback onLoadingCallback;

    private NestedScrollingParentHelper mNestedParentHelper;

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
        outThresholdWidth = thresholdWidth;
        setAttrs(attrs);
        DecelerateInterpolator interpolator = new DecelerateInterpolator();
        mScroller = new Scroller(context, interpolator);
        mNestedParentHelper = new NestedScrollingParentHelper(this);
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
     * @param child childView
     */
    public void addDragView(@NonNull View child) {
        super.addView(child);
        dragView = child;
    }

    /**
     * 执行抽屉拉出后回滚动画
     */
    private void doDragBackAnimationCheck() {
        if (isLoadingType && hasOutThresholdWidth(Math.abs(scrollTotalDx))) {
            isLoading = true;
            doDragBackAnimation(-thresholdWidth);
            if (onLoadingCallback != null) {
                onLoadingCallback.onStartLoading();
            }
            return;
        }
        isLoading = false;
        doDragBackAnimation(0);
    }

    /**
     * 执行抽屉拉出后回滚动画
     */
    private void doDragBackAnimation(float endX) {
        PropertyValuesHolder holder = PropertyValuesHolder.ofFloat(TAG, mChildView.getTranslationX(), endX);
        doBackAnimation(holder, dragBackAniDuration);
        resetScrollTotalDx(endX);
    }

    public void stopLoading() {
        if (isLoadingType) {
            isLoading = false;
            doDragBackAnimation(0);
            if (onLoadingCallback != null) {
                onLoadingCallback.onStopLoading();
            }
        }
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
     * Fling到最后的时候滚动出抽屉效果动效，此方法是执行拉出动效，待拉出结束之后会回调{@link #doBackAnimation(PropertyValuesHolder, int)}
     */
    private void doInertiaAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(0, -thresholdWidth);
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
                doDragBackAnimationCheck();
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
        if (mScroller != null && mScroller.computeScrollOffset()) {
            if (!canChildScrollRight()) {
                //抽屉未拉出的时候不做动画
                if (!isDragViewIsShow()) {
                    doInertiaAnimation();
                    if (onDragCallBack != null) {
                        onDragCallBack.onFling();
                    }
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
     * @param dx dx
     */
    private void requestNestedView(float dx) {
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
            if (hasOutThresholdWidth(Math.abs(dx))) {
                onDragCallBack.onDragging(true);
            } else {
                onDragCallBack.onDragging(false);
            }
        }
    }

    /**
     * 是否超出临界值
     * @param width 宽度
     * @return 是否超出临界值
     */
    private boolean hasOutThresholdWidth(float width) {
        return width > thresholdWidth;
    }

    /**
     * 限制滚动dx不会超过临界值
     *
     * @param dx 输入dx值
     * @return -mPullWidth < dx < 0
     */
    private float operateDx(float dx) {
        dx = Math.max(-mPullWidth, dx);
        dx = Math.min(dx, 0);
        return dx;
    }

    private void resetScrollTotalDx(float x) {
        scrollTotalDx = x;
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

    /**
     * 设置可以拖拽出来的宽度
     * @param pullWidth 可以拖拽出来的宽度
     */
    public void setPullWidth(int pullWidth) {
        mPullWidth = pullWidth;
    }

    /**
     * 设置临界值
     * @param thresholdWidth 临界值
     */
    public void setThresholdWidth(int thresholdWidth) {
        this.thresholdWidth = thresholdWidth;
    }

    /**
     * 设置Loading模式，是否开始Loading模式，Loading模式拉出后需要调用{@link #stopLoading()}方法才能结束调用
     * @param isLoadingType Loading模式
     */
    public void setLoadingType(boolean isLoadingType) {
        this.isLoadingType = isLoadingType;
    }

    /**
     * 设置回调
     * @param onDragCallBack 回调
     */
    public void setOnDragCallBack(OnDragCallBack onDragCallBack) {
        this.onDragCallBack = onDragCallBack;
    }

    public void setOnLoadingCallBack(OnLoadingCallback onLoadingCallback) {
        this.onLoadingCallback = onLoadingCallback;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        mNestedParentHelper.onNestedScrollAccepted(child, target, ViewCompat.SCROLL_AXIS_HORIZONTAL);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {

    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedParentHelper.getNestedScrollAxes();
    }

    @Override
    public void onStopNestedScroll(View child) {
        mNestedParentHelper.onStopNestedScroll(child);

        if (isDragViewIsShow()) {

            if (isLoading) {
                return;
            }
            if (onDragCallBack != null) {
                onDragCallBack.onRelease(hasOutThresholdWidth(Math.abs(scrollTotalDx)));
            }

            doDragBackAnimationCheck();
        }
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_HORIZONTAL) != 0;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        if (mScroller != null && velocityX > 0) {
            mScroller.startScroll(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
            postInvalidate();
        }
        return false;
    }



    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        if (isLoading) {
            return;
        }
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
}
