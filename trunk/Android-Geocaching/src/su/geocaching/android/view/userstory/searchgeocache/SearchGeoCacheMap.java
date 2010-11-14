package su.geocaching.android.view.userstory.searchgeocache;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.view.MainMenu;
import su.geocaching.android.view.R;
import su.geocaching.android.view.geocachemap.GeoCacheItemizedOverlay;
import su.geocaching.android.view.geocachemap.GeoCacheMap;

/**
 * @author Android-Geocaching.su student project team
 * @description Search GeoCache with the map.
 * @since October 2010
 */
public class SearchGeoCacheMap extends GeoCacheMap {
    public final static String DEFAULT_GEOCACHE_ID_NAME = "GeoCache id";

    private GeoCache geoCache;
    private OverlayItem cacheOverlayItem;
    private GeoCacheItemizedOverlay cacheItemizedOverlay;
    private DistanceToGeoCacheOverlay distanceOverlay;
    private MyLocationOverlay userOverlay;
    private AlertDialog waitingLocationFixAlert;
    private boolean isLocationFixed;
    private boolean wasInitialized;
    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	wasInitialized = false;
    }

    @Override
    protected void onPause() {
	super.onPause();

	if (wasInitialized) {
	    userOverlay.disableCompass();
	    userOverlay.disableMyLocation();
	    locationManager.pause();
	}
    }

    @Override
    protected void onResume() {
	super.onResume();
	if (!isBestProviderEnabled()) {
	    askTurnOnGPS();
	} else {
	    runLogic();
	}
    }

    /**
     * Ask user turn on GPS, if this disabled
     */
    private void askTurnOnGPS() {
	if (isBestProviderEnabled()) {
	    return;
	}
	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	builder.setMessage(getString(R.string.ask_enable_gps_text)).setCancelable(false).setPositiveButton(getString(R.string.ask_enable_gps_yes), new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface dialog, int id) {
		Intent startGPS = new Intent(Settings.ACTION_SECURITY_SETTINGS);
		startActivity(startGPS);
		dialog.cancel();
	    }
	}).setNegativeButton(getString(R.string.ask_enable_gps_no), new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface dialog, int id) {
		dialog.cancel();
		finish();
	    }
	});
	AlertDialog turnOnGPSAlert = builder.create();
	turnOnGPSAlert.show();
    }

    /**
     * @return true if best provider enabled
     */
    private boolean isBestProviderEnabled() {
	LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	Criteria criteria = new Criteria();
	criteria.setAccuracy(Criteria.ACCURACY_FINE);
	String bestProv = locManager.getBestProvider(criteria, false);
	if (!bestProv.equals(LocationManager.GPS_PROVIDER)) {
		Toast.makeText(this, getString(R.string.device_without_gps_alert), Toast.LENGTH_LONG).show();
	}
	return locManager.isProviderEnabled(bestProv);
    }

    /**
     * Init and run all activity content
     */
    private void runLogic() {
	// from onCreate
	// Controller controller = Controller.getInstance();

	// not working yet
	// geoCache = controller.getGeoCacheByID(intent.getIntExtra(
	// MainMenu.DEFAULT_GEOCACHE_ID_NAME, -1));

	Intent intent = this.getIntent();
	geoCache = new GeoCache(intent.getIntExtra(MainMenu.DEFAULT_GEOCACHE_ID_NAME, -1));
	isLocationFixed = intent.getBooleanExtra("location fixed", false);
	statusTextView = (TextView) findViewById(R.id.statusTextView);

	userOverlay = new MyLocationOverlay(this, map);
	userOverlay.enableCompass();
	userOverlay.enableMyLocation();

	if (!isLocationFixed) {
	    showWaitingLocationFix();
	}

	// from onResume
	locationManager.resume();
	Drawable cacheMarker = this.getResources().getDrawable(R.drawable.orangecache);
	cacheMarker.setBounds(0, -cacheMarker.getMinimumHeight(), cacheMarker.getMinimumWidth(), 0);

	cacheItemizedOverlay = new GeoCacheItemizedOverlay(cacheMarker);
	cacheOverlayItem = new OverlayItem(geoCache.getLocationGeoPoint(), "", "");
	cacheItemizedOverlay.addOverlayItem(cacheOverlayItem);
	mapOverlays.add(cacheItemizedOverlay);

	map.invalidate();

	wasInitialized = true;
    }

    /**
     * Show cancelable alert which tell user what location fixing
     */
    private void showWaitingLocationFix() {
	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	builder.setMessage(getString(R.string.waiting_location_fix_message)).setCancelable(false)
		.setPositiveButton(getString(R.string.waiting_location_fix_fon), new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
			dialog.cancel();
			statusTextView.setText("Waiting location fix...");
			
		    }
		}).setNegativeButton(getString(R.string.waiting_location_fix_cancel), new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
			dialog.cancel();
			finish();
		    }
		});
	waitingLocationFixAlert = builder.create();
	waitingLocationFixAlert.show();
    }

    /**
     * Start SearchGeoCacheCompass activity
     */
    private void startCompassView() {
	Intent intent = new Intent(this, SearchGeoCacheCompass.class);
	intent.putExtra(DEFAULT_GEOCACHE_ID_NAME, geoCache.getId());
	intent.putExtra("location fixed", locationManager.isLocationFixed());
	startActivity(intent);
	this.finish();
    }

    @Override
    public void updateLocation(Location location) {
	if (locationManager.isLocationFixed()) {
	    if (!isLocationFixed) {
		if (waitingLocationFixAlert.isShowing()) {
		    waitingLocationFixAlert.dismiss();
		} else {
		   statusTextView.setText("Location fixed");
		}

	    }
	    isLocationFixed = true;
	} else {
	    if (isLocationFixed) {
		locationManager.setLocationFixed();
		userOverlay.onLocationChanged(location);
	    } else {
		return;
	    }
	}
	Location loc = locationManager.getCurrentLocation();
	GeoPoint currentGeoPoint = new GeoPoint((int) (loc.getLatitude() * 1E6), (int) (loc.getLongitude() * 1E6));

	if (distanceOverlay == null) {
	    // It's really first run of update location
	    resetZoom();
	    distanceOverlay = new DistanceToGeoCacheOverlay(currentGeoPoint, geoCache.getLocationGeoPoint());
	    mapOverlays.add(distanceOverlay);
	    mapOverlays.add(userOverlay);
	    return;
	}
	distanceOverlay.setCachePoint(geoCache.getLocationGeoPoint());
	distanceOverlay.setUserPoint(currentGeoPoint);
	map.invalidate();
    }

    @Override
    public Location getLastLocation() {
	return locationManager.getCurrentLocation();
    }

    /**
     * Set map zoom which can show userPoint and GeoCachePoint
     */
    private void resetZoom() {
	Location loc = locationManager.getCurrentLocation();
	GeoPoint currentGeoPoint = new GeoPoint((int) (loc.getLatitude() * 1E6), (int) (loc.getLongitude() * 1E6));
	mapController.zoomToSpan(Math.abs(geoCache.getLocationGeoPoint().getLatitudeE6() - currentGeoPoint.getLatitudeE6()),
		Math.abs(geoCache.getLocationGeoPoint().getLongitudeE6() - currentGeoPoint.getLongitudeE6()));

	GeoPoint center = new GeoPoint((geoCache.getLocationGeoPoint().getLatitudeE6() + currentGeoPoint.getLatitudeE6()) / 2,
		(geoCache.getLocationGeoPoint().getLongitudeE6() + currentGeoPoint.getLongitudeE6()) / 2);
	mapController.animateTo(center);
    }

    /**
     * Creating menu object
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.search_geocache_map, menu);
	return true;
    }

    /**
     * Called when menu element selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case R.id.menuDefaultZoom:
	    if (locationManager.isLocationFixed()) {
		resetZoom();
	    }
	    return true;
	case R.id.menuStartCompass:
	    this.startCompassView();
	    return true;
	case R.id.menuToggleShortestWay:
	    distanceOverlay.toggleShorteshtWayVisible();
	    return true;	    
	default:
	    return super.onOptionsItemSelected(item);
	}
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
	String statusString = "";
	if (!isLocationFixed) {
	    statusString = "Fixing location:\n\t";
	}
	statusString += "Provider: " + provider + "\n\t";
	switch (status) {
	case LocationProvider.OUT_OF_SERVICE:
	    statusString += "Status: out of service";
	case LocationProvider.TEMPORARILY_UNAVAILABLE:
	    statusString += "Status: temporarily unavailable";
	case LocationProvider.AVAILABLE:
	    statusString += "Status: available";
	}
	if (provider.equals(LocationManager.GPS_PROVIDER)) {
	    statusString += "\n\t" + "Satellites: " + extras.getString("satellites");
	}
	showInfo(statusString);
    }

    /**
     * @param string
     *            string which will be shown on waiting dialog or textView
     */
    private void showInfo(String string) {
	if (waitingLocationFixAlert.isShowing()) {
	    waitingLocationFixAlert.setMessage(string);
	} else {
	    statusTextView.setText(string);
	}
    }

    @Override
    public void onProviderEnabled(String provider) {
	// TODO Auto-generated method stub

    }

    @Override
    public void onProviderDisabled(String provider) {
	if (!isBestProviderEnabled()) {
	    askTurnOnGPS();
	}
    }
}