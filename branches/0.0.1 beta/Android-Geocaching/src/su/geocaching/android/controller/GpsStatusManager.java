package su.geocaching.android.controller;

import java.util.ArrayList;
import java.util.List;

import su.geocaching.android.ui.R;
import android.content.Context;
import android.location.*;
import android.util.Log;

/**
 * This class listen status of gps engine
 * 
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Nov 18, 2010
 */
public class GpsStatusManager implements GpsStatus.Listener {
    private final static String TAG = GpsStatusManager.class.getCanonicalName();

    private List<IGpsStatusAware> subsribers;
    private LocationManager locationMaganer;
    private Context context;

    /**
     * @param locationManager
     *            manager which can add or remove updates of gps status
     * @param context
     *            which can get strings from application resources
     */
    public GpsStatusManager(LocationManager locationManager, Context context) {
	this.locationMaganer = locationManager;
	subsribers = new ArrayList<IGpsStatusAware>();
	this.context = context;
	Log.d(TAG, "Init");
    }

    /**
     * @param subsriber
     *            activity which will be listen location updates
     */
    public void addSubscriber(IGpsStatusAware subsriber) {
	if (subsribers.size() == 0) {
	    addUpdates();
	}
	if (!subsribers.contains(subsriber)) {
	    subsribers.add(subsriber);
	}
	Log.d(TAG, "add subsriber. Count of subsribers became " + Integer.toString(subsribers.size()));
    }

    /**
     * @param subsriber
     *            activity which no need to listen location updates
     * @return true if activity was subsribed on location updates
     */
    public boolean removeSubsriber(IGpsStatusAware subsriber) {
	boolean res = subsribers.remove(subsriber);
	if (subsribers.size() == 0) {
	    removeUpdates();
	}
	Log.d(TAG, "remove subsriber. Count of subsribers became " + Integer.toString(subsribers.size()));
	return res;
    }

    /**
     * Add this to listeners of gps status
     */
    private void addUpdates() {
	locationMaganer.addGpsStatusListener(this);
	Log.d(TAG, "add updates");
    }

    /**
     * Remove this to listeners of gps status
     */
    private void removeUpdates() {
	locationMaganer.removeGpsStatusListener(this);
	Log.d(TAG, "remove updates");
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.location.GpsStatus.Listener#onGpsStatusChanged(int)
     */
    @Override
    public void onGpsStatusChanged(int arg0) {
	String status = "";
	Log.d(TAG, "gps status changed");
	switch (arg0) {
	case GpsStatus.GPS_EVENT_STARTED:
	    status = context.getString(R.string.gps_status_started);
	    Log.d(TAG, "     started");
	    break;
	case GpsStatus.GPS_EVENT_STOPPED:
	    status = context.getString(R.string.gps_status_stopped);
	    Log.d(TAG, "     stoped");
	    break;
	case GpsStatus.GPS_EVENT_FIRST_FIX:
	    status = context.getString(R.string.gps_status_first_fix);
	    Log.d(TAG, "     first fix");
	    break;
	case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
	    status = context.getString(R.string.gps_status_satellite_status) + " ";
	    GpsStatus gpsStatus = locationMaganer.getGpsStatus(null);
	    int usedInFix = 0;
	    int count = 0;
	    if (gpsStatus.getSatellites() == null) {
		status = "GPS: unknown";
		Log.d(TAG, "     no satellities");
		break;
	    }
	    for (GpsSatellite satellite : gpsStatus.getSatellites()) {
		count++;
		if (satellite.usedInFix()) {
		    usedInFix++;
		}
	    }
	    status += usedInFix + "/" + count;
	    Log.d(TAG, "     satellities all=" + count + " used in fix =" + usedInFix);

	}
	for (IGpsStatusAware subsriber : subsribers) {
	    subsriber.updateStatus(status);
	}
    }

}