package com.cuctomviews.den.FlyWheelMenu.charting;

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

        import com.cuctomviews.den.FlyWheelMenu.R;

        import java.lang.Math;
        import java.lang.Override;
        import java.util.ArrayList;
        import java.util.List;

public class PieChart extends ViewGroup {
    private List<Item> mData = new ArrayList<Item>();

    private float mTotal = 0.0f;

    private RectF mPieBounds = new RectF();

    private Paint mPiePaint;

    private float mHighlightStrength = 1.15f;

    private int mPieRotation;

    private OnCurrentItemChangedListener mCurrentItemChangedListener = null;

    private PieView mPieView;
    private Scroller mScroller;
    private ValueAnimator mScrollAnimator;
    private GestureDetector mDetector;
    private float mDiameterMax;

    private int mCurrentItemAngle;

    // the index of the current item.
    private int mCurrentItem = 0;
    private boolean mAutoCenterInSlice;
    private ObjectAnimator mAutoCenterAnimator;

    public static final int FLING_VELOCITY_DOWNSCALE = 4;

    public static final int AUTOCENTER_ANIM_DURATION = 250;

    int [] icons = {R.mipmap.ic_launcher};

    public interface OnCurrentItemChangedListener {
        void OnCurrentItemChanged(PieChart source, int currentItem);
    }

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

