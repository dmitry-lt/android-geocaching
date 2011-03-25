package su.geocaching.android.ui;

import su.geocaching.android.controller.LogManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter;

public class CheckpointsFolder extends AbstractCacheFolder implements OnItemClickListener {

    public static String CACHE_ID = "cacheId";

    private static final String TAG = FavoritesFolder.class.getCanonicalName();
    private int cacheid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cacheid = getIntent().getIntExtra(CACHE_ID, 0);
    }

    @Override
    protected void onResume() {
        favoritesList = dbm.getCheckpointsArrayById(cacheid);
        if (favoritesList.isEmpty()) {
            lvListShowCache.setAdapter(null);
            tvNoCache.setText(getString(R.string.favorit_folder_In_DB_not_cache)); // TODO Replace
            LogManager.d(TAG, "checkpoints DB empty");
        } else {
            SimpleAdapter simpleAdapter = new SimpleAdapter(this, createGeoCacheList(favoritesList), R.layout.row_in_favorit_rolder, new String[] { "type", "name", "typeText", "statusText" },
                    new int[] { R.id.favorite_list_image_button_type, R.id.favorite_list_text_view_name, R.id.favorites_row_type_text, R.id.favorites_row_status_text });

            lvListShowCache.setAdapter(simpleAdapter);
        }
        super.onResume();
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
    }
}