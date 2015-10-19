package com.cuctomviews.den.FlyWheelMenu;

        import android.animation.ObjectAnimator;
        import android.animation.ValueAnimator;
        import android.content.Context;
        import android.content.res.TypedArray;
        import android.graphics.*;
        import android.os.Build;
        import android.util.AttributeSet;
        import android.util.Log;
        import android.view.*;
        import android.widget.Scroller;

        import java.lang.Math;
        import java.lang.Override;
        import java.util.ArrayList;
        import java.util.List;

public class FlyWheelMenu extends ViewGroup {

    private List<SectorFlyWheelModel> mData = new ArrayList<SectorFlyWheelModel>();

    private float mTotal;

    private RectF mFlyWheelBounds = new RectF();

    private Paint mFlyWheelPaint;

    private int mFlyWheelRotation;

    private SectorFlyWheelView mSectorFlyWheelView;
    private Scroller mScroller;
    private ValueAnimator mScrollAnimator;
    private GestureDetector mDetector;
    private float mDiameterMax;

    private ObjectAnimator mAutoCenterAnimator;

    public static final int FLING_VELOCITY_DOWNSCALE = 4;

    public static final int AUTOCENTER_ANIM_DURATION = 250;

    private SectorFlyWheelModel mSectorFlyWheelModel;

    private int mWidth;
    private int mHeight;

    public FlyWheelMenu(Context _context) {
        super(_context);
        init();
    }

    public FlyWheelMenu(Context _context, AttributeSet _attrs) {
        super(_context, _attrs);

        TypedArray a = _context.getTheme().
                obtainStyledAttributes(_attrs, R.styleable.FlyWheelMenu, 0, 0);

        try {
            mFlyWheelRotation = a.getInt(R.styleable.FlyWheelMenu_flyWheelRotation, 0);
        } finally {
            a.recycle();
        }

        init();
    }

    public int getFlyWheelRotation() {
        return mFlyWheelRotation;
    }

    public void setFlyWheelRotation(int rotation) {
        rotation = (int) ((rotation % 360f + 360f) % 360f);
        mFlyWheelRotation = rotation;
        mSectorFlyWheelView.rotateTo(rotation);
    }

    public int addItem(float _value, int _sliceColor, int _strokeColor) {
        Log.d("START:", " addItem!");

        mSectorFlyWheelModel = new SectorFlyWheelModel();
        mSectorFlyWheelModel.mSectorColor = _sliceColor;
        mSectorFlyWheelModel.mStrokeColor = _strokeColor;

        mSectorFlyWheelModel.mValue = _value;

        mData.add(mSectorFlyWheelModel);

        mTotal += _value;

        onDataChanged();

        return mData.size() - 1;
    }

    @Override
    protected void onLayout(boolean _changed, int _l, int _t, int _r, int _b) {
    }

    @Override
    protected void onMeasure(int _widthMeasureSpec, int _heightMeasureSpec) {
        Log.d("START:", " onMeasure!");

        int desiredWidth = 200;
        int desiredHeight = 200;

        int widthMode = MeasureSpec.getMode(_widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(_widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(_heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(_heightMeasureSpec);


//   Pixels to DP
//        Resources resources = getContext().getResources();
//        DisplayMetrics metrics = resources.getDisplayMetrics();
//        float dpWidth = widthSize / (metrics.densityDpi / 160f);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(desiredWidth, widthSize);
            } else {
                width = desiredWidth;
            }
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(desiredHeight, heightSize);
            } else {
                height = desiredHeight;
            }
        }

        mHeight = height;
        mWidth = width;

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int _w, int _h, int _oldw, int _oldh) {
    super.onSizeChanged(_w, _h, _oldw, _oldh);

        Log.d("START:", " onSizeChanged!");

        float xpad = (float) (getPaddingLeft() + getPaddingRight());
        float ypad = (float) (getPaddingTop() + getPaddingBottom());

        float ww = (float) _w - xpad;
        float hh = (float) _h - ypad;

        mDiameterMax = Math.min(ww, hh);
        mFlyWheelBounds = new RectF(0, 0, mDiameterMax, mDiameterMax);
        mFlyWheelBounds.offsetTo(_w / 2 - mDiameterMax / 2, getPaddingTop());

        mSectorFlyWheelView.layout((int) mFlyWheelBounds.left,
                (int) mFlyWheelBounds.top,
                (int) mFlyWheelBounds.right,
                (int) mFlyWheelBounds.bottom);
        mSectorFlyWheelView.setPivot(mFlyWheelBounds.width() / 2, mFlyWheelBounds.height() / 2);

    onDataChanged();
    }

