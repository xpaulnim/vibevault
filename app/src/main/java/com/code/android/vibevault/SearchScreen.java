package com.code.android.vibevault;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.code.android.vibevault.SearchSettingsDialogFragment.SearchSettingsDialogInterface;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class SearchScreen extends AppCompatActivity implements
        DialogAndNavigationListener,
        SearchSettingsDialogInterface,
        NowPlayingFragment.PlayerListener {

    private static final String LOG_TAG = SearchScreen.class.getName();

    private static final int UPGRADE_DB = 20;

    private final int EXTERNAL_STORAGE_PERMISSION = 1;
    private final String[] REQUIRED_PERMISSIONS = new String[]{WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onResume() {
        super.onResume();
        if (this.getIntent() != null && this.getIntent().hasExtra("type")) {
            Logging.Log(LOG_TAG, this.getIntent().getExtras().getInt("type"));
        }

//		NowPlayingFragment nowPlayingFrag = (NowPlayingFragment) getFragmentManager().findFragmentByTag("nowplaying");
//		if(nowPlayingFrag==null){
//			getFragmentManager().
//		}
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void clearBackStack() {
        for (int i = 0; i < this.getSupportFragmentManager().getBackStackEntryCount(); ++i) {
            this.getSupportFragmentManager().popBackStack();
        }
    }

    private boolean storagePermissionGranted() {
        return ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!storagePermissionGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, EXTERNAL_STORAGE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == EXTERNAL_STORAGE_PERMISSION) {
            if (storagePermissionGranted()) {
                Logging.Log(LOG_TAG, "Permission granted");
            } else {
                Logging.Log(LOG_TAG, "Permission not granted");
            }
        }
    }

    private boolean dialogShown;

    private StaticDataStore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_screen);

        db = StaticDataStore.getInstance(this);

        BottomNavigationView bottomBarView = findViewById(R.id.bottom_nav_view);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomBarView, navController);
    }

    private boolean needsArtistFetching() {
        String dateString = db.getPref("artistUpdate");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            Logging.Log(LOG_TAG, "Trying date parse: " + dateString);
            Date dbDate = format.parse(dateString);
            GregorianCalendar cal1 = new GregorianCalendar();
            cal1.add(Calendar.MONTH, -2);
            Date upgradeDate = cal1.getTime();
            GregorianCalendar cal2 = new GregorianCalendar();
            cal2.add(Calendar.YEAR, 1);
            Date yearLater = cal2.getTime();
            Logging.Log(LOG_TAG, "Comparing " + upgradeDate.toString() + " .after(" + dbDate.toString());
			return upgradeDate.after(dbDate) || yearLater.before(dbDate);
        } catch (java.text.ParseException e) {
            Logging.Log(LOG_TAG, "Error Getting Artists");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return Navigation.findNavController(this, R.id.nav_host_fragment).navigateUp();
    }


    @Override
    public void showLoadingDialog(String message) {
        showLoadingDialog(message, true);
    }

    @Override
    public void showLoadingDialog(String message, boolean useTitle) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction. We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        // Note that if there was a previous dialog, it might still be
        // being removed from the Activity, in which case we don't try
        // to remove it again, because we would get an error.
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            if (prev.isRemoving()) {
            } else {
                ft.remove(prev);
            }
        }
        // Create and show the dialog.
        DialogFragment newFragment = LoadingDialog.newInstance(message, useTitle);
        newFragment.show(ft, "dialog");
    }

    @Override
    public void showSettingsDialog(Bundle b) {
        SearchSettingsDialogFragment settingsFrag = SearchSettingsDialogFragment.newInstance(
                b.getString("type"),
                b.getInt("number"),
                b.getInt("datepos"),
                b.getInt("month"),
                b.getInt("day"),
                b.getInt("year"));
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction. We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        // Note that if there was a previous dialog, it might still be
        // being removed from the Activity, in which case we don't try
        // to remove it again, because we would get an error.
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            if (prev.isRemoving()) {
            } else {
                ft.remove(prev);
            }
        }
        // Create and show the dialog.
        settingsFrag.show(ft, "dialog");
    }

    @Override
    public void showDialog(String message, String title) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction. We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        // Note that if there was a previous dialog, it might still be
        // being removed from the Activity, in which case we don't try
        // to remove it again, because we would get an error.
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            if (prev.isRemoving()) {
            } else {
                ft.remove(prev);
            }
        }
        // Create and show the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle(title).setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        builder.setMessage(message).setTitle(title).setNeutralButton("Donate!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=vibevault%40gmail%2ecom&lc=US&item_name=Vibe%20Vault&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted"));
                startActivity(browserIntent);
            }
        });
        builder.create().show();
        ft.commit();
    }

    @Override
    public void hideDialog() {
        // If a dialog currently exists, remove it, unless it is currently being removed.
        // It would be nice to be able to call dismiss but Android will crash if you commit
        // (which calling dismiss will do) without allowing state loss.  See this post:
        // https://groups.google.com/forum/#!topic/android-developers/dXZZjhRjkMk/discussion
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        LoadingDialog prev = (LoadingDialog) getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            if (prev.isRemoving()) {
            } else {
                ft.remove(prev);
            }
        }
        ft.commitAllowingStateLoss();
    }

    @Override
    public void goHome() {
        Intent i = new Intent(SearchScreen.this, SearchScreen.class);
        startActivity(i);
        this.finish();
    }

    @Override
    public void onSettingsOkayButtonPressed(String searchType, int numResults, int dateTypePos, int month, int day, int year) {
        SearchFragment searchFrag = (SearchFragment) this.getSupportFragmentManager().findFragmentByTag("searchfrag");
        if (searchFrag != null) {
            searchFrag.onSettingsOkayButtonPressed(searchType, numResults, dateTypePos, month, day, year);
        }
    }

