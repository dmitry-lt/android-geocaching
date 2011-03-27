package su.geocaching.android.ui.searchgeocache;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;

public class DistanceToGeoCacheOverlay extends com.google.android.maps.Overlay {

    //private static final int LINE_COLOR = Color.BLUE;

    private GeoPoint userPoint;
    private GeoPoint cachePoint;
    private Paint paintLine;
    private boolean withShortestWay;
    private Point to;
    private Point from;

    public DistanceToGeoCacheOverlay(GeoPoint userPoint, GeoPoint cachePoint) {
        this.userPoint = userPoint;
        this.cachePoint = cachePoint;
        withShortestWay = true;
        from = new Point();
        to = new Point();
        paintLine = new Paint();
        paintLine.setARGB(100, 100, 20, 100);

        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setAntiAlias(true);
        paintLine.setStrokeWidth(4);
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, shadow);

        if (withShortestWay) {

            Projection proj = mapView.getProjection();
            proj.toPixels(userPoint, from);
            proj.toPixels(cachePoint, to);

            canvas.drawLine(from.x, from.y, to.x, to.y, paintLine);
        }       
    }

    protected void setUserPoint(GeoPoint userPoint) {
        this.userPoint = userPoint;
    }

    protected void setCachePoint(GeoPoint cachePoint) {
        this.cachePoint = cachePoint;
    }

    protected void setShorteshtWayVisible(boolean with) {
        withShortestWay = with;
    }
}
