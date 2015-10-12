package com.cuctomviews.den.FlyWheelMenu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build;
import android.util.Log;
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

    private SectorFlyWheelModel mSectorFlyWheelModel;

    RectF mBounds;
    private List<SectorFlyWheelModel> mData = new ArrayList<SectorFlyWheelModel>();
    private Paint mPiePaint;


    int [] icons = {R.drawable.bluetooth,R.drawable.call_transfer,R.drawable.callback, R.drawable.cellular_network,
            R.drawable.end_call,R.drawable.high_connection, R.drawable.missed_call,R.drawable.mms};

    public SectorFlyWheelView(Context context, SectorFlyWheelModel sectorFlyWheelModel) {
        super(context);
        mSectorFlyWheelModel = sectorFlyWheelModel;
        init();
    }

    public void init(){
        mPiePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        mSectorFlyWheelModel = new SectorFlyWheelModel();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (Build.VERSION.SDK_INT < 11) {
            mTransform.set(canvas.getMatrix());
            mTransform.preRotate(mRotation, mPivot.x, mPivot.y);
            canvas.setMatrix(mTransform);
        }

        int width = getWidth();
        int height = getHeight();
        int radius;

        if (width > height) {
            radius = height / 2;
        } else {
            radius = width / 2;
        }

        float x = (float) (Math.cos(Math.toRadians(270-360f/mData.size()/2))*(radius-5));
        float y = (float) (Math.sin(Math.toRadians(270-360f/mData.size()/2))*(radius-5));

        for (SectorFlyWheelModel it : mData) {

            mBounds.set(width / 2 - radius + 5, height / 2 - radius + 5, width / 2 + radius - 5, height / 2 + radius - 5);

            //Fill Sector
            mPiePaint.setColor(it.mSliceColor);
            mPiePaint.setAntiAlias(true);
            mPiePaint.setStyle(Paint.Style.FILL);
            canvas.drawArc(mBounds,
                    360f - it.mEndAngle - (90 - 360f / mData.size()) - 360f / mData.size() / 2,
                    360f / mData.size(),
                    true, mPiePaint);
            Log.d("CANVAS: ", "End: " + 360f / mData.size() + " ,Start: " + (360f - it.mEndAngle - (90 - 360f / mData.size()) - 360f / mData.size() / 2));

            //Line
//                mPiePaint.setColor(it.mStrokeColor);
//                mPiePaint.setStrokeWidth(5);
//                mPiePaint.setAntiAlias(true);
//                mPiePaint.setStyle(Paint.Style.STROKE);
//                canvas.drawArc(mBounds,
//                        360f - it.mEndAngle - (90 - 360f/mData.size()) - 360f/mData.size()/2,
//                        360f/mData.size(),
//                        true, mPiePaint);
        }

//            TODO: заменить Битмап на дровабле
        SectorFlyWheelModel it;
        for (int i = 0 ; i <  mData.size() ; i ++) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), icons[i]);
            canvas.drawBitmap(bitmap, mBounds.centerX() - bitmap.getWidth() / 2,
                    mBounds.centerY() - (radius * 2 / 3) - bitmap.getHeight() / 2, mPiePaint);

            it = mData.get( mData.size() -1 - i);
            mPiePaint.setColor(it.mStrokeColor);
            mPiePaint.setStrokeWidth(5);
            canvas.drawLine(mBounds.centerX(), mBounds.centerY(), mBounds.centerX() + x, mBounds.centerY() + y, mPiePaint);

            canvas.rotate(360f/mData.size(), mBounds.centerX(), mBounds.centerY());
        }

        //Ring1
        mPiePaint.setColor(getResources().getColor(R.color.strokeColor));
        mPiePaint.setStrokeWidth(10);
        mPiePaint.setAntiAlias(true);
        mPiePaint.setStrokeCap(Paint.Cap.ROUND);
        mPiePaint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(mBounds, 0, 360, false, mPiePaint);

        //Ring2

        mBounds.set(width / 2 - radius / 3, height / 2 - radius / 3, width / 2 + radius / 3, height / 2 + radius / 3);

        mPiePaint.setColor(getResources().getColor(R.color.pressedSectorColor));
        mPiePaint.setAntiAlias(true);
        mPiePaint.setStyle(Paint.Style.FILL);
        canvas.drawArc(mBounds, 0, 360, false, mPiePaint);

        //Ring3
        mBounds.set(width / 2 - radius / 5, height / 2 - radius / 5, width / 2 + radius / 5, height / 2 + radius / 5);
        mPiePaint.setColor(getResources().getColor(R.color.strokeColor));
        mPiePaint.setAntiAlias(true);
        mPiePaint.setStyle(Paint.Style.FILL);
        canvas.drawArc(mBounds, 0, 360, false, mPiePaint);
//            mPiePaint.setColor(getResources().getColor(R.color.redShadowColor));
//            mPiePaint.setStrokeWidth(10);
//            mPiePaint.setAntiAlias(true);
//            mPiePaint.setStrokeCap(Paint.Cap.ROUND);
//            mPiePaint.setStyle(Paint.Style.STROKE);
//            canvas.drawArc(mBounds, 0, 360, false, mPiePaint);
//

        canvas.save();
    }

    public void setColor(int color) {
        mColor = color;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mBounds = new RectF(0, 0, w, h);
    }

    public void rotateTo(float pieRotation) {
        mRotation = pieRotation;
        if (Build.VERSION.SDK_INT >= 11) {
            setRotation(pieRotation);
        } else {
            invalidate();
        }
    }

    public void setPivot(float x, float y) {
        mPivot.x = x;
        mPivot.y = y;
        if (Build.VERSION.SDK_INT >= 11) {
            setPivotX(x);
            setPivotY(y);
        } else {
            invalidate();
        }
    }
}

