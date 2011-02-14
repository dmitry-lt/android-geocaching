package su.geocaching.android.controller.compass;

import su.geocaching.android.ui.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class CompassPreferenceManager {

	public static String PREFS_COMPASS_SPEED_KEY;
	private static CompassPreferenceManager compassPreference;
	private SharedPreferences preferences;

	private CompassPreferenceManager(Context context) {
		preferences = PreferenceManager.getDefaultSharedPreferences(context);

		PREFS_COMPASS_SPEED_KEY = context.getString(R.string.prefs_speed_key);
	}

	public String getString(String key, String defaultValue) {
		return preferences.getString(PREFS_COMPASS_SPEED_KEY, defaultValue);
	}

	public static CompassPreferenceManager getPreference(Context context) {
		if (compassPreference == null) {
			compassPreference = new CompassPreferenceManager(context);
		}
		return compassPreference;
	}
}