//	@Override
//	public void playShow(int pos, ArrayList<ArchiveSongObj> showSongs) {
//
//		this.instantiateNowPlayingFragmentForActivity(pos, showSongs);
//	}

    @Override
    public void registerReceivers(BroadcastReceiver stateChangedBroadcast, BroadcastReceiver positionChangedBroadcast) {
        registerReceiver(stateChangedBroadcast, new IntentFilter(PlaybackService.SERVICE_STATE));
        registerReceiver(positionChangedBroadcast, new IntentFilter(PlaybackService.SERVICE_POSITION));
    }

    @Override
    public void unregisterReceivers(BroadcastReceiver stateChangedBroadcast, BroadcastReceiver positionChangedBroadcast) {
        unregisterReceiver(stateChangedBroadcast);
        unregisterReceiver(positionChangedBroadcast);
    }

    /** Persist worker Thread across orientation changes.
     *
     * Includes Thread bookkeeping to prevent not leaking Views on orientation changes.
     */
//	@Override
//	public Object onRetainNonConfigurationInstance(){
//		if (upgradeTask != null) {
//			upgradeTask.setActivity(null);
//		}
//		return upgradeTask;
//	}

    /**
     * Dialog preparation method.
     * <p>
     * Includes Thread bookkeeping to prevent not leaking Views on orientation changes.
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        if (id == UPGRADE_DB) {
            dialogShown = true;
        }
    }

    /**
     * Dialog creation method.
     * <p>
     * Includes Thread bookkeeping to prevent not leaking Views on orientation changes.
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case UPGRADE_DB:

                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage("Updating Database, ");
                return dialog;
            default:
                return super.onCreateDialog(id);
        }
    }

    private void onTaskCompleted() {
        if (dialogShown) {
            try {
                dismissDialog(UPGRADE_DB);
            } catch (IllegalArgumentException e) {

                e.printStackTrace();
            }

        }
    }

}
