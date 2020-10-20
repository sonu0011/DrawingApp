package com.sonu.drawingapp.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class PaintView extends View {
    private Bitmap btmBackground, btmView;
    private Paint paint = new Paint();
    private Path path = new Path();
    private int canvasBg, eraserSize, brushSize;
    private float mx, my;
    private Canvas canvas;
    private ArrayList<Bitmap> listAction = new ArrayList<>();
    public ArrayList<Bitmap> allActions = new ArrayList<>();
    private ArrayList<Bitmap> redo = new ArrayList<>();


    public PaintView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        eraserSize = brushSize = 12;
        canvasBg = Color.WHITE;
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(toPx(brushSize));
    }

    private float toPx(int sizeBrush) {
        return sizeBrush * (getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        btmBackground = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        btmView = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(btmView);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(canvasBg);
        canvas.drawBitmap(btmBackground, 0, 0, null);
        canvas.drawBitmap(btmView, 0, 0, null);
    }

    public void setCanvasBackground(int color) {
        canvasBg = color;
        invalidate();
    }

    public void setBrushSize(int size) {
        brushSize = size;
        paint.setStrokeWidth(brushSize);
    }

    public int getBrushSize() {
        return brushSize;
    }

    public void setBrushColor(int color) {
        paint.setColor(color);
    }

    public void setEraserSize(int size) {
        eraserSize = size;
        paint.setStrokeWidth(toPx(eraserSize));
    }

    public int getEraserSize() {
        return eraserSize;
    }


    public void enableEraser() {
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    public void disableEraser() {
        paint.setXfermode(null);
        paint.setShader(null);
        paint.setMaskFilter(null);
    }

    public void addLastAction(Bitmap bitmap) {
        listAction.add(bitmap);
        allActions.add(bitmap);
    }

    public void undo() {

        if (listAction.size() > 0) {

            redo.add(listAction.get(listAction.size() - 1));
            listAction.remove(listAction.size() - 1);
            if (allActions.size() > 0) {
                allActions.remove(allActions.size() - 1);
            }

            if (listAction.size() > 0) {
                btmView = listAction.get(listAction.size() - 1);

            } else {
                btmView = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);

            }

            canvas = new Canvas(btmView);

            invalidate();
        } else {
            allActions.clear();
            Toast.makeText(getContext(), "Nothing to undo", Toast.LENGTH_LONG).show();
        }
    }

    public void redo() {
        if (redo.size() > 0) {
            btmView = redo.get(redo.size() - 1);
            listAction.add(redo.get(redo.size() - 1));
            allActions.add(redo.get(redo.size() - 1));
            canvas = new Canvas(btmView);
            invalidate();
            redo.remove(redo.size() - 1);
        } else {
            allActions.clear();
            Toast.makeText(getContext(), "Nothing to redo", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                break;
            case MotionEvent.ACTION_UP:
                touchUp(x, y);
                addLastAction(getBitmap());
                break;
        }

        return true;
    }


    private void touchUp(float x, float y) {
        path.reset();
    }

    private void touchStart(float x, float y) {
        path.moveTo(x, y);
        mx = x;
        my = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mx);
        float dy = Math.abs(y - my);

        int DIFFERENCE_SPACE = 4;
        if (dx >= DIFFERENCE_SPACE || dy >= DIFFERENCE_SPACE) {
            path.quadTo(x, y, (x + mx) / 2, (y + my) / 2);
            mx = x;
            my = y;
            canvas.drawPath(path, paint);
            invalidate();

        }

    }

    public Bitmap getBitmap() {
        this.setDrawingCacheEnabled(true);
        this.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(this.getDrawingCache());
        this.setDrawingCacheEnabled(true);
        return bitmap;
    }

    public void clear() {
        listAction.clear();
        redo.clear();
        allActions.clear();
        canvasBg = Color.WHITE;
        btmView = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(btmView);
        invalidate();
    }
}
