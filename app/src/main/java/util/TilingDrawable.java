package util;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Created by Ahmad on 10/29/17.
 * All rights reserved.
 */

public class TilingDrawable extends android.support.v7.graphics.drawable.DrawableWrapper {

    private boolean callbackEnabled = true;

    public TilingDrawable(Drawable drawable) {
        super(drawable);
    }

    @Override
    public void draw(Canvas canvas) {
        callbackEnabled = false;
        Rect bounds = getBounds();
        Drawable wrappedDrawable = getWrappedDrawable();

        int width = wrappedDrawable.getIntrinsicWidth();
        int height = wrappedDrawable.getIntrinsicHeight();
        for (int x = bounds.left; x < bounds.right + width - 1; x+= width) {
            for (int y = bounds.top; y < bounds.bottom + height - 1; y += height) {
                wrappedDrawable.setBounds(x, y, x + width, y + height);
                wrappedDrawable.draw(canvas);
            }
        }
        callbackEnabled = true;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
    }

    /**
     * {@inheritDoc}
     */
    public void invalidateDrawable(Drawable who) {
        if (callbackEnabled) {
            super.invalidateDrawable(who);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        if (callbackEnabled) {
            super.scheduleDrawable(who, what, when);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void unscheduleDrawable(Drawable who, Runnable what) {
        if (callbackEnabled) {
            super.unscheduleDrawable(who, what);
        }
    }
}