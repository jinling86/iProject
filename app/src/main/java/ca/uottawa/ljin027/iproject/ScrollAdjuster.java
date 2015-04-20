package ca.uottawa.ljin027.iproject;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by ljin027 on 19/04/2015.
 */
public class ScrollAdjuster extends ScrollView {
    private int onLayoutScrollByX = 0;
    private int onLayoutScrollByY = 0;

    public ScrollAdjuster(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ScrollAdjuster(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollAdjuster(Context context) {
        super(context);
    }

    public interface OnScrollChangedListener {
        void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt);
    }

    private OnScrollChangedListener mOnScrollChangedListener;

    public void setOnScrollChangedListener(OnScrollChangedListener listener) {
        mOnScrollChangedListener = listener;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollChangedListener != null) {
            mOnScrollChangedListener.onScrollChanged(this, l, t, oldl, oldt);
        }
    }



    public void planScrollBy(int x, int y) {
        onLayoutScrollByX += x;
        onLayoutScrollByY += y;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        doPlannedScroll();
    }

    public void doPlannedScroll() {
        if (onLayoutScrollByX != 0 || onLayoutScrollByY != 0) {
            scrollBy(onLayoutScrollByX, onLayoutScrollByY);
            onLayoutScrollByX = 0;
            onLayoutScrollByY = 0;
        }
    }
}