        calcCurrentItem();
    }

    public int getCurrentItem() {
        return mCurrentItem;
    }

    public void setCurrentItem(int currentItem) {
        setCurrentItem(currentItem, true);
    }

    private void setCurrentItem(int currentItem, boolean scrollIntoView) {
        mCurrentItem = currentItem;
        if (mCurrentItemChangedListener != null) {
            mCurrentItemChangedListener.OnCurrentItemChanged(this, currentItem);
        }
        if (scrollIntoView) {
            centerOnCurrentItem();
        }
        invalidate();
    }

    public void setOnCurrentItemChangedListener(OnCurrentItemChangedListener listener) {
        mCurrentItemChangedListener = listener;
    }

    public int addItem(float value, int sliceColor, int strokeColor) {
        Item it = new Item();
        it.mSliceColor = sliceColor;
        it.mStrokeColor = strokeColor;

        it.mValue = value;

        mTotal += value;

        mData.add(it);

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



        mDiameterMax = Math.min(ww, hh);//TODO: использовать входящее значение диаметра из атрибутов
        mPieBounds = new RectF(
                0.0f,
                0.0f,
                mDiameterMax,
                mDiameterMax);
        mPieBounds.offsetTo(getPaddingLeft(), getPaddingTop());

        mPieView.layout((int) mPieBounds.left,
                (int) mPieBounds.top,
                (int) mPieBounds.right,
                (int) mPieBounds.bottom);
        mPieView.setPivot(mPieBounds.width() / 2, mPieBounds.height() / 2);

        onDataChanged();
    }

    private void calcCurrentItem() {
        int pointerAngle = (mCurrentItemAngle + 360 + mPieRotation) % 360;
        for (int i = 0; i < mData.size(); ++i) {
            Item it = mData.get(i);
            if (it.mStartAngle <= pointerAngle && pointerAngle <= it.mEndAngle) {
                if (i != mCurrentItem) {
                    setCurrentItem(i, false);
                }
                break;
            }
        }
    }

    private void onDataChanged() {
        int currentAngle = 0;
        for (Item it : mData) {
            it.mStartAngle = currentAngle;
            it.mEndAngle = (int) ((float) currentAngle + it.mValue * 360.0f / mTotal);
            currentAngle = it.mEndAngle;
        }
        calcCurrentItem();
    }

    private void init() {
        setLayerToSW(this);

        mPiePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPiePaint.setStyle(Paint.Style.FILL);

        mPieView = new PieView(getContext());
        addView(mPieView);
        mPieView.rotateTo(mPieRotation);

        if (Build.VERSION.SDK_INT >= 11) {
            mAutoCenterAnimator = ObjectAnimator.ofInt(PieChart.this, "PieRotation", 0);

            mAutoCenterAnimator.addListener(new Animator.AnimatorListener() {
                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    mPieView.decelerate();
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
                Log.d("COORD: ", "COORDX: " + event.getX() + " ,COORDY: " + event.getY()
                        + " Pos: " + getCurrentItem() + " ,In: " + mPieRotation);
            }
            if (!result) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    result = true;
                }
            }
            return result;
        }
        return true;
    }

    public double getAngle(float pointX, float pointY){

        double angle = Math.toDegrees(Math.atan2(mPieBounds.centerY() - pointY, mPieBounds.centerX() - pointX)) +90;
            if (angle < 0) {
                angle += 360;
            }

        Log.d("COS: ","angle: "+ (360 - angle) + " coord: " + pointY + " , x: " + pointX);

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

    private void setLayerToHW(View v) {
        if (!v.isInEditMode() && Build.VERSION.SDK_INT >= 11) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
    }

    private void centerOnCurrentItem() {
        Item current = mData.get(getCurrentItem());
        int targetAngle = current.mStartAngle + (current.mEndAngle - current.mStartAngle)/2;
        targetAngle -= mCurrentItemAngle;

        Log.d("DEBUG: ", ", "+ targetAngle + " , " + mPieRotation + " , " + current.mStartAngle + " , " + current.mEndAngle);
        if (targetAngle < 90 && mPieRotation > 180) {
            targetAngle += 270;
        }else{
            targetAngle -= 90;
        }

        if (Build.VERSION.SDK_INT >= 11) {
            // Fancy animated version
            mAutoCenterAnimator.setIntValues(targetAngle);
            mAutoCenterAnimator.setDuration(AUTOCENTER_ANIM_DURATION).start();
        } else {
            // Dull non-animated version
            mPieView.rotateTo(targetAngle);
        }
    }

    /**
     * Internal child class that draws the pie chart onto a separate hardware layer
     * when necessary.
     */
    private class PieView extends View {
        // Used for SDK < 11
        private float mRotation = 0;
        private Matrix mTransform = new Matrix();
        private PointF mPivot = new PointF();
        private int mColor;

        /**
         * Construct a PieView
         *
         * @param context
         */
        public PieView(Context context) {
            super(context);

        }

        /**
         * Enable hardware acceleration (consumes memory)
         */
        public void accelerate() {
            setLayerToHW(this);
        }

        /**
         * Disable hardware acceleration (releases memory)
         */
        public void decelerate() {
            setLayerToSW(this);
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

            for (Item it : mData) {

                mBounds.set(width / 2 - radius + 5, height / 2 - radius + 5, width / 2 + radius - 5, height / 2 + radius - 5);

                //Fill Sector
                mPiePaint.setColor(it.mSliceColor);
                mPiePaint.setAntiAlias(true);
                mPiePaint.setStyle(Paint.Style.FILL);
                canvas.drawArc(mBounds,
                        360 - it.mEndAngle,
                        it.mEndAngle - it.mStartAngle,
                        true, mPiePaint);
                Log.d("CANVAS: ", "End: " + it.mEndAngle + " ,Start: " + it.mStartAngle);

                //Line
                mPiePaint.setColor(it.mStrokeColor);
                mPiePaint.setStrokeWidth(5);
                mPiePaint.setAntiAlias(true);
                mPiePaint.setStyle(Paint.Style.STROKE);
                canvas.drawArc(mBounds,
                        360 - it.mEndAngle,
                        it.mEndAngle - it.mStartAngle,
                        true, mPiePaint);

            }

            for (int i = 0; i < 5; i++) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), icons[0]);
                canvas.drawBitmap(bitmap, mBounds.centerX() - bitmap.getWidth() / 2,
                        mBounds.centerY() - (radius * 2 / 3) - bitmap.getHeight() / 2, mPiePaint);
                canvas.rotate(72, mBounds.centerX(), mBounds.centerY());
            }


            //Ring1
            mPiePaint.setColor(getResources().getColor(R.color.strokeColor));
            mPiePaint.setStrokeWidth(10);
            mPiePaint.setAntiAlias(true);
            mPiePaint.setStrokeCap(Paint.Cap.ROUND);
            mPiePaint.setStyle(Paint.Style.STROKE);
            canvas.drawArc(mBounds, 0, 360, false, mPiePaint);


            mBounds.set(width / 2 - radius / 4, height / 2 - radius / 4, width / 2 + radius / 4, height / 2 + radius / 4);
            //Ring3
            mPiePaint.setColor(getResources().getColor(R.color.strokeColor));
            mPiePaint.setAntiAlias(true);
            mPiePaint.setStyle(Paint.Style.FILL);
            canvas.drawArc(mBounds, 0, 360, false, mPiePaint);

            //Ring2
            mPiePaint.setColor(getResources().getColor(R.color.pressedColor));
            mPiePaint.setStrokeWidth(20);
            mPiePaint.setAntiAlias(true);
            mPiePaint.setStrokeCap(Paint.Cap.ROUND);
            mPiePaint.setStyle(Paint.Style.STROKE);
            canvas.drawArc(mBounds, 0, 360, false, mPiePaint);

            canvas.save();
        }

        public void setColor(int color) {
            mColor = color;
        }

        public void createSector(Canvas canvas, Item it, int radius){
            for (int i = 0; i < icons.length; i++) {

                mPiePaint.setColor(it.mSliceColor);
                mPiePaint.setAntiAlias(true);
                mPiePaint.setStyle(Paint.Style.FILL);

                canvas.drawArc(mBounds, 270 - (it.mEndAngle - it.mStartAngle) / 2,
                        it.mEndAngle - it.mStartAngle, true, mPiePaint);

                mPiePaint.setColor(it.mStrokeColor);
                mPiePaint.setStrokeWidth(5);
                mPiePaint.setAntiAlias(true);
                mPiePaint.setStyle(Paint.Style.STROKE);

                canvas.drawArc(mBounds, 270 - (it.mEndAngle - it.mStartAngle) / 2,
                        it.mEndAngle - it.mStartAngle, true, mPiePaint);

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), icons[i]);
                canvas.drawBitmap(bitmap, mBounds.centerX() - bitmap.getWidth() / 2,
                        mBounds.centerY() - (radius * 2 / 3) - bitmap.getHeight() / 2, mPiePaint);

                canvas.rotate(it.mEndAngle - it.mStartAngle, mBounds.centerX(), mBounds.centerY());

            }

        }


        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            mBounds = new RectF(0, 0, w, h);
        }

        RectF mBounds;

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

    /**
     * Maintains the state for a data item.
     */
    private class Item {
        public float mValue;
        public int mSliceColor;
        public int mStrokeColor;

        // computed values
        public int mStartAngle;
        public int mEndAngle;
    }

    /**
     * Extends {@link GestureDetector.SimpleOnGestureListener} to provide custom gesture
     * processing.
     */
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {


//            Get pressed item
            double angle = getAngle(e.getY(),e.getX());

            double an = mPieRotation ;

            for (int i = mData.size()-1; i >=0 ; i --) {
                an = an + (360/mData.size());
                if(an > 360 && (an-(360/mData.size())) > 0) {
                    an = an - 360;
                    if ((angle <= an) && (angle > (an - (360 / mData.size())))) {
                        Log.d("ROTATE: ", "angle ROTATE: " + an + " , angle TOUCH: " + angle + " ,I: " + i);
                        setCurrentItem(i);
                        setPressedColor(i);
                    }
                } else {
                    if ((angle <= an) && (angle > (an - (360 / mData.size()))||
                            (angle < 360) && (angle > (an - (360 / mData.size()))))) {
                        Log.d("ROTATE: ", "angle ROTATE: " + an + " , angle TOUCH: " + angle + " ,I: " + i);
                        setCurrentItem(i);
                        setPressedColor(i);
                    }
                }
            }

            return true;
        }

        public void setPressedColor(int i){
            Item current;
            for(int j = 0; j < mData.size(); j++){
                if (j == i){
                    current = mData.get(i);
                    current.mSliceColor = getResources().getColor(R.color.pressedColor);
                    current.mStrokeColor = getResources().getColor(R.color.pressedColor);

                } else{
                    current = mData.get(j);
                    current.mSliceColor = getResources().getColor(R.color.fillSector);
                    current.mStrokeColor = getResources().getColor(R.color.strokeColor);
                }
            }
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Set the pie rotation directly.
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
            // Set up the Scroller for a fling
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

                // Start the animator and tell it to animate for the expected duration of the fling.
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
        if(Math.sqrt(Math.pow((e.getX() - mDiameterMax/2), 2) +
                Math.pow((e.getY() - mDiameterMax/2), 2))
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

