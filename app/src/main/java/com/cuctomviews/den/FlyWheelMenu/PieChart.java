package com.cuctomviews.den.FlyWheelMenu;

        import android.animation.Animator;
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

public class PieChart extends ViewGroup {

    private List<SectorFlyWheelModel> mData = new ArrayList<SectorFlyWheelModel>();

    private float mTotal = 0.0f;

    private RectF mPieBounds = new RectF();

    private Paint mPiePaint;

    private float mHighlightStrength = 1.15f;

    private int mPieRotation;

    private PieView mPieView;
    private SectorFlyWheelView mSectorFlyWheelView;
    private Scroller mScroller;
    private ValueAnimator mScrollAnimator;
    private GestureDetector mDetector;
    private float mDiameterMax;

    private int mCurrentItemAngle;

    private int mCurrentItem = 0;
    private boolean mAutoCenterInSlice;
    private ObjectAnimator mAutoCenterAnimator;

    public static final int FLING_VELOCITY_DOWNSCALE = 4;

    public static final int AUTOCENTER_ANIM_DURATION = 250;

    int [] icons = {R.drawable.bluetooth,R.drawable.call_transfer,R.drawable.callback, R.drawable.cellular_network,
            R.drawable.end_call,R.drawable.high_connection, R.drawable.missed_call,R.drawable.mms};

    private float mRadius;

    private SectorFlyWheelModel mSectorFlyWheelModel;

    public PieChart(Context context) {
        super(context);
        init();
    }

    public PieChart(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.PieChart,
                0, 0
        );

        try {
            mHighlightStrength = a.getFloat(R.styleable.PieChart_highlightStrength, 1.0f);
            mPieRotation = a.getInt(R.styleable.PieChart_pieRotation, 0);
            mAutoCenterInSlice = a.getBoolean(R.styleable.PieChart_autoCenterPointerInSlice, false);
        } finally {
            a.recycle();
        }

