package su.geocaching.android.ui.searchmap;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.ui.R;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Apr 1, 2011
 * 
 */
public class SearchMapPreferenceActivity extends PreferenceActivity {
    /*
     * (non-Javadoc)
     * 
     * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
     */
    private static final String SEARCH_MAP_PREFERENCE_ACTIVITY_FOLDER = "/SearchMapPreferenceActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Controller.getInstance().getGoogleAnalyticsManager().trackPageView(SEARCH_MAP_PREFERENCE_ACTIVITY_FOLDER);
        addPreferencesFromResource(R.xml.search_gc_map_preference);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}