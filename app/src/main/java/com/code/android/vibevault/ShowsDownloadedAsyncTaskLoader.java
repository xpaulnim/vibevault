package com.code.android.vibevault;

import android.content.Context;

import androidx.loader.content.AsyncTaskLoader;

import java.util.ArrayList;

public class ShowsDownloadedAsyncTaskLoader extends AsyncTaskLoader<ArrayList<ArchiveShowObj>> {

    private static final String LOG_TAG = ShowsDownloadedAsyncTaskLoader.class.getName();

    private StaticDataStore db;

    public ShowsDownloadedAsyncTaskLoader(Context context) {
        super(context);
        db = StaticDataStore.getInstance(context);
        Logging.Log("Download Async", "Created task");
    }

    @Override
    public void onStartLoading() {
        forceLoad();
    }

    @Override
    public ArrayList<ArchiveShowObj> loadInBackground() {
        Logging.Log(LOG_TAG, "Getting shows from DataStore");
        ArrayList<ArchiveShowObj> shows = db.getDownloadShows();
        return shows;
    }

    @Override
    public void deliverResult(ArrayList<ArchiveShowObj> shows) {
        super.deliverResult(shows);
    }

}
