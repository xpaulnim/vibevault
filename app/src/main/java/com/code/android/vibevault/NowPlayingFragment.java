/*
 * NowPlayingScreen.java
 * VERSION 3.1
 * 
 * Copyright 2011 Andrew Pearson and Sanders DeNardi.
 * 
 * This file is part of Vibe Vault.
 * 
 * Vibe Vault is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 */

package com.code.android.vibevault;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Locale;

public class NowPlayingFragment extends Fragment {
	
	private static final String LOG_TAG = NowPlayingFragment.class.getName();

	private static final int MENU_REMOVE = 0;
	
	private static final String TIME_FORMAT = String.format(Locale.getDefault(),"%%0%dd", 2);  
	
	private BroadcastReceiver stateChangedReceiver = new StateChangeReceiver();
	private BroadcastReceiver positionChangedReceiver = new PositionChangeReceiver();
	
    // private Vibrator vibrator;
	
	private TextView nowPlayingTextView;
	
	private MaterialButton previous;
	private MaterialButton stop;
	private MaterialButton pause;
	private MaterialButton next;
	
	private SeekBar progressBar;
	private TextView timeCurrent;
	private TextView timeTotal;

	private DraggableListView songsListView;
	private PlaylistAdapter adapter = null;
			
	private PlayerListener playerListener;
	
	private ShareActionProvider mShareActionProvider;
	
	private DialogAndNavigationListener dialogAndNavigationListener;
	
	private Parcelable draggableListViewState = null;
	
	private StaticDataStore db;
	
	private int currentPos = -1;
	
	private String nowPlaying = "";

	private boolean newExternalPositionPlayed = false;
	
	public interface PlayerListener {
		public void registerReceivers(BroadcastReceiver stateChangedBroadcast, BroadcastReceiver positionChangedBroadcast);
		public void unregisterReceivers(BroadcastReceiver stateChangedBroadcast, BroadcastReceiver positionChangedBroadcast);
	}
	
	// Called right before onCreate(), which is right before onCreateView().
	// http://developer.android.com/guide/topics/fundamentals/fragments.html#Lifecycle
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try{
			dialogAndNavigationListener = (DialogAndNavigationListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement DialogListener");
		}
		try {
			playerListener = (PlayerListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement PlayerListener");
		}
	}
	
	// This method is called right after onCreateView() is called. "Called when the
	// fragment's activity has been created and this fragment's view hierarchy instantiated."
	// http://developer.android.com/guide/topics/fundamentals/fragments.html#Lifecycle
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// If this ShowDetailsFragment has arguments (a passed show), grab it and parse it.
		if (this.getArguments() != null) {
			this.attachToPlaybackService();
		}
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
		// Inflate the fragment and grab a reference to it.
		View view = inflater.inflate(R.layout.now_playing, container, false);
		
		// Initialize the interface.
		previous = (MaterialButton) view.findViewById(R.id.PrevButton);
		stop = (MaterialButton) view.findViewById(R.id.StopButton);
		pause = (MaterialButton) view.findViewById(R.id.PauseButton);
		next = (MaterialButton) view.findViewById(R.id.NextButton);
		progressBar = (SeekBar) view.findViewById(R.id.SeekBarNowPlaying);
		timeCurrent = (TextView) view.findViewById(R.id.TimeCurrent);
		timeTotal = (TextView) view.findViewById(R.id.TimeTotal);
		nowPlayingTextView = (TextView) view.findViewById(R.id.PlayingLabelTextView);

		// Initialize the DraggableListView of songs, settings listeners for clicking,
		// long-pressing, dragging, and removing.
		songsListView = (DraggableListView) view.findViewById(R.id.PlayListListView);
		songsListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				Intent intent = defaultPlaybackServiceIntent()
						.setAction(PlaybackService.ACTION_PLAY_POSITION);
				intent.putExtra(PlaybackService.EXTRA_PLAYLIST_POSITION, position);
				v.getContext().startService(intent);
			}
		});
