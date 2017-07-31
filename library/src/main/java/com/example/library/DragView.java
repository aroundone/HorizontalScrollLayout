package com.example.library;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by guoziliang on 2017/7/31.
 */

public class DragView extends View{

    private final float BEZIER = 0.552284749831f;

    private Paint paint;
    private Path path;
    public DragView(Context context) {
        this(context, null, 0);
    }

    public DragView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.parseColor("#bbbbbb"));
        paint.setStrokeWidth(1);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(1);
        path = new Path();
        initPoints();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawPath(canvas);
        
    }

    private void drawPath(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        initPoint(width, height);
        drawBezier();
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

    private void drawBezier() {
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

}
