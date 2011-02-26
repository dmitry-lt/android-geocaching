package su.geocaching.android.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import su.geocaching.android.ui.geocachemap.ConnectionManager;
import su.geocaching.android.ui.geocachemap.IInternetAware;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.datastorage.DbManager;
import su.geocaching.android.model.datastorage.DownloadInfoCacheTask;
import su.geocaching.android.model.datastorage.DownloadWebNotebookTask;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.model.datatype.GeoCacheType;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.searchgeocache.SearchGeoCacheMap;
import su.geocaching.android.utils.GpsHelper;
import su.geocaching.android.utils.UiHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.maps.GeoPoint;

/**
 * 
 * 
 * @author Alekseenko Vladimir
 * 
 */
public class ShowGeoCacheInfo extends Activity implements OnCheckedChangeListener, OnClickListener, IInternetAware {
	private static final String TAG = ShowGeoCacheInfo.class.getCanonicalName();
	private WebView webView;
	private TextView tvNameText;
	private TextView tvTypeGeoCacheText;
	private TextView tvStatusGeoCacheText;
	private ImageView btGoToSearchGeoCache;
	private CheckBox cbAddDelCache;
	private DbManager dbm;
	private MenuItem menuInfo;
	private GeoCache GeoCacheForShowInfo;
	private String htmlTextGeoCache = null;
	private String htmlTextNotebookGeoCache = null;
	private boolean isCacheStoredInDataBase;
	private ConnectionManager connectManager;
	private GoogleAnalyticsTracker tracker;
	private boolean isPageNoteBook;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info_geocach_activity);
		dbm = new DbManager(getApplicationContext());

		webView = (WebView) findViewById(R.id.info_web_brouse);
		btGoToSearchGeoCache = (ImageView) findViewById(R.id.info_geocach_Go_button);
		cbAddDelCache = (CheckBox) findViewById(R.id.info_geocache_add_del);
		tvNameText = (TextView) findViewById(R.id.info_text_name);
		tvTypeGeoCacheText = (TextView) findViewById(R.id.info_GeoCache_type);
		tvStatusGeoCacheText = (TextView) findViewById(R.id.info_GeoCache_status);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient());
		isPageNoteBook = false;
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start(getString(R.string.id_Google_Analytics), this);
		tracker.trackPageView(getString(R.string.geocache_info_activity_folder));
		tracker.dispatch();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			if (htmlTextNotebookGeoCache == null || htmlTextNotebookGeoCache == "") {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(this.getString(R.string.ask_download_notebook)).setCancelable(false)
						.setPositiveButton(this.getString(R.string.ask_download_notebook_yes), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								htmlTextNotebookGeoCache = getHtmlString(!isPageNoteBook);
								dialog.cancel();
							}
						}).setNegativeButton(this.getString(R.string.ask_download_notebook_no), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
				AlertDialog askDownloadNotebook = builder.create();
				askDownloadNotebook.show();
			}
			dbm.openDB();
			dbm.addGeoCache(GeoCacheForShowInfo, htmlTextGeoCache, htmlTextNotebookGeoCache);
			dbm.closeDB();
		} else {
			dbm.openDB();
			dbm.deleteCacheById(GeoCacheForShowInfo.getId());
			dbm.closeDB();
		}

	}

	@Override
	protected void onStop() {
		connectManager.removeSubscriber(this);
		tracker.stop();
		super.onStop();
	}

	@Override
	protected void onStart() {
		GeoCacheForShowInfo = getIntent().getParcelableExtra(GeoCache.class.getCanonicalName());

		connectManager = Controller.getInstance().getConnectionManager(this);
		connectManager.addSubscriber(this);

		dbm.openDB();
		isCacheStoredInDataBase = (dbm.getCacheByID(GeoCacheForShowInfo.getId()) != null);
		dbm.closeDB();

		if (isCacheStoredInDataBase)
			cbAddDelCache.setChecked(true);

		tvNameText.setText(GeoCacheForShowInfo.getName());

		switch (GeoCacheForShowInfo.getStatus()) {
		case VALID:
			tvStatusGeoCacheText.setText(getString(R.string.status_geocache_valid));
			break;
		case NOT_VALID:
			tvStatusGeoCacheText.setText(getString(R.string.status_geocache_no_valid));
			break;
		case NOT_CONFIRMED:
			tvStatusGeoCacheText.setText(getString(R.string.status_geocache_no_confirmed));
			break;			
		case ACTIVE_CHECKPOINT:
			tvStatusGeoCacheText.setText(getString(R.string.status_geocache_active_checkpoint));
			break;
		case NOT_ACTIVE_CHECKPOINT:
			tvStatusGeoCacheText.setText(getString(R.string.status_geocache_not_active_checkpoint));
			break;			
		default:
			tvStatusGeoCacheText.setText(getString(R.string.status_geocache_no_confirmed));
			break;
		}

		switch (GeoCacheForShowInfo.getType()) {
		case TRADITIONAL:
			tvTypeGeoCacheText.setText(getString(R.string.type_geocache_traditional));
			break;
		case VIRTUAL:
			tvTypeGeoCacheText.setText(getString(R.string.type_geocache_virtua));
			break;
		case STEP_BY_STEP:
			tvTypeGeoCacheText.setText(getString(R.string.type_geocache_step_by_step));
			break;
		case EVENT:
			tvTypeGeoCacheText.setText(getString(R.string.type_geocache_event));
			break;
		case EXTREME:
			tvTypeGeoCacheText.setText(getString(R.string.type_geocache_extreme));
			break;
		case CHECKPOINT:
			tvTypeGeoCacheText.setText(getString(R.string.type_geocache_checkpoint));
			break;
		default:
			tvTypeGeoCacheText.setText("???");
			break;
		}

		cbAddDelCache.setOnCheckedChangeListener(this);
		btGoToSearchGeoCache.setOnClickListener(this);

		if (!connectManager.isInternetConnected() && !isCacheStoredInDataBase)
			webView.loadData("<?xml version='1.0' encoding='utf-8'?>" + "<center>" + getString(R.string.info_geocach_not_internet_and_not_in_DB) + "</center>", "text/html", "utf-8");
		else
			htmlTextGeoCache = getHtmlString(isPageNoteBook);
		webView.loadDataWithBaseURL("http://pda.geocaching.su/", htmlTextGeoCache, "text/html", "utf-8", "");
		super.onStart();
	}

	@Override
	public void onClick(View v) {
		if (!isCacheStoredInDataBase) {
			cbAddDelCache.setChecked(true);
		}
		Intent intent = new Intent(this, SearchGeoCacheMap.class);
		intent.putExtra(GeoCache.class.getCanonicalName(), GeoCacheForShowInfo);
		startActivity(intent);
	}

	public void onHomeClick(View v) {
		UiHelper.goHome(this);
	}

	/**
	 * This class need for
	 * 
	 * 
	 */

	@Override
	public void onInternetLost() {
		btGoToSearchGeoCache.setOnClickListener(null);
		Toast.makeText(getBaseContext(), getString(R.string.select_geocache_status_without_internet), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onInternetFound() {
		btGoToSearchGeoCache.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.info_about_cache, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.show_web_notebook_cache: {
			changeMenuItem(item);
			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void changeMenuItem(MenuItem item) {
		if (!connectManager.isInternetConnected() && !isCacheStoredInDataBase)
			webView.loadData("<?xml version='1.0' encoding='utf-8'?>" + "<center>" + getString(R.string.info_geocach_not_internet_and_not_in_DB) + "</center>", "text/html", "utf-8");
		else {
			if (!isPageNoteBook) {
				item.setTitle(R.string.menu_show_info_cache);
				isPageNoteBook = true;
				htmlTextNotebookGeoCache = getHtmlString(isPageNoteBook);
				webView.loadDataWithBaseURL("http://pda.geocaching.su/", htmlTextNotebookGeoCache, "text/html", "utf-8", "");

			} else {
				isPageNoteBook = false;
				item.setTitle(R.string.menu_show_web_notebook_cache);
				htmlTextGeoCache = getHtmlString(isPageNoteBook);
				webView.loadDataWithBaseURL("http://pda.geocaching.su/", htmlTextGeoCache, "text/html", "utf-8", "");
			}
		}
	}

	private String getHtmlString(boolean isPageNoteBook) {
		String exString = "";
		if (!isPageNoteBook)
			try {
				exString = new DownloadInfoCacheTask(dbm, isCacheStoredInDataBase, GeoCacheForShowInfo.getId()).execute(htmlTextGeoCache).get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else {
			try {
				exString = new DownloadWebNotebookTask(dbm, isCacheStoredInDataBase, GeoCacheForShowInfo.getId()).execute(htmlTextNotebookGeoCache).get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return exString;

	}
}