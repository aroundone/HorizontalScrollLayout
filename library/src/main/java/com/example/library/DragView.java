package com.example.library;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Typeface;
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
 * 实现{@link com.example.library.HorizontalLayout.OnDragCallBack}。保证联动
 */

public class DragView extends View implements HorizontalLayout.OnDragCallBack {

    private final float BEZIER = 0.552284749831f;

    private Paint paint;
    private Path path;


    public static final int LAYOUT_CHANGED = 1;
    private Paint textPaint;
    private float mTextPosx = 0;// x坐标
    private float mTextPosy = 0;// y坐标
    private float mTextWidth = 0;// 绘制宽度
    private float mTextHeight = 0;// 绘制高度
    private float mFontHeight = 0;// 绘制字体高度
    private float mFontSize = 24;// 字体大小
    private float TextLength = 0 ;//字符串长度
    private String text="";//待显示的文字


    private String dragText = "大V推荐";
    private int dragTextColor = Color.BLACK;
    private String releaseText = "松开啦";
    private int releaseTextColor = Color.BLUE;


    public DragView(Context context) {
        this(context, null, 0);
    }

    public DragView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
        initTextPaint();
        initTextPaint();
        initPoints();
        path = new Path();
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

    private void initPaint() {
        paint = new Paint();
        paint.setColor(Color.parseColor("#bbbbbb"));
        paint.setStrokeWidth(1);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
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

    private void drawText(Canvas canvas, String thetext) {
        char ch;
        Paint.FontMetricsInt fontMetrics = textPaint.getFontMetricsInt();
        mTextPosx = (int) (canvas.getWidth() + ((textPaint.descent() + textPaint.ascent())));//初始化x坐标
        mTextPosy =(int) ((canvas.getHeight() - mTextHeight) / 2) - fontMetrics.top;//初始化y坐标
        for (int i = 0; i < this.TextLength; i++) {
            ch = thetext.charAt(i);
            canvas.drawText(String.valueOf(ch), mTextPosx, mTextPosy, textPaint);
            mTextPosy += mFontHeight;
        }
    }

    //计算文字行数和总宽
    private void GetTextInfo() {
        //获得字宽
        float[] widths = new float[1];
        textPaint.getTextWidths("正", widths);//获取单个汉字的宽度
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        mFontHeight = (int) Math.ceil(fm.descent - fm.top);// 获得字体高度
        mTextHeight = mFontHeight * text.length();
    }

    private void drawBezier(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        initPoint(width, height);
        drawBezierPath();
        canvas.drawPath(path, paint);
    }

    private PointF[] vertexPoints = new PointF[3];
    private PointF[] controlPoints = new PointF[4];
    private PointF centerPoint = new PointF();


    private void initPoints() {
        vertexPoints[0] = new PointF();
        vertexPoints[1] = new PointF();
        vertexPoints[2] = new PointF();
        controlPoints[0] = new PointF();
        controlPoints[1] = new PointF();
        controlPoints[2] = new PointF();
        controlPoints[3] = new PointF();
    }

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



    public final void setText(String text) {
        this.text=text;
        this.TextLength = text.length();
        GetTextInfo();
        requestLayout();
    }
    //设置字体大小
    public final void setTextSize(float size) {
        if (size != textPaint.getTextSize()) {
            mFontSize = size;
            GetTextInfo();
        }
    }
    //设置字体颜色
    public final void setTextColor(int color) {
        textPaint.setColor(color);
        requestLayout();
    }
    //设置字体颜色
    public final void setTextARGB(int a,int r,int g,int b) {
        textPaint.setARGB(a, r, g, b);
    }
    //设置字体
    public void setTypeface(Typeface tf) {
        if (this.textPaint.getTypeface() != tf) {
            this.textPaint.setTypeface(tf);
        }
    }
    //获取实际宽度
    public float getTextWidth() {
        return mTextWidth;
    }

    public void setDragText(String dragText) {
        this.dragText = dragText;
    }

    public void setDragTextColor(int dragTextColor) {
        this.dragTextColor = dragTextColor;
    }

    public void setReleaseText(String releaseText) {
        this.releaseText = releaseText;
    }

    public void setReleaseTextColor(int releaseTextColor) {
        this.releaseTextColor = releaseTextColor;
    }

    @Override
    public void onDrag() {
        setText(releaseText);
        setTextColor(releaseTextColor);
    }

    @Override
    public void onRelease() {
        setText(dragText);
        setTextColor(dragTextColor);
    }
}
