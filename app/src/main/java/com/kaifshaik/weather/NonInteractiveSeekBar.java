package com.kaifshaik.weather;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

public class NonInteractiveSeekBar extends androidx.appcompat.widget.AppCompatSeekBar {

    public NonInteractiveSeekBar(Context context) {
        super(context);
    }

    public NonInteractiveSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NonInteractiveSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Do not call super.onTouchEvent(event) to prevent default touch behavior
        return true; // Consume the touch event without changing progress
    }
}
