package su.geocaching.android.ui.compass;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.compass.AbstractCompassDrawing;
import su.geocaching.android.controller.compass.DefaultCompassDrawing;
import su.geocaching.android.controller.compass.PreviewCompassDrawing;
import su.geocaching.android.controller.compass.WhiteStandardCompassDrawing;
import su.geocaching.android.controller.managers.IBearingAware;
import su.geocaching.android.controller.managers.LogManager;

/**
 * @author Nikita Bumakov
 */
public class OneThreadCompassView extends View implements IBearingAware {

    private static final String TAG = OneThreadCompassView.class.getCanonicalName();

    private AbstractCompassDrawing compassDrawing;

    private float northDirection; // in degrees
    private float cacheDirection;
    private boolean isLocationFixed = false;

    public OneThreadCompassView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        LogManager.d(TAG, "new CompassView");
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        if (compassDrawing != null) {
            super.onDraw(canvas);
            compassDrawing.draw(canvas, northDirection);
            if (isLocationFixed) {
                compassDrawing.drawCacheArrow(canvas, cacheDirection + northDirection);
            }
        } else {
            LogManager.w("TAG", "draw not ready");
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        LogManager.d(TAG, "onSizeChanged" + w + " " + h);
        compassDrawing.onSizeChanged(w, h);
    }

    /**
     * @param direction - direction to geocache in degrees
     */
    public void setCacheDirection(float direction) {
        cacheDirection = direction;
        isLocationFixed = true;
        //doAnimation();
    }

     /**
     * @param string //TODO describe it
     */
    // TODO too many objects
    public void setHelper(String string) {
        if (string.equals("CLASSIC") && !(compassDrawing instanceof DefaultCompassDrawing)) {
            compassDrawing = new DefaultCompassDrawing();
        } else if (string.equals("PALE") && !(compassDrawing instanceof WhiteStandardCompassDrawing)) {
            compassDrawing = new WhiteStandardCompassDrawing();
        } else if (string.equals("PREVIEW") && !(compassDrawing instanceof PreviewCompassDrawing)) {
            compassDrawing = new PreviewCompassDrawing();
        }

        if (getWidth() > 0) {
            compassDrawing.onSizeChanged(getWidth(), getHeight());
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Controller.getInstance().getCompassManager().addSubscriber(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
          Controller.getInstance().getCompassManager().removeSubscriber(this);
    }

    @Override
    public void updateBearing(float bearing) {
        northDirection = -bearing;
        invalidate();
    }
}