//		songsListView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
//			@Override
//			public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
//				menu.add(Menu.NONE, MENU_REMOVE, Menu.NONE, "Remove from playlist");
//			}
//		});
		songsListView.setDragListener(new DraggableListView.DragListener() {
			@Override
			public void drag(int from, int to) {
				// It is not necessary to do anything with "from" and "to", because the action happens when
				// the song is actually "dropped" in the setDropListene.  We save the instance state, however,
				// because this allows us to maintain the ListView's position on refreshTrackList() calls where
				// the ListView is redrawn.
				draggableListViewState = songsListView.onSaveInstanceState();
			}
		});
		songsListView.setDropListener(new DraggableListView.DropListener() {
			@Override
			public void drop(int from, int to) {
				Intent intent = defaultPlaybackServiceIntent()
						.setAction(PlaybackService.ACTION_MOVE)
						.putExtra(PlaybackService.EXTRA_MOVE_FROM, from)
						.putExtra(PlaybackService.EXTRA_MOVE_TO, to);
				NowPlayingFragment.this.getActivity().startService(intent);
			}
		});
		songsListView.setRemoveListener(new DraggableListView.RemoveListener() {
			@Override
			public void remove(int which) {
				Intent intent = defaultPlaybackServiceIntent()
						.setAction(PlaybackService.ACTION_DELETE)
						.putExtra(PlaybackService.EXTRA_PLAYLIST_POSITION, which);
				NowPlayingFragment.this.getActivity().startService(intent);
			}
		});
		
		initPlayControls();
