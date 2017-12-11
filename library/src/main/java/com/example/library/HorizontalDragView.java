package com.example.library;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by guoziliang on 2017/7/31.
 *
 * 实现{@link OnDragCallBack}。保证联动
 */

public class HorizontalDragView extends View implements OnDragCallBack, OnLoadingCallback {

    private final float BEZIER = 0.552284749831f;

    private Paint bezierPaint;
    private int bezierPaintColor;
    private Path path;

    private float mTextPosx = 0;// x坐标
    private float mTextPosy = 0;// y坐标
    private float mTextHeight = 0;// 绘制高度
    private float mFontHeight = 0;// 绘制字体高度
    private float TextLength = 0 ;//字符串长度
    private Paint textPaint;
    private String text="";//待显示的文字


    private String outThresholdText;
    private int outThresholdColor;
    private String inThresholdText;
    private int inThresholdColor;

    private PointF[] vertexPoints = new PointF[3];
    private PointF[] controlPoints = new PointF[4];
    private PointF centerPoint = new PointF();

    public HorizontalDragView(Context context) {
        this(context, null, 0);
    }

    public HorizontalDragView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalDragView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttributes();
        initBezierPaint();
        initTextPaint();
        initPointsArray();
        path = new Path();
    }

    private void initAttributes() {
        bezierPaintColor = Color.parseColor("#dddddd");
        inThresholdText = "相关推荐";
        outThresholdText = "松开啦";
        inThresholdColor = Color.parseColor("#888888");
        outThresholdColor = Color.parseColor("#04da00");
    }

    private void initTextPaint() {
        textPaint = new Paint();
        textPaint.setStrokeWidth(4);
        textPaint.setTextSize(35);
        textPaint.setColor(Color.BLACK);
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void initBezierPaint() {
        bezierPaint = new Paint();
        bezierPaint.setColor(bezierPaintColor);
        bezierPaint.setStrokeWidth(1);
        bezierPaint.setAntiAlias(true);
        bezierPaint.setStyle(Paint.Style.FILL);
    }

    private void initPointsArray() {
        vertexPoints[0] = new PointF();
        vertexPoints[1] = new PointF();
        vertexPoints[2] = new PointF();
        controlPoints[0] = new PointF();
        controlPoints[1] = new PointF();
        controlPoints[2] = new PointF();
        controlPoints[3] = new PointF();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.RIGHT;
        setLayoutParams(params);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBezier(canvas);
        drawText(canvas, this.text);
    }

    private void doDragCallBack(float dx) {

        if ((Math.abs(dx)) > HorizontalScrollLayout.outThresholdWidth) {
            onDragging(true);
        } else {
            onDragging(false);
        }

    }

    private void drawBezier(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        doDragCallBack(width);
        initPoint(width, height);
        drawBezierPath();
        canvas.drawPath(path, bezierPaint);
    }

    /**
     * 计算贝塞尔曲线顶点和控制点
     */
    private void initPoint(int width, int height) {
        float halfHeight = height / 2;
        float halfWidth = width / 2;
        float bezierWidth = width * BEZIER;
        float bezierHeight = height * BEZIER;
        float bezierHalfHeight = halfHeight * BEZIER;

        centerPoint.set(width, halfHeight);
        vertexPoints[0].set(width, 0);
        vertexPoints[1].set(0, halfHeight);
        vertexPoints[2].set(width, height);

        controlPoints[0].set(width - bezierWidth, 0);
        controlPoints[1].set(0, halfHeight - bezierHalfHeight);
        controlPoints[2].set(0, halfHeight + bezierHalfHeight);
        controlPoints[3].set(width - bezierWidth, height);
    }

    /**
     * 绘制贝塞尔曲线
     */
    private void drawBezierPath() {
        path.reset();
        path.moveTo(vertexPoints[0].x, vertexPoints[0].y);

        path.cubicTo(controlPoints[0].x, controlPoints[0].y,
                controlPoints[1].x, controlPoints[1].y,
                vertexPoints[1].x, vertexPoints[1].y);

        path.cubicTo(controlPoints[2].x, controlPoints[2].y,
                controlPoints[3].x, controlPoints[3].y,
                vertexPoints[2].x, vertexPoints[2].y);

        path.close();
    }

    private void drawText(Canvas canvas, String text) {
        char ch;
        Paint.FontMetricsInt fontMetrics = textPaint.getFontMetricsInt();
        mTextPosx = (int) (canvas.getWidth() + ((textPaint.descent() + textPaint.ascent())));//初始化x坐标
        mTextPosy =(int) ((canvas.getHeight() - mTextHeight) / 2) - fontMetrics.top;//初始化y坐标
        for (int i = 0; i < this.TextLength; i++) {
            ch = text.charAt(i);
            canvas.drawText(String.valueOf(ch), mTextPosx, mTextPosy, textPaint);
            mTextPosy += mFontHeight;
        }
    }

    /**
     * 获取单个文字高度
     */
    private void GetTextInfo() {
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        mFontHeight = (int) Math.ceil(fm.descent - fm.top);// 获得字体高度
        mTextHeight = mFontHeight * text.length();
    }

    private void setText(String text) {
        this.text=text;
        this.TextLength = text.length();
        GetTextInfo();
        requestLayout();
    }

    //设置字体大小
    public final void setTextSize(float size) {
        if (size != textPaint.getTextSize()) {
            GetTextInfo();
        }
    }

    //设置字体颜色
    private void setTextColor(int color) {
        if (textPaint != null) {
            textPaint.setColor(color);
            requestLayout();
        }
    }

    public void setInThresholdText(String inThresholdText) {
        this.inThresholdText = inThresholdText;
    }

    public void setInThresholdColor(@ColorInt int inThresholdColor) {
        this.inThresholdColor = inThresholdColor;
    }

    public void setOutThresholdText(String outThresholdText) {
        this.outThresholdText = outThresholdText;
    }

    public void setOutThresholdColor(@ColorInt int outThresholdColor) {
        this.outThresholdColor = outThresholdColor;
    }

    public void setBezierPaintColor(@ColorInt int bezierPaintColor) {
        this.bezierPaintColor = bezierPaintColor;
    }

    @Override
    public void onDragging(boolean outThreshold) {
        if (outThreshold) {
            setText(outThresholdText);
            setTextColor(outThresholdColor);
        } else {
            setText(inThresholdText);
            setTextColor(inThresholdColor);
        }
    }

    @Override
    public void onRelease(boolean outThreshold) {

    }

    @Override
    public void onFling() {

    }

    @Override
    public void onStartLoading() {

    }

    @Override
    public void onStopLoading() {

    }
}
