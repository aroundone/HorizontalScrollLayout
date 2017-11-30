package com.example.library;

/**
 * Created by gzl on 17-11-30.
 * 拉拽CallBack
 */

public interface OnDragCallBack {

    /**
     * 跟随手指移动不停的调用，用来更新UI
     * @param outThreshold 是否超过临界值
     */
    void onDragging(boolean outThreshold);

    /**
     * 释放手指的调用，用来回应事件
     * @param outThreshold 是否超过临界值
     */
    void onRelease(boolean outThreshold);

    /**
     * 触发fling
     */
    void onFling();
}