        init();
    }

    public int getPieRotation() {
        return mPieRotation;
    }

    public void setPieRotation(int rotation) {
        rotation = (rotation % 360 + 360) % 360;
        mPieRotation = rotation;
        mPieView.rotateTo(rotation);
    }

    public int addItem(float value, int sliceColor, int strokeColor) {
        mSectorFlyWheelModel = new SectorFlyWheelModel();
        mSectorFlyWheelModel.mSliceColor = sliceColor;
        mSectorFlyWheelModel.mStrokeColor = strokeColor;

        mSectorFlyWheelModel.mValue = value;

        mTotal += value;

        mData.add(mSectorFlyWheelModel);

        onDataChanged();

        return mData.size() - 1;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // Do nothing. Do not call the superclass method--that would start a layout pass
        // on this view's children. PieChart lays out its children in onSizeChanged().
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float xpad = (float) (getPaddingLeft() + getPaddingRight());
        float ypad = (float) (getPaddingTop() + getPaddingBottom());

        float ww = (float) w - xpad;
        float hh = (float) h - ypad;

        mDiameterMax = Math.min(ww, hh);
        mPieBounds = new RectF(
                0.0f,
                0.0f,
                mDiameterMax,
                mDiameterMax);
        mPieBounds.offsetTo(w/2-mDiameterMax/2, getPaddingTop());

        mPieView.layout((int) mPieBounds.left,
                (int) mPieBounds.top,
                (int) mPieBounds.right,
                (int) mPieBounds.bottom);
        mPieView.setPivot(mPieBounds.width() / 2, mPieBounds.height() / 2);

        onDataChanged();
    }

    private void onDataChanged() {
        float currentAngle = 0;
        for (SectorFlyWheelModel it : mData) {
            it.mStartAngle = currentAngle;
            it.mEndAngle = (currentAngle + it.mValue * 360.0f / mTotal);
            currentAngle = it.mEndAngle;
        }
    }

    private void init() {

        setLayerToSW(this);

        mPiePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPiePaint.setStyle(Paint.Style.FILL);

//        mSectorFlyWheelView = new SectorFlyWheelView(getContext(), mSectorFlyWheelModel);

        mPieView = new PieView(getContext());

        addView(mPieView);
        mPieView.rotateTo(mPieRotation);

        if (Build.VERSION.SDK_INT >= 11) {
            mAutoCenterAnimator = ObjectAnimator.ofInt(PieChart.this, "PieRotation", 0);

            mAutoCenterAnimator.addListener(new Animator.AnimatorListener() {
                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                }

                public void onAnimationCancel(Animator animator) {
                }

                public void onAnimationRepeat(Animator animator) {
                }
            });
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
    public boolean onTouchEvent(MotionEvent event) {

        if(pointInCircle(event)) {

            boolean result = mDetector.onTouchEvent(event);
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
            }
            if (!result) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    result = true;
                }
            }
            return result;
        }
        return super.onTouchEvent(event);
    }

    public double getAngle(float pointX, float pointY){

        double angle = Math.toDegrees(Math.atan2(mPieBounds.centerY() - pointY, mPieBounds.centerX() - pointX)) +90;
            if (angle < 0) {
                angle += 360;
            }
        return 360 - angle;
    }

    private void tickScrollAnimation() {
        if (!mScroller.isFinished()) {
            mScroller.computeScrollOffset();
            setPieRotation(mScroller.getCurrY());
        } else {
            if (Build.VERSION.SDK_INT >= 11) {
                mScrollAnimator.cancel();
            }
        }
    }

    private void setLayerToSW(View v) {
        if (!v.isInEditMode() && Build.VERSION.SDK_INT >= 11) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    private class PieView extends View {
        // Used for SDK < 11
        private float mRotation = 0;
        private Matrix mTransform = new Matrix();
        private PointF mPivot = new PointF();
        private int mColor;

        public PieView(Context context) {
            super(context);
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

        RectF mBounds;

//        @Override
//        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//
//            int measuredWidth = measureWidth(widthMeasureSpec);
//            mRadius = mDiameterMax/2;
//            if (mRadius == 0) {
//                mRadius = measuredWidth;
//            }
//            int measuredHeight = measureHeight(heightMeasureSpec);
//            if (measuredHeight < measuredWidth)
//                mRadius  = measuredHeight / 2;
//            mRadius -= 10;
//            setMeasuredDimension(measuredWidth, measuredHeight);
//        }
//
//        private int measureWidth(int measureSpec) {
//            int specMode = MeasureSpec.getMode(measureSpec);
//            int specSize = MeasureSpec.getSize(measureSpec);
//            int result = 0;
//            if (specMode == MeasureSpec.AT_MOST) {
//                result = specSize;
//            } else if (specMode == MeasureSpec.EXACTLY) {
//                result = specSize;
//            }
//            return result;
//        }
//
//        private int measureHeight(int measureSpec) {
//            int specMode = MeasureSpec.getMode(measureSpec);
//            int specSize = MeasureSpec.getSize(measureSpec);
//            int result = 0;
//            if (specMode == MeasureSpec.AT_MOST) {
//                result = (int) (mRadius * 2);
//            } else if (specMode == MeasureSpec.EXACTLY) {
//                result = specSize;
//            }
//            return result;
//        }

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

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {

//            Get pressed item
            float angle = (float) getAngle(e.getY(),e.getX()) ;

            float an = mPieRotation - (90-360f/mData.size()) - 360f/mData.size()/2;

            if (an<0) an += 360;

            for (int i = mData.size() -1 ; i >=0  ; i --) {
                an = an + (360f/mData.size());

                if (an > 360) {an = an - 360;}
                if ((an-(360f/mData.size())) > 0) {

                    if ((angle <= an) && (angle > (an - (360f / mData.size())))) {

                        moveToCenterCurrentItem(an);
                        setPressedColor(i);
                    }
                } else{
                    if ((angle < 360f) && (angle > (360 + an - 360f / mData.size()))) {

                        moveToCenterCurrentItem(an);
                        setPressedColor(i);

                    }else if (angle<=360 && angle > (360-(360f/mData.size()-an)) || angle <= an && an >= 0){

                        moveToCenterCurrentItem(an);
                        setPressedColor(i);
                    }
                }
            }

            return true;
        }

        public void moveToCenterCurrentItem(float angle){

            float targetAngle;

            if (angle < (360 / mData.size() / 2)){
                targetAngle = 360 + (angle - 360 / mData.size() / 2);
            } else{
                targetAngle = angle - 360 / mData.size() / 2;
            }

            if (targetAngle <= 270 && targetAngle > 90
                    || targetAngle > 270 && targetAngle <= 360) {
                targetAngle = mPieRotation + (270 - targetAngle);

            } else if(targetAngle <= 90 && targetAngle >= 0 ){
                targetAngle = mPieRotation - (targetAngle + 90);
            }

                if (Build.VERSION.SDK_INT >= 11) {
                    mAutoCenterAnimator.setIntValues((int) targetAngle);
                    mAutoCenterAnimator.setDuration(AUTOCENTER_ANIM_DURATION).start();
                } else {
                    mPieView.rotateTo(targetAngle);
                }

            invalidate();
        }

        public void setPressedColor(int i){
            SectorFlyWheelModel current;
            SectorFlyWheelModel it;

            Log.d("COLOR: ", " Item: " + i);

            for(int j = 0; j < mData.size(); j++){
                if (j == i){
                    current = mData.get(j);
                    current.mSliceColor = getResources().getColor(R.color.pressedSectorColor);
                    Log.d("COLOR1: ", " Item: " + (j));

                    if((j-1) < 0){
                        it = mData.get(mData.size() - 2);
                        current = mData.get(mData.size() - 1);
                        Log.d("COLOR2: ", " Item: " + (mData.size() - 1));

                    }else {
                        Log.d("COLORIN: ", " Item: " + (j));

                        current = mData.get(j-1);
                        Log.d("COLOR3: ", " Item: " + (j - 1));

                        if (j-2 < 0){
                            it = mData.get(mData.size()-1);
                            Log.d("COLOR4: ", " Item: " + (mData.size() - 1));

                        } else {
                            it = mData.get(j-2);
                            Log.d("COLOR5: ", " Item: " + (mData.size() - 2));

                        }
                    }
                    current.mStrokeColor = getResources().getColor(R.color.pressedStrokeColor);
                    it.mStrokeColor = getResources().getColor(R.color.pressedStrokeColor);
                }
                else {
                    current = mData.get(j);
                    current.mSliceColor = getResources().getColor(R.color.fillSector);

                    if((j-1)<0){
                        current = mData.get(mData.size()-1);
                        Log.d("COLOR6: ", " Item: " + (mData.size()-1));

                    }else {
                        current = mData.get(j-1);
                        Log.d("COLOR7: ", " Item: " + (j));
                    }
                    current.mStrokeColor = getResources().getColor(R.color.strokeColor);
                }
            }
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                float scrollTheta = vectorToScalarScroll(
                        distanceX,
                        distanceY,
                        e2.getX() - mPieBounds.centerX(),
                        e2.getY() - mPieBounds.centerY());
                setPieRotation(getPieRotation() - (int) scrollTheta / FLING_VELOCITY_DOWNSCALE);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                float scrollTheta = vectorToScalarScroll(
                        velocityX,
                        velocityY,
                        e2.getX() - mPieBounds.centerX(),
                        e2.getY() - mPieBounds.centerY());
                mScroller.fling(
                        0,
                        (int) getPieRotation(),
                        0,
                        (int) scrollTheta / FLING_VELOCITY_DOWNSCALE,
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
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }

    public boolean pointInCircle(MotionEvent e){
        if(Math.sqrt(Math.pow((e.getX() - mPieBounds.centerX()), 2) +
                Math.pow((e.getY() - mPieBounds.centerY()), 2))
                <= mDiameterMax/2){
            return true;
        }
        return false;
    }

    private static float vectorToScalarScroll(float dx, float dy, float x, float y) {
        float l = (float) Math.sqrt(dx * dx + dy * dy);

        float crossX = -y;
        float crossY = x;

        float dot = (crossX * dx + crossY * dy);
        float sign = Math.signum(dot);

        return l * sign;
    }
}

