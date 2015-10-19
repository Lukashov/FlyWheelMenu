package com.cuctomviews.den.FlyWheelMenu;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
;
/**
 * Created by Den on 12.10.15.
 */
public class SectorFlyWheelView extends View {

    // Used for SDK < 11
    private float mRotation = 0;
    private Matrix mTransform = new Matrix();
    private PointF mPivot = new PointF();

    private int mColor;
    private float mSweepAngle;

    private SectorFlyWheelModel mSectorFlyWheelModel;

    RectF mBounds;
    private List<SectorFlyWheelModel> mData = new ArrayList<SectorFlyWheelModel>();
    private Paint mFlyWheelPaint;


    int [] icons = {
            R.drawable.bluetooth,
            R.drawable.call_transfer,
            R.drawable.callback,
            R.drawable.cellular_network,
            R.drawable.end_call,
            R.drawable.high_connection,
            R.drawable.missed_call,
            R.drawable.mms};

    private Drawable mDrawable;

    public SectorFlyWheelView(Context _context, List<SectorFlyWheelModel> _data) {
        super(_context);
        mData = _data;
        init();
    }

    public void init(){
        mFlyWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSectorFlyWheelModel = new SectorFlyWheelModel();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas _canvas) {
        super.onDraw(_canvas);

        if (Build.VERSION.SDK_INT < 11) {
            mTransform.set(_canvas.getMatrix());
            mTransform.preRotate(mRotation, mPivot.x, mPivot.y);
            _canvas.setMatrix(mTransform);
        }

        int width = getWidth();
        int height = getHeight();
        int radius;

        if (width > height) {
            radius = height / 2;
        } else {
            radius = width / 2;
        }

        createFillSector(_canvas, width, height, radius);
        createInnerRing(_canvas, width, height, radius);

        _canvas.save();
    }

    private void createFillSector(Canvas _canvas, int _width, int _height, int _radius) {
        drawSector(_canvas, _width, _height, _radius);
        drawIconAndLineForSector(_canvas, _radius);
        drawArcForSector(_canvas);
    }

    private void drawSector(Canvas _canvas, int _width, int _height, int _radius) {
        mBounds.set(_width / 2 - _radius + 5,
                _height / 2 - _radius + 5,
                _width / 2 + _radius - 5,
                _height / 2 + _radius - 5);

        for (SectorFlyWheelModel it : mData) {

            mFlyWheelPaint.setColor(it.mSectorColor);
            mFlyWheelPaint.setAntiAlias(true);
            mFlyWheelPaint.setStyle(Paint.Style.FILL);
            _canvas.drawArc(mBounds,
                    270f - it.mEndAngle + 360f / mData.size() / 2,
                    360f / mData.size(),
                    true, mFlyWheelPaint);
        }
    }

    private void drawArcForSector(Canvas _canvas) {
        mFlyWheelPaint.setColor(getResources().getColor(R.color.strokeColor));
        mFlyWheelPaint.setStrokeWidth(10);
        mFlyWheelPaint.setAntiAlias(true);
        mFlyWheelPaint.setStrokeCap(Paint.Cap.ROUND);
        mFlyWheelPaint.setStyle(Paint.Style.STROKE);
        _canvas.drawArc(mBounds, 0, 360, false, mFlyWheelPaint);
    }

    private void drawIconAndLineForSector(Canvas _canvas, int _radius) {

        float x = (float) (Math.cos(Math.toRadians(270 - 360f / mData.size() / 2))*(_radius - 10));
        float y = (float) (Math.sin(Math.toRadians(270 - 360f / mData.size() / 2))*(_radius - 10));

        SectorFlyWheelModel it;
        for (int i = 0 ; i <  mData.size() ; i ++) {

            mDrawable = ContextCompat.getDrawable(getContext(), icons[i]);
            int sizeImage = (_radius * 20) / 100;
            int positionLeft = (int) (mBounds.centerX() - sizeImage / 2);
            int positionTop = (int) (mBounds.centerY() - (_radius * 2 / 3) - sizeImage / 2);
            mDrawable.setBounds(positionLeft,
                    positionTop,
                    positionLeft + sizeImage,
                    positionTop + sizeImage);

            mDrawable.draw(_canvas);

            it = mData.get( mData.size() -1 - i);
            mFlyWheelPaint.setColor(it.mStrokeColor);
            mFlyWheelPaint.setStrokeWidth(5);

            _canvas.drawLine(mBounds.centerX(),
                    mBounds.centerY(),
                    mBounds.centerX() + x,
                    mBounds.centerY() + y, mFlyWheelPaint);

            _canvas.rotate(360f / mData.size(), mBounds.centerX(), mBounds.centerY());
        }
    }

    private void createInnerRing(Canvas _canvas, int _width, int _height, int _radius) {
        //Ring1
        mBounds.set(_width / 2 - _radius / 3,
                _height / 2 - _radius / 3,
                _width / 2 + _radius / 3,
                _height / 2 + _radius / 3);

        mFlyWheelPaint.setColor(getResources().getColor(R.color.pressedSectorColor));
        mFlyWheelPaint.setAntiAlias(true);
        mFlyWheelPaint.setStyle(Paint.Style.FILL);
        _canvas.drawArc(mBounds, 0, 360, false, mFlyWheelPaint);

        //Ring2
        mBounds.set(_width / 2 - _radius / 5,
                _height / 2 - _radius / 5,
                _width / 2 + _radius / 5,
                _height / 2 + _radius / 5);
        mFlyWheelPaint.setColor(getResources().getColor(R.color.strokeColor));
        mFlyWheelPaint.setAntiAlias(true);
        mFlyWheelPaint.setStyle(Paint.Style.FILL);
        _canvas.drawArc(mBounds, 0, 360, false, mFlyWheelPaint);
    }

    public void setColor(int _color) {
        mColor = _color;
    }

    @Override
    protected void onSizeChanged(int _w, int _h, int _oldw, int _oldh) {
        mBounds = new RectF(0, 0, _w, _h);
    }

    public void rotateTo(float _pieRotation) {
        mRotation = _pieRotation;
        if (Build.VERSION.SDK_INT >= 11) {
            setRotation(_pieRotation);
        } else {
            invalidate();
        }
    }

    public void setPivot(float _x, float _y) {
        mPivot.x = _x;
        mPivot.y = _y;
        if (Build.VERSION.SDK_INT >= 11) {
            setPivotX(_x);
            setPivotY(_y);
        } else {
            invalidate();
        }
    }


    public RectF getBounds() {
        return mBounds;
    }
}