    private void onDataChanged() {
        float currentAngle = 0;
        for (SectorFlyWheelModel it : mData) {
            it.mStartAngle = currentAngle;
            it.mEndAngle = (currentAngle + it.mValue * 360f / mTotal);
            currentAngle = it.mEndAngle;
        }
    }

    private void init() {
        Log.d("START:", " init!");

        setLayerToSW(this);

        mFlyWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFlyWheelPaint.setStyle(Paint.Style.FILL);

        mSectorFlyWheelView = new SectorFlyWheelView(getContext(), mData);

        addView(mSectorFlyWheelView);
        mSectorFlyWheelView.rotateTo(mFlyWheelRotation);

        if (Build.VERSION.SDK_INT >= 11) {
            mAutoCenterAnimator = ObjectAnimator.ofInt(FlyWheelMenu.this, "FlyWheelRotation", 0);
        }

        if (Build.VERSION.SDK_INT < 11) {
            mScroller = new Scroller(getContext());
        } else {
            mScroller = new Scroller(getContext(), null, true);
        }
        if (Build.VERSION.SDK_INT >= 11) {
            mScrollAnimator = ValueAnimator.ofFloat(0, 1);
            mScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    tickScrollAnimation();
                }
            });
        }

        mDetector = new GestureDetector(getContext(), new GestureListener());
        mDetector.setIsLongpressEnabled(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent _event) {

        if(pointInCircle(_event)) {

            boolean result = mDetector.onTouchEvent(_event);
            if (_event.getAction() == MotionEvent.ACTION_DOWN) {
            }
            if (!result) {
                if (_event.getAction() == MotionEvent.ACTION_UP) {
                    result = true;
                }
            }
            return result;
        }
        return super.onTouchEvent(_event);
    }

    public double getAngle(float _pointX, float _pointY){

        double angle = Math.toDegrees(Math.atan2(mSectorFlyWheelView.getBounds().centerY() - _pointY,
                mSectorFlyWheelView.getBounds().centerX() - _pointX)) +90;

        if (angle < 0) {
                angle += 360f;
            }
        return 360f - angle;
    }

    private void tickScrollAnimation() {
        if (!mScroller.isFinished()) {
            mScroller.computeScrollOffset();
            setFlyWheelRotation(mScroller.getCurrY());
        } else {
            if (Build.VERSION.SDK_INT >= 11) {
                mScrollAnimator.cancel();
            }
        }
    }

    private void setLayerToSW(View _v) {
        if (!_v.isInEditMode() && Build.VERSION.SDK_INT >= 11) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent _e) {

//            Get pressed item and his angle
            float angle = (float) getAngle(_e.getY(), _e.getX()) ;

            float an = mFlyWheelRotation - (90-360f/mData.size()) - 360f/mData.size()/2;

            if (an<0) an += 360f;

            for (int i = mData.size() -1 ; i >=0  ; i --) {
                an = an + (360f/mData.size());

                if (an > 360f) {an = an - 360f;}

                if ((an-(360f/mData.size())) > 0) {

                    if ((angle <= an) && (angle > (an - (360f / mData.size())))) {

                        moveToCenterCurrentItem(an);
                        setPressedColor(i);
                    }
                } else{
                    if ((angle < 360f) && (angle > (360f + an - 360f / mData.size()))) {

                        moveToCenterCurrentItem(an);
                        setPressedColor(i);

                    }else if (angle<=360f && angle > (360f-(360f/mData.size()-an))
                            || angle <= an && an >= 0){

                        moveToCenterCurrentItem(an);
                        setPressedColor(i);
                    }
                }
            }

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent _e1, MotionEvent _e2, float _distanceX, float _distanceY) {
            float scrollTheta = vectorToScalarScroll(
                    _distanceX,
                    _distanceY,
                    _e2.getX() - mWidth/2,
                    _e2.getY() - mHeight/2);
                setFlyWheelRotation(getFlyWheelRotation() - (int) scrollTheta / FLING_VELOCITY_DOWNSCALE);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent _e1, MotionEvent _e2, float _velocityX, float _velocityY) {

            float scrollTheta = vectorToScalarScroll(
                    _velocityX,
                    _velocityY,
                    _e2.getX() - mWidth/2,
                    _e2.getY() - mHeight/2);

                mScroller.fling(0, getFlyWheelRotation(), 0, (int) scrollTheta / FLING_VELOCITY_DOWNSCALE,
                        0,
                        0,
                        Integer.MIN_VALUE,
                        Integer.MAX_VALUE);

                if (Build.VERSION.SDK_INT >= 11) {
                    mScrollAnimator.setDuration(mScroller.getDuration());
                    mScrollAnimator.start();
                }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent _e) {
            return true;
        }
    }

    public boolean pointInCircle(MotionEvent _e){
        if(Math.sqrt(Math.pow((_e.getX() - mSectorFlyWheelView.getBounds().centerX()), 2) +
                Math.pow((_e.getY() - mSectorFlyWheelView.getBounds().centerY()), 2))
                <= Math.min(mWidth,mHeight)/2){
            return true;
        }
        return false;
    }

    private static float vectorToScalarScroll(float _dx, float _dy, float _x, float _y) {
        float l = (float) Math.sqrt(_dx * _dx + _dy * _dy);

        float crossX = -_y;
        float crossY = _x;

        float dot = (crossX * _dx + crossY * _dy);
        float sign = Math.signum(dot);

        return l * sign;
    }

    public void moveToCenterCurrentItem(float _angle){

        float targetAngle;
        float halfSweepAngle = 360f / mData.size() / 2;

        if (_angle < (halfSweepAngle)){
            targetAngle = 360f + (_angle - halfSweepAngle);
        } else{
            targetAngle = _angle - halfSweepAngle;
        }

        if (targetAngle <= 270 && targetAngle > 90
                || targetAngle > 270 && targetAngle <= 360f) {
            targetAngle = mFlyWheelRotation + (270 - targetAngle);

        } else if(targetAngle <= 90 && targetAngle >= 0 ){
            targetAngle = mFlyWheelRotation - (targetAngle + 90);
        }

        if (Build.VERSION.SDK_INT >= 11) {
            mAutoCenterAnimator.setIntValues((int) targetAngle);
            mAutoCenterAnimator.setDuration(AUTOCENTER_ANIM_DURATION).start();
        } else {
            mSectorFlyWheelView.rotateTo(targetAngle);
        }

        invalidate();
    }

    public void setPressedColor(int _i){

        SectorFlyWheelModel current;
        SectorFlyWheelModel it;

        Log.d("Touch:", " Touch: " + _i);
        for(int j = 0; j < mData.size(); j++){
            if (j == _i){
                current = mData.get(j);
                current.mSectorColor = getResources().getColor(R.color.pressedSectorColor);

                if((j-1) < 0){
                    it = mData.get(mData.size() - 2);
                    current = mData.get(mData.size() - 1);

                }else {

                    current = mData.get(j-1);

                    if (j-2 < 0){
                        it = mData.get(mData.size()-1);

                    } else {
                        it = mData.get(j-2);

                    }
                }
                current.mStrokeColor = getResources().getColor(R.color.pressedStrokeColor);
                it.mStrokeColor = getResources().getColor(R.color.pressedStrokeColor);
            }
            else {
                current = mData.get(j);
                current.mSectorColor = getResources().getColor(R.color.fillSector);

                if((j-1)<0){
                    current = mData.get(mData.size()-1);

                }else {
                    current = mData.get(j-1);
                }
                current.mStrokeColor = getResources().getColor(R.color.strokeColor);
            }
        }
    }
}

