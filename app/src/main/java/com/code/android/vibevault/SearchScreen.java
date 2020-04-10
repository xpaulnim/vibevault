package com.code.android.vibevault;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.code.android.vibevault.BrowseArtistsFragment.BrowseActionListener;
import com.code.android.vibevault.SearchFragment.SearchActionListener;
import com.code.android.vibevault.SearchSettingsDialogFragment.SearchSettingsDialogInterface;
import com.code.android.vibevault.ShowDetailsFragment.ShowDetailsActionListener;
import com.code.android.vibevault.VotesFragment.VotesActionListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.app.AlertDialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONObject;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class SearchScreen extends AppCompatActivity implements SearchActionListener,
		DialogAndNavigationListener,
		SearchSettingsDialogInterface,
		ShowDetailsActionListener,
		NowPlayingFragment.PlayerListener,
		BrowseActionListener,
		VotesActionListener {

	private static final String LOG_TAG = SearchScreen.class.getName();
	
	@Override
	protected void onResume(){
		super.onResume();
		if(this.getIntent()!=null&&this.getIntent().hasExtra("type")){
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
	
	private void clearBackStack(){
		for(int i = 0; i<this.getFragmentManager().getBackStackEntryCount(); ++i){
			this.getFragmentManager().popBackStack();
		}
	}
	
	@Override
	public void onBackPressed(){
		super.onBackPressed();
		if(this.getFragmentManager().getBackStackEntryCount()==0){
			this.finish();
		}
	}
	
	@Override
	protected void onNewIntent(Intent i){
		super.onNewIntent(i);
		this.setIntent(i);
		if(i!=null){
			this.setIntent(i);
			if(i.hasExtra("type")){
				int type = i.getExtras().getInt("type");
				if(type!=NOW_PLAYING_FRAGMENT){
					this.clearBackStack();
				}
				instantiateFragment(i.getExtras().getInt("type"));
			}
		}
	}

	public static final int SEARCH_FRAGMENT = 0;
	public static final int DETAILS_FRAGMENT = 1;
	public static final int NOW_PLAYING_FRAGMENT = 2;
	public static final int SHOWS_STORED_FRAGMENT = 3;
	public static final int VOTES_FRAGMENT = 4;
	public static final int ARTISTS_FRAGMENT = 5;
	public static final int DOWNLOADS_FRAGMENT = 6;

	private static final int UPGRADE_DB = 20;

	private final int EXTERNAL_STORAGE_PERMISSION = 1;
	private final String[] REQUIRED_PERMISSIONS = new String[]{WRITE_EXTERNAL_STORAGE};

	private void instantiateFragment(int type){
		switch(type){
	    	case SEARCH_FRAGMENT: // Search
	    		this.instantiateSearchFragmentForActivity(null);
	    		break;
	    	case DETAILS_FRAGMENT: // Show Details
	    		this.instantiateShowDetailsFragmentForActivity(null);
	    		break;
	    	case NOW_PLAYING_FRAGMENT: // Now Playing
	    		this.instantiateNowPlayingFragmentForActivity(-1, null);
	    		break;
	    	case SHOWS_STORED_FRAGMENT: // Shows Stored
	    		this.instantiateShowsStoredFragmentForActivity();
	    		break;
	    	case VOTES_FRAGMENT: // Votes
	    		this.instantiateVotesFragmentForActivity(null);
	    		break;
	    	case ARTISTS_FRAGMENT: // Browse Artists
	    		this.instantiateBrowseArtistsFragmentForActivity();
	    		break;
	    	case DOWNLOADS_FRAGMENT: // Downloads
	    		this.instantiateShowsDownloadedFramentForActivity();
	    		break;
	    	default:
		}
		Logging.Log(LOG_TAG, "BACK STACK COUNT: " + this.getFragmentManager().getBackStackEntryCount());
	}

	private void setImageButtonToFragments(MenuItem menuItem) {
		Intent i = new Intent(SearchScreen.this, SearchScreen.class);
		i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		switch (menuItem.getItemId()) {
			case R.id.menu_search:
				i.putExtra("type", SEARCH_FRAGMENT);
				break;
			case R.id.menu_your_shows:
				i.putExtra("type", SHOWS_STORED_FRAGMENT);
				break;
			case R.id.menu_downloads:
				i.putExtra("type", DOWNLOADS_FRAGMENT);
				break;
			case R.id.menu_now_playing:
				i.putExtra("type", NOW_PLAYING_FRAGMENT);
				break;
			case R.id.menu_browse_artists:
				i.putExtra("type", ARTISTS_FRAGMENT);
				break;
		}
		startActivity(i);
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

		if (requestCode == EXTERNAL_STORAGE_PERMISSION){
			if (storagePermissionGranted()){
				Logging.Log(LOG_TAG, "Permission granted");

				// do the thing
			} else {
				Logging.Log(LOG_TAG, "Permission not granted");
			}
		}
	}

	private UpgradeTask upgradeTask;

	private boolean dialogShown;

	private StaticDataStore db;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.search_screen);

		Object retained = getLastNonConfigurationInstance();

		if(retained instanceof UpgradeTask){
			Logging.Log(LOG_TAG,"UpgradeTask retained");
			upgradeTask = (SearchScreen.UpgradeTask)retained;
			upgradeTask.setActivity(this);
		} else{
			//upgradeTask = new UpgradeTask(this);
		}
		db = StaticDataStore.getInstance(this);

		BottomNavigationView bottomBarView = (BottomNavigationView) findViewById(R.id.bottom_nav_view);
		bottomBarView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(@androidx.annotation.NonNull MenuItem item) {
				if (db.needsUpgrade && upgradeTask == null) { //DB needs updating
					Toast.makeText(SearchScreen.this, R.string.db_upgrade_message_text, Toast.LENGTH_SHORT).show();
					upgradeTask = new UpgradeTask(SearchScreen.this);
					upgradeTask.execute();
				} else { // DB Up to date, check artist date
					setImageButtonToFragments(item);

					if (needsArtistFetching() && upgradeTask == null) {
						upgradeTask = new UpgradeTask(SearchScreen.this);
						upgradeTask.execute();
					}
				}

				return true;
			}
		});

		// See what type of Fragment we want to launch.
        int type = SEARCH_FRAGMENT;
        
		// See if this Screen was spawned by the user clicking on a link.
		// If so, form an ArchiveShowObj from the URL.
		if (this.getIntent().getScheme()!=null&&this.getIntent().getScheme().equals("https")) {
	        ArchiveShowObj show = null;
			Logging.Log(LOG_TAG, "User clicked on link.");
			type = DETAILS_FRAGMENT;
			String linkString =  this.getIntent().getData().toString();
			Logging.Log(LOG_TAG, "URL: " + linkString);
			if (linkString.contains("/download/")) {
				String[] paths = linkString.split("/");
				for (int i = 0; i < paths.length; i++) {
					if (paths[i].equals("download")) {
						show = new ArchiveShowObj(new String("https://www.archive.org/details/" + paths[i + 1]), true);
						show.setSelectedSong(linkString);
					}
				}
			// Show link clicked on (not an individual song link).
			} else {
				show = new ArchiveShowObj(linkString, false);
			}
			this.instantiateShowDetailsFragmentForActivity(show);
		} else if(savedInstanceState==null){
        	instantiateFragment(type);
		}
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
			if (upgradeDate.after(dbDate) || yearLater.before(dbDate)) {
				return true;
			}
			else {
				return false;
			}
		} catch (java.text.ParseException e) {
			Logging.Log(LOG_TAG, "Error Getting Artists");
			e.printStackTrace();
			return false;
		}
	}


	/**
	 * The instantiate___Activity() methods could be combined into one larger method,
	 * but I am keeping it like this for now in case we want to customize the actions
	 * performed when certain Fragments are opened, and for debugging purposes.  Making
	 * this one large method would save some lines of code, but be harder to read, in my opinion.
	 * @param artist 
	 */
	private void instantiateSearchFragmentForActivity(String artist){
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		SearchFragment frag = (SearchFragment) fm.findFragmentByTag("searchfrag");
		
		Logging.Log(LOG_TAG, "BACK STACK COUNT: " + fm.getBackStackEntryCount());
		Bundle b = this.getIntent().getExtras();
		if(artist!=null){
			b = new Bundle();
			b.putString("Artist", artist);
		}
		if(frag==null){
			frag = new SearchFragment();
			frag.setArguments(b);
			ft.replace(R.id.fragment_container, frag,"searchfrag");
			ft.addToBackStack(null);
		} else{
			// TODO: is this needed
//			frag.getArguments().putAll(b);
			ft.replace(R.id.fragment_container, frag,"searchfrag");
			if(fm.getBackStackEntryCount()==0){
				ft.addToBackStack(null);
			}
		}
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
	    ft.commit();
	}

	// This can take a null show object.
	private void instantiateShowDetailsFragmentForActivity(ArchiveShowObj show){
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ShowDetailsFragment frag = (ShowDetailsFragment) fm.findFragmentByTag("showdetails");
		if(frag==null){
			frag = new ShowDetailsFragment();
			if(show!=null){
    			Bundle b = new Bundle();
    			b.putSerializable("show", show);
    			frag.setArguments(b);
			}
			if(!frag.isAdded()){
				ft.replace(R.id.fragment_container, frag,"showdetails");
				ft.addToBackStack(null);
			}
			
		} else{
			if(show!=null){
    			Bundle b = new Bundle();
    			b.putSerializable("show", show);
    			if(frag.getArguments()!=null){
    				frag.getArguments().putAll(b);
    			} else{
    				frag.setArguments(b);
    			}
    		} else{
    			frag.getArguments().putAll(this.getIntent().getExtras());
    		}
			if(!frag.isAdded()){
				ft.replace(R.id.fragment_container, frag,"showdetails");
			}
			if(fm.getBackStackEntryCount()==0){
				ft.addToBackStack(null);
			}
		}
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
	    ft.commit();
	    Logging.Log(LOG_TAG, "BACK STACK COUNT : " + fm.getBackStackEntryCount());
	}
	
	private void instantiateNowPlayingFragmentForActivity(int pos, ArrayList<ArchiveSongObj> showSongs){
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		NowPlayingFragment frag = (NowPlayingFragment) fm.findFragmentByTag("nowplaying");
		
		Bundle b = this.getIntent().getExtras();
		if(pos >=0 && showSongs != null){
			b = new Bundle();
			b.putSerializable("position", pos);
			b.putSerializable("showsongs", showSongs);
			Logging.Log(LOG_TAG, "Creating Bundle with position and songs.");
		}
		
		if(frag==null){
			Logging.Log(LOG_TAG, "Making a new NowPlayingFragment.");
			frag = new NowPlayingFragment();
			frag.setArguments(b);
			ft.replace(R.id.fragment_container, frag,"nowplaying");
			ft.addToBackStack(null);
		} else{
			Logging.Log(LOG_TAG, "Creating Bundle with position and songs.");
			frag.getArguments().putAll(b);
			ft.replace(R.id.fragment_container, frag,"nowplaying");
			if(fm.getBackStackEntryCount()==0){
				ft.addToBackStack(null);
			}
		}
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
	    ft.commit();

	}
	
	private void instantiateShowsStoredFragmentForActivity(){
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ShowsStoredFragment frag = (ShowsStoredFragment) fm.findFragmentByTag("showsstored");
		if(frag==null){
			frag = new ShowsStoredFragment();
			frag.setArguments(this.getIntent().getExtras());
			ft.replace(R.id.fragment_container, frag,"showsstored");
			ft.addToBackStack(null);
		} else{
			frag.getArguments().putAll(this.getIntent().getExtras());
			ft.replace(R.id.fragment_container, frag,"showsstored");
			if(fm.getBackStackEntryCount()==0){
				ft.addToBackStack(null);
			}
		}
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
	    ft.commit();

	}
	
	private void instantiateBrowseArtistsFragmentForActivity(){
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		BrowseArtistsFragment frag = (BrowseArtistsFragment) fm.findFragmentByTag("browsefrag");
		if(frag==null){
			frag = new BrowseArtistsFragment();
			frag.setArguments(this.getIntent().getExtras());
			ft.replace(R.id.fragment_container, frag,"browsefrag");
			ft.addToBackStack(null);
		} else{
			frag.getArguments().putAll(this.getIntent().getExtras());
			ft.replace(R.id.fragment_container, frag,"browsefrag");
			if(fm.getBackStackEntryCount()==0){
				ft.addToBackStack(null);
			}
		}
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
	    ft.commit();

	}
	
	private void instantiateVotesFragmentForActivity(ArchiveArtistObj artist){
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		VotesFragment frag = (VotesFragment) fm.findFragmentByTag("votesfrag");
		
		Bundle b = this.getIntent().getExtras();
		// This means that we are opening up a VotingFragment with
		// an artist and not a show.  Ths requires creating a new VotesFragment.
		if(artist!=null){
			b = new Bundle();
			b.putSerializable("ArchiveArtist", artist);
		}
		if(frag==null || artist != null){
			frag = new VotesFragment();
			frag.setArguments(b);
			ft.replace(R.id.fragment_container, frag,"votesfrag");
			ft.addToBackStack(null);
		} else{
			frag.getArguments().putAll(b);
			ft.replace(R.id.fragment_container, frag,"votesfrag");
			if(fm.getBackStackEntryCount()==0){
				ft.addToBackStack(null);
			}
		}
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
	    ft.commit();

	}
	
	private void instantiateShowsDownloadedFramentForActivity(){
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ShowsDownloadedFragment frag = (ShowsDownloadedFragment) fm.findFragmentByTag("downloadfrag");
		if(frag==null){
			frag = new ShowsDownloadedFragment();
			ft.replace(R.id.fragment_container, frag,"downloadfrag");
			ft.addToBackStack(null);
		} else {
			ft.replace(R.id.fragment_container, frag,"downloadfrag");
			if(fm.getBackStackEntryCount()==0){
				ft.addToBackStack(null);
			}
		}
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
	    ft.commit();

	}
	
	@Override
	public void onShowSelected(ArchiveShowObj show) {
		Logging.Log(LOG_TAG, "Show selected, making new ShowDetailsFragment.");
		Logging.Log(LOG_TAG, "BACK STACK COUNT: " + this.getFragmentManager().getBackStackEntryCount());
		this.instantiateShowDetailsFragmentForActivity(show);
		Logging.Log(LOG_TAG, "BACK STACK COUNT: " + this.getFragmentManager().getBackStackEntryCount());
	}
	
	@Override
	public void showLoadingDialog (String message) {
		showLoadingDialog(message, true);
	}
	
	@Override
	public void showLoadingDialog (String message, boolean useTitle) {
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
		SearchSettingsDialogFragment settingsFrag = SearchSettingsDialogFragment.newInstanceSearchSettingsDialogFragment(b.getString("type"), b.getInt("number"), b.getInt("datepos"), b.getInt("month"), b.getInt("day"), b.getInt("year"));
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
	public void showDialog(String message, String title){
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
	        	   startActivity(browserIntent);	           }
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
		LoadingDialog prev = (LoadingDialog)getSupportFragmentManager().findFragmentByTag("dialog");
	    if (prev != null) {
	    	if(prev.isRemoving()){
	    	} else{
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
		SearchFragment searchFrag = (SearchFragment)this.getSupportFragmentManager().findFragmentByTag("searchfrag");
		if(searchFrag!=null){
			searchFrag.onSettingsOkayButtonPressed(searchType, numResults, dateTypePos, month, day, year);
		}
	}

	@Override
	public void playShow(int pos, ArrayList<ArchiveSongObj> showSongs) {
		
		this.instantiateNowPlayingFragmentForActivity(pos, showSongs);
	}

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
	
	@Override
	public void browse(String artist) {
		this.instantiateSearchFragmentForActivity(artist);
	}
	
	@Override
	public void openArtistShowList(ArchiveArtistObj artist) {
		this.instantiateVotesFragmentForActivity(artist);
	}


	private class UpgradeTask extends AsyncTask<String, Integer, String> {

		private SearchScreen parentScreen;
		private boolean success = false;
		private boolean completed = false;

		private UpgradeTask(SearchScreen activity){
			this.parentScreen = activity;
		}

		protected void onPreExecute(){
			Logging.Log(LOG_TAG, "Starting UpgradeTask");
			if (db.needsUpgrade) {
				parentScreen.showDialog(UPGRADE_DB);
			}
		}

		@Override
		protected String doInBackground(String... upgradeString) {
			/*Upgrade or copy*/
			//Upgrade existing
			if (db.needsUpgrade) {
				Logging.Log(LOG_TAG, "Upgrading DB");
				success = db.upgradeDB();
				//Copy new one if failure upgrading

				if(!success){
					try {
						db.copyDB();
					} catch (IOException e) {
						throw new Error("Error copying database");
					}
				}
				//Finally open DB
				try {
					db.openDataBase();
				} catch (SQLException e) {
					Logging.Log(LOG_TAG, "Unable to open database");
					Logging.Log(LOG_TAG, e.getStackTrace().toString());
				}
				//DB is now ready to use
				db.updatePref("splashShown", "true");
				publishProgress(25);
			}

			//Fix bad shows
			ArrayList<ArchiveShowObj> shows = db.getBadShows();
			Logging.Log(LOG_TAG, "Looking for shows to fix, found "  + shows.size());
			if (shows.size() > 0) {
				Logging.Log(LOG_TAG, "Starting to fix shows");
				for (final ArchiveShowObj s : shows) {
					JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, "https://archive.org/metadata/" + s.getIdentifier(), null,
							new Response.Listener<JSONObject>() {
								@Override
								public void onResponse(JSONObject response) {
									Logging.Log(LOG_TAG, "JSON for show with bad DB entry...  Size: " + response.toString().length());
									ArrayList<ArchiveSongObj> showSongs = ShowDetailsFragment.parseShowJSON(response);
									if(!showSongs.isEmpty()){
										db.setShowExists(s);
										db.insertRecentShow(s);
									}
								}
							},
							new Response.ErrorListener() {
								@Override
								public void onErrorResponse(VolleyError error) {

								}
							});
					RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsObjRequest);

				}
				publishProgress(50);
			}

			//Update Artists if necessary
			if (needsArtistFetching()) {
				Searching.updateArtists(db);
				publishProgress(75);
			}

			Downloading.syncFilesDirectory(parentScreen, db);

			return "Completed";
		}

		protected void onPostExecute(String upgradeString) {
		}

		protected void onProgressUpdate(Integer... progress) {
			if (progress[0] == 25) {
				parentScreen.dismissDialog(UPGRADE_DB);
//				setImageButtonToFragments();
				completed=true;
				notifyActivityTaskCompleted();
			}
			if (progress[0] == 50) {
				Logging.Log(LOG_TAG, "Finished fixing shows");
			}
			if (progress[0] == 75) {
				String message = "Updated Artists";
				Toast.makeText(SearchScreen.this, message, Toast.LENGTH_SHORT).show();
			}
		}

		// The parent could be null if you changed orientations
		// and this method was called before the new SearchScreen
		// could set itself as this Thread's parent.
		private void notifyActivityTaskCompleted(){
			if(parentScreen!=null){
				parentScreen.onTaskCompleted();
			}
		}

		// When a SearchScreen is reconstructed (like after an orientation change),
		// we call this method on the retained SearchScreen (if one exists) to set
		// its parent Activity as the new SearchScreen because the old one has been destroyed.
		// This prevents leaking any of the data associated with the old SearchScreen.
		private void setActivity(SearchScreen activity){
			this.parentScreen = activity;
			if(completed){
				notifyActivityTaskCompleted();
			}
		}

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

	/** Dialog preparation method.
	 *
	 * Includes Thread bookkeeping to prevent not leaking Views on orientation changes.
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog){
		super.onPrepareDialog(id, dialog);
		if(id==UPGRADE_DB){
			dialogShown = true;
		}
	}

	/** Dialog creation method.
	 *
	 * Includes Thread bookkeeping to prevent not leaking Views on orientation changes.
	 */
	@Override
	protected Dialog onCreateDialog(int id){
		switch(id){
			case UPGRADE_DB:

				ProgressDialog dialog = new ProgressDialog(this);
				dialog.setMessage("Updating Database, ");
				return dialog;
			default:
				return super.onCreateDialog(id);
		}
	}

	private void onTaskCompleted(){
		if(dialogShown){
			try{
				dismissDialog(UPGRADE_DB);
			} catch(IllegalArgumentException e){

				e.printStackTrace();
			}

		}
	}
	
}