//		songsListView.setBackgroundColor(Color.BLACK);

		Toolbar topAppBar = view.findViewById(R.id.topAppBar);
		topAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
					case R.id.SearchActionBarButton:
						NavHostFragment
								.findNavController(NowPlayingFragment.this)
								.navigate(R.id.frag_search);
						return true;
					case R.id.VoteButton:
						vote();
						return true;
					case R.id.HelpButton:
						dialogAndNavigationListener.showDialog(NowPlayingFragment.this.getResources().getString(R.string.now_playing_screen_help), "Help");
						return true;
					case android.R.id.home:
						dialogAndNavigationListener.goHome();
						return true;
					case R.id.DownloadButton:
						NowPlayingFragment.this.getActivity().startService(defaultPlaybackServiceIntent().setAction(PlaybackService.ACTION_DOWNLOAD));
						return true;
					case R.id.BookmarkButton:
						if(currentPos>=0&&currentPos<NowPlayingFragment.this.adapter.getCount()){
							ArchiveSongObj voteSong = NowPlayingFragment.this.adapter.getItem(currentPos);
							if(voteSong!=null){
								ArchiveShowObj bookmarkShow = db.getShow(voteSong.getShowIdentifier());
								if(bookmarkShow!=null){
									db.openDataBase();
									db.insertFavoriteShow(bookmarkShow);
									db.close();
									Toast.makeText(getActivity(), R.string.confirm_bookmarked_message_text, Toast.LENGTH_SHORT).show();
									return true;
								}
							}
						}
						Toast.makeText(getActivity().getBaseContext(), R.string.error_no_song_bookmark_message_text, Toast.LENGTH_SHORT).show();
						return false;
					default:
						return false;
				}
			}
		});

		MenuItem item = topAppBar.getMenu().findItem(R.id.ShareButton);
		mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}
	
	
	// Initialize the controls for the player.
	private void initPlayControls(){
		this.previous.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				v.getContext().startService(defaultPlaybackServiceIntent().setAction(PlaybackService.ACTION_PREV));
				if(currentPos < songsListView.getFirstVisiblePosition() || currentPos > songsListView.getLastVisiblePosition()){
					if(currentPos <= 0){
						songsListView.setSelection(0);
					} else{
						songsListView.setSelection(currentPos-1);
					}
					Logging.Log(LOG_TAG, "Selection set to: " + songsListView.getSelectedItemPosition());
					draggableListViewState = songsListView.onSaveInstanceState();
				}
			}
		});
		this.stop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				v.getContext().startService(defaultPlaybackServiceIntent().setAction(PlaybackService.ACTION_STOP));
			}
		});

		this.pause.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				view.getContext().startService(defaultPlaybackServiceIntent().setAction(PlaybackService.ACTION_TOGGLE));
			}
		});

		this.next.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				v.getContext().startService(defaultPlaybackServiceIntent().setAction(PlaybackService.ACTION_NEXT));
				if(currentPos < songsListView.getFirstVisiblePosition() || currentPos > songsListView.getLastVisiblePosition()){
					if(currentPos <= 0){
						songsListView.setSelection(currentPos);
					} else{ 
						songsListView.setSelection(currentPos-1);
					}
					Logging.Log(LOG_TAG, "Selection set to: " + songsListView.getSelectedItemPosition());
					draggableListViewState = songsListView.onSaveInstanceState();
				}
			}			
		});
		
		progressBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					Intent intent = defaultPlaybackServiceIntent()
							.setAction(PlaybackService.ACTION_SEEK)
							.putExtra(PlaybackService.EXTRA_SEEK_POSITON, progress);
					NowPlayingFragment.this.getActivity().startService(intent);
				}
			}
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
			}
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
			}
		});
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
//		if(getActivity().getFragmentManager().getBackStackEntryCount()<1){
//			getActivity().finish();
//		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		db = StaticDataStore.getInstance(getActivity());
		checkAndUpdatePlayerSongs();
		this.newExternalPositionPlayed=true;
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}

	private Intent defaultPlaybackServiceIntent(){
		return new Intent(getActivity(), PlaybackService.class);
	}
	
	private void refreshTrackList(ArrayList<ArchiveSongObj> list){
		Logging.Log(LOG_TAG, "Refresh called...");

		adapter = new PlaylistAdapter(getActivity(), R.layout.playlist_row, list);
		songsListView.setAdapter(adapter);

		if(draggableListViewState!=null){
			this.songsListView.onRestoreInstanceState(draggableListViewState);
		}
		this.updateShareIntent();
		// FIXME
		// There seems to be some sort of race condition issue here.  If we don't wait,
		// we are unable to play songs, because
		if(newExternalPositionPlayed && !adapter.isEmpty()) {
			Logging.Log(LOG_TAG, "newExternalPositionPlayed is set to true, adapter has songs.");
			songsListView.postDelayed(new Runnable() {
				@Override
				public void run() {
					Logging.Log(LOG_TAG, "currentPos is: " + currentPos);
					if(currentPos <= 0){
						songsListView.smoothScrollToPosition(0);
					} else {
						int scrollPosition = currentPos+songsListView.getChildCount()-2;
						Logging.Log(LOG_TAG, "Scroll position: " + scrollPosition);
						Logging.Log(LOG_TAG, "Adapter size: " + adapter.getCount());
						Logging.Log(LOG_TAG, "Number of items in ListView: " + songsListView.getChildCount());
						scrollPosition = scrollPosition>=adapter.getCount()?adapter.getCount()-1:scrollPosition;
						Logging.Log(LOG_TAG, "Scrolling to: " + (scrollPosition));
						songsListView.smoothScrollToPosition(scrollPosition);
					}
				}
			}, 1000);
			newExternalPositionPlayed = false;			
		}
//		songsListView.setSelection(currentPos);
	}

	@Override
	public void onResume() {
		super.onResume();
		attachToPlaybackService();
		updateShareIntent();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		detachFromPlaybackService();
		songsListView.setAdapter(null);
	}

	private void attachToPlaybackService() {
		playerListener.registerReceivers(stateChangedReceiver, positionChangedReceiver);
		this.getActivity().startService(defaultPlaybackServiceIntent().setAction(PlaybackService.ACTION_POLL));
		Logging.Log(LOG_TAG, "Attached to Playback Service");
	}

	private void detachFromPlaybackService() {
		playerListener.unregisterReceivers(stateChangedReceiver, positionChangedReceiver);
	}
	
	private void checkAndUpdatePlayerSongs(){
		if(this.getArguments()!=null){
			if(this.getArguments().containsKey("position")){
				currentPos = this.getArguments().getInt("position");
				Logging.Log(LOG_TAG, "Position passed in Intent... " + currentPos);
				this.getArguments().remove("position");
				this.newExternalPositionPlayed=true;
			}
			ArrayList<ArchiveSongObj>showSongs = (ArrayList<ArchiveSongObj>)this.getArguments().getSerializable("showsongs");
			if(showSongs!=null){
				Intent intent = defaultPlaybackServiceIntent()
						.setAction(PlaybackService.ACTION_QUEUE_SHOW)
						.putExtra(PlaybackService.EXTRA_PLAYLIST, showSongs)
						.putExtra(PlaybackService.EXTRA_PLAYLIST_POSITION, currentPos)
						.putExtra(PlaybackService.EXTRA_DO_PLAY, true);
				NowPlayingFragment.this.getActivity().startService(intent);
				this.getArguments().remove("showsongs");
			}
		}
	}
	
	private class StateChangeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Logging.Log(LOG_TAG, "StateChangeReceiver...");
			int pauseIcon = R.drawable.ic_pause_24px;
			
			int status = intent.getIntExtra(PlaybackService.EXTRA_STATUS, PlaybackService.STATUS_STOPPED);
			switch(status) {
				case PlaybackService.STATUS_BUFFERING:
					nowPlaying = "Buffering...";
					progressBar.setEnabled(false);
					break;
				case PlaybackService.STATUS_PAUSED:
					pauseIcon = R.drawable.ic_play_arrow_24px;
					progressBar.setEnabled(true);
					nowPlaying = intent.getStringExtra(PlaybackService.EXTRA_TITLE);
					break;
				case PlaybackService.STATUS_PLAYING:
					progressBar.setEnabled(true);
					nowPlaying = intent.getStringExtra(PlaybackService.EXTRA_TITLE);
					break;
				case PlaybackService.STATUS_STOPPED:
					nowPlaying = "";
					pauseIcon = R.drawable.ic_play_arrow_24px;
					progressBar.setEnabled(false);
					break;
			}
			
			progressBar.setMax(intent.getIntExtra(PlaybackService.EXTRA_PLAY_DURATION, 0));
			progressBar.setProgress(intent.getIntExtra(PlaybackService.EXTRA_PLAY_PROGRESS, 0));
			progressBar.setSecondaryProgress(intent.getIntExtra(PlaybackService.EXTRA_BUFFER_PROGRESS, 0));
			timeCurrent.setText(getElapsedTimeHoursMinutesSecondsString(intent.getIntExtra(PlaybackService.EXTRA_PLAY_PROGRESS, 0)));
			timeTotal.setText(getElapsedTimeHoursMinutesSecondsString(intent.getIntExtra(PlaybackService.EXTRA_PLAY_DURATION, 0)));
			ArrayList<ArchiveSongObj> songs = (ArrayList<ArchiveSongObj>) intent.getSerializableExtra(PlaybackService.EXTRA_PLAYLIST);
			currentPos = intent.getIntExtra(PlaybackService.EXTRA_PLAYLIST_POSITION, 0);
			draggableListViewState = songsListView.onSaveInstanceState();
			refreshTrackList(songs);
			
			if(!nowPlayingTextView.getText().equals(nowPlaying)){
				nowPlayingTextView.setText(nowPlaying);
				nowPlayingTextView.setSelected(true);
				nowPlayingTextView.setMarqueeRepeatLimit(-1);
				nowPlayingTextView.setSingleLine();
				nowPlayingTextView.setHorizontallyScrolling(true);
			}

			pause.setIconResource(pauseIcon);
		}
	}
	
	private class PositionChangeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			int status = intent.getIntExtra(PlaybackService.EXTRA_STATUS, PlaybackService.STATUS_STOPPED);
			if (status == PlaybackService.STATUS_PLAYING || status == PlaybackService.STATUS_PAUSED) {
				progressBar.setMax(intent.getIntExtra(PlaybackService.EXTRA_PLAY_DURATION, 0));
				progressBar.setProgress(intent.getIntExtra(PlaybackService.EXTRA_PLAY_PROGRESS, 0));
				progressBar.setSecondaryProgress(intent.getIntExtra(PlaybackService.EXTRA_BUFFER_PROGRESS, 0));
				timeCurrent.setText(getElapsedTimeHoursMinutesSecondsString(intent.getIntExtra(PlaybackService.EXTRA_PLAY_PROGRESS, 0)));
				timeTotal.setText(getElapsedTimeHoursMinutesSecondsString(intent.getIntExtra(PlaybackService.EXTRA_PLAY_DURATION, 0)));
			}
		}
	}

	private void updateShareIntent(){
		Logging.Log(LOG_TAG, "Updating Intent for sharing... method called..");
		if(songsListView!=null&&songsListView.getAdapter()!=null && !songsListView.getAdapter().isEmpty()){
			Logging.Log(LOG_TAG,"Updating Intent for sharing... songsListView is not null, and has an adapter...");
			ArchiveSongObj song = (ArchiveSongObj)this.songsListView.getAdapter().getItem(this.currentPos>=0&&this.currentPos<this.songsListView.getAdapter().getCount()?this.currentPos:0);
			if(song!=null){
				Logging.Log(LOG_TAG, "Song is not null, setting new Intent for sharing.");
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				// Add data to the intent, the receiving app will decide what to do with it.
				intent.putExtra(Intent.EXTRA_SUBJECT, "Vibe Vault");
				intent.putExtra(Intent.EXTRA_TEXT, "Listen to " + song.getSongTitle() + " by " + song.getShowArtist() + ": " + song.getLowBitRate()
						+ "\n\nSent using #VibeVault for Android.");
				if(mShareActionProvider!=null){
					mShareActionProvider.setShareIntent(intent);
				}
			}
		}
		
	}
	
	public String getElapsedTimeHoursMinutesSecondsString(int msec) {
	    msec = msec / 1000;
	    return String.format(TIME_FORMAT, (msec % 3600) / 60) + ":" + String.format(TIME_FORMAT, msec % 60);
	}
	
	private class PlaylistAdapter extends ArrayAdapter<ArchiveSongObj> {

		private final static int DELETE_SONG = 102;
		private final static int DELETE_SHOW = 103;
		
		public PlaylistAdapter(Context context, int textViewResourceId, ArrayList<ArchiveSongObj> objects){
			super(context,textViewResourceId, objects);
		}
		
		@Override 
		public View getView(int position, View convertView, @NonNull ViewGroup parent){
			final ArchiveSongObj song = this.getItem(position);
			if(convertView==null){
				LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.playlist_row, null);
			}
			final TextView songText = (TextView) convertView.findViewById(R.id.SongTitle);
			final TextView artistText = (TextView) convertView.findViewById(R.id.ArtistTitle);
			final ImageView menuIcon = (ImageView) convertView.findViewById(R.id.menuIcon);
			final PopupMenu menu = new PopupMenu(getActivity(), menuIcon);
			// If we have an ArchiveSongObject for this View's position, set the data to display.
			if(song != null){
				songText.setText(song.toString());
				artistText.setText(song.getShowArtist());
				if(position == currentPos){
					// TODO: Use better color
					songText.setTextColor(getResources().getColor(R.color.colorPrimary));
					artistText.setTextColor(getResources().getColor(R.color.colorPrimary));
				}
				else{
//					songText.setTextColor(Color.rgb(18, 125, 212));
//					artistText.setTextColor(Color.WHITE);
				}
			}
			artistText.setSelected(true);
			songText.setSelected(true);
			artistText.setMarqueeRepeatLimit(-1);
			artistText.setSingleLine();
			artistText.setHorizontallyScrolling(true);
			songText.setMarqueeRepeatLimit(-1);
			songText.setSingleLine();
			songText.setHorizontallyScrolling(true);
			menuIcon.setVisibility(View.VISIBLE);
			// Set up the PopupMenu for the View.
			menuIcon.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					// Putting the inflation code here speeds up the getView() method, because
					// inflation only occurs when the user clicks to see the PopupMenu.
					menu.getMenuInflater().inflate(R.menu.song_options_menu, menu.getMenu());
					if (db.songIsDownloaded(song.getFileName())) {
						menu.getMenu().add(Menu.NONE, DELETE_SONG, Menu.NONE, "Delete song");
					} else {
						menu.getMenu().add(Menu.NONE, DELETE_SHOW, Menu.NONE, "Download song");
					}
					menu.getMenu().removeItem(R.id.AddButton);
					menu.setOnMenuItemClickListener(new OnMenuItemClickListener(){
						@Override
						public boolean onMenuItemClick(MenuItem item) {
							updateShareIntent();
							switch (item.getItemId()) {
								case (R.id.DownloadButton):
									Logging.Log(LOG_TAG,"Downloading " + song.getSongTitle());
									DownloadingAsyncTask task = new DownloadingAsyncTask(getActivity());
									task.execute(song);
									break;
//								case (R.id.AddButton):
//									Intent intent = new Intent(PlaybackService.ACTION_QUEUE_SONG);
//									intent.putExtra(PlaybackService.EXTRA_SONG, song);
//									intent.putExtra(PlaybackService.EXTRA_DO_PLAY, false);
//									getActivity().startService(intent);
//									break;
								case (DELETE_SONG):
									if(Downloading.deleteSong(getContext(), song, db)){
										Toast.makeText(getContext(), R.string.confirm_song_deleted_message_text, Toast.LENGTH_SHORT).show();
									} else{
										Toast.makeText(getContext(), R.string.error_song_not_deleted_message_text, Toast.LENGTH_SHORT).show();
									}
									break;
								case (DELETE_SHOW):
									DownloadingAsyncTask deletionTask = new DownloadingAsyncTask(getActivity());
									deletionTask.execute(song);
									break;
								default:
									return false;
								}
							return true;
						}
					});
					menu.show();
				}
			});
			return convertView;
		}
	}

	public boolean onContextItemSelected(MenuItem item){
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo)item.getMenuInfo();
		if(menuInfo!=null){
			switch(item.getItemId()){
				case(MENU_REMOVE):
					Intent intent = defaultPlaybackServiceIntent()
							.setAction(PlaybackService.ACTION_DELETE)
							.putExtra(PlaybackService.EXTRA_PLAYLIST_POSITION, menuInfo.position);
					this.getActivity().startService(intent);
					break;
				default:
					return false;
			}
			return true;
		}
		return false;
	}
	
	private void vote(){
		if(currentPos>=0&&currentPos<this.adapter.getCount()){
			ArchiveSongObj voteSong = this.adapter.getItem(currentPos);
			if(voteSong!=null){
				VoteTask t = new VoteTask();
				String showDate = voteSong.getShowTitle();
				try{
					// FIXME Is it a bad idea that this is hardcoded?
					// 10 because 4 chars for year, 2 for month, 2 for day, and 2 hyphens.
					showDate = showDate.substring(showDate.length()-10);
				} catch(IndexOutOfBoundsException e){
					showDate = "";
				}
				t.execute(voteSong.getShowIdentifier(),voteSong.getShowArtist(),voteSong.getShowTitle(),showDate);
				return;
			}
		}
		Toast.makeText(getActivity().getBaseContext(), R.string.error_no_song_vote_message_text, Toast.LENGTH_SHORT).show();
	}
	
	private class VoteTask extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			Toast.makeText(getActivity().getBaseContext(), R.string.confirm_voting_message_text, Toast.LENGTH_SHORT).show();
		}

		@Override
		protected String doInBackground(String... showFields) {
			return Voting.vote(showFields[0], showFields[1], showFields[2], showFields[3], db, getActivity().getApplicationContext());
		}

		@Override
		protected void onPostExecute(String result) {
			Toast.makeText(getActivity().getBaseContext(), result, Toast.LENGTH_SHORT).show();
		}
	}
}