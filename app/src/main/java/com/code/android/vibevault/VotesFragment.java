package com.code.android.vibevault;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.ArrayList;

public class VotesFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<ArrayList<?>>,
		AdapterView.OnItemSelectedListener {
	
	protected static final String LOG_TAG = VotesFragment.class.getName();
	private DialogAndNavigationListener dialogAndNavigationListener;
	private ListView votedList;
	private ArrayList<ArchiveVoteObj> votes;
	protected Button moreButton;
	private int offset = 0;
	private int voteType = Voting.VOTES_SHOWS;
	private int voteResultType = Voting.VOTES_NEWEST_VOTED;
	private int numResults = 10;
	private int artistId = -1;
	private int currentSelectedMode = -1;
	
	private ShareActionProvider mShareActionProvider;
	private StaticDataStore db;
	
	private boolean moreResults = false;

	private Spinner spinner;

	// TODO: Kept after removing action bar
	private String actionBarTitle = "";
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if (voteType != Voting.VOTES_SHOWS_BY_ARTIST) {
			switch(position){
				case 0:
					this.voteType = Voting.VOTES_SHOWS;
					this.voteResultType = Voting.VOTES_NEWEST_VOTED;
					break;
				case 1:
					this.voteType = Voting.VOTES_SHOWS;
					this.voteResultType = Voting.VOTES_ALL_TIME;
					break;
				case 2:
					this.voteType = Voting.VOTES_SHOWS;
					this.voteResultType = Voting.VOTES_WEEKLY;
					break;
				case 3:
					this.voteType = Voting.VOTES_SHOWS;
					this.voteResultType = Voting.VOTES_DAILY;
					break;
				case 4:
					this.voteType = Voting.VOTES_ARTISTS;
					this.voteResultType = Voting.VOTES_NEWEST_VOTED;
					break;
				case 5:
					this.voteType = Voting.VOTES_ARTISTS;
					this.voteResultType = Voting.VOTES_ALL_TIME;
					break;
				case 6:
					this.voteType = Voting.VOTES_ARTISTS;
					this.voteResultType = Voting.VOTES_WEEKLY;
					break;
				case 7:
					this.voteType = Voting.VOTES_ARTISTS;
					this.voteResultType = Voting.VOTES_DAILY;
					break;
				default:
					this.voteType = Voting.VOTES_SHOWS;
					this.voteResultType = Voting.VOTES_NEWEST_VOTED;
			}
		} else {
			voteType = Voting.VOTES_SHOWS_BY_ARTIST;
			switch(position) {
				case 0:
					voteResultType = Voting.VOTES_NEWEST_VOTED;
					break;
				case 1:
					voteResultType = Voting.VOTES_ALL_TIME;
					break;
				case 2:
					voteResultType = Voting.VOTES_WEEKLY;
					break;
				case 3:
					voteResultType = Voting.VOTES_DAILY;
					break;
				default:
					voteResultType = Voting.VOTES_NEWEST_VOTED;
					break;
			}
		}
		Logging.Log(LOG_TAG, "VotesFragment: Execute Refresh from callback");
		if(currentSelectedMode!=position){
			currentSelectedMode=position;
			moreResults = false;
			offset = 0;
			executeRefresh();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
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
	}
	
	// Called when the fragment is first created.  According to the Android API,
	// "should initialize essential components of the fragment that you want
	// to retain when the fragment is paused or stopped, then resumed."
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Logging.Log(LOG_TAG, "onCreate");
		votes = new ArrayList<ArchiveVoteObj>();
		// Control whether a fragment instance is retained across Activity re-creation (such as from a configuration change).
		this.setRetainInstance(true);
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
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Logging.Log(LOG_TAG, "CREATING VIEW.");
		// Inflate the fragment and grab a reference to it.
		View view = inflater.inflate(R.layout.votes_fragment, container, false);
		this.votedList = (ListView) view.findViewById(R.id.VotesListView);
		votedList.setFooterDividersEnabled(false);

		this.moreButton = new Button(getActivity());
		this.moreButton.setText("More");
		this.moreButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		this.moreButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				offset = votedList.getCount();
				moreResults = true;
				executeRefresh();
			}
			
		});
		votedList.addFooterView(moreButton);
		moreButton.setVisibility(this.votes.size()>0?View.VISIBLE:View.GONE);		
		this.votedList.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				Object object = a.getAdapter().getItem(position);
				if(object != null){
					if(object instanceof ArchiveShowObj){
						Logging.Log(LOG_TAG, "Show selected, making new ShowDetailsFragment.");
						Bundle bundle = new Bundle();
						bundle.putSerializable("show", ((ArchiveShowObj)object));
						NavHostFragment.findNavController(VotesFragment.this)
								.navigate(R.id.frag_show_details, bundle);
					}
					else if(object instanceof ArchiveArtistObj){
						Bundle bundle = new Bundle();
						bundle.putSerializable("ArchiveArtist", ((ArchiveArtistObj)object));
						NavHostFragment.findNavController(VotesFragment.this)
								.navigate(R.id.action_menu_votes_self, bundle);
					}
				}
			}
		});

		Toolbar topAppBar = view.findViewById(R.id.topAppBar);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				getActivity(),
				R.array.voting_array,
				android.R.layout.simple_spinner_dropdown_item);

		spinner = (Spinner) topAppBar.getMenu()
				.findItem(R.id.ShowsStoredSpinner)
				.getActionView()
				.findViewById(R.id.planets_spinner);

		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(this);

		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Toolbar topAppBar = view.findViewById(R.id.topAppBar);

		NavController navController = Navigation.findNavController(view);
		AppBarConfiguration appBarConfiguration = new AppBarConfiguration
				.Builder(navController.getGraph())
				.build();
		NavigationUI.setupWithNavController(topAppBar, navController, appBarConfiguration);
	}
	
	private void executeRefresh(){
		if(voteType==-1){
			return;
		}
		LoaderManager lm = getLoaderManager();
		Bundle b = new Bundle();
		b.putIntArray("queryArray", new int[] {voteType, voteResultType, numResults, offset, artistId});
		lm.restartLoader(2, b, VotesFragment.this);
	}
	
	private void openShowsByArtist(ArchiveArtistObj artist){
		if(artist != null){
			this.voteType = Voting.VOTES_SHOWS_BY_ARTIST;
			this.voteResultType = Voting.VOTES_ALL_TIME;
			this.artistId = artist.getArtistId();

	        actionBarTitle = "Shows By Artist";
	        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	        		getActivity(),
					R.array.voting_by_artist,
					android.R.layout.simple_spinner_dropdown_item);

			spinner.setAdapter(adapter);
			spinner.setOnItemSelectedListener(this);
		}
	}
	
	// This method is called right after onCreateView() is called. "Called when the
	// fragment's activity has been created and this fragment's view hierarchy instantiated."
	// http://developer.android.com/guide/topics/fundamentals/fragments.html#Lifecycle
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Must call in order to get callback to onOptionsItemSelected()
		setHasOptionsMenu(true);
		AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        actionBarTitle = "Top Voted";

		Bundle b = new Bundle();
		b.putIntArray("queryArray", new int[] {this.voteType, this.voteResultType, numResults, this.offset, artistId});
		
		if(voteType == -1){
			return;
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Loader<ArrayList<?>> onCreateLoader(int id, Bundle args) {
		this.dialogAndNavigationListener.showLoadingDialog("Getting votes...");
		int[] queryVals = args.getIntArray("queryArray");
		return (Loader) new VotesQueryAsyncTaskLoader(getActivity(), queryVals[0], queryVals[1], queryVals[2], queryVals[3], queryVals[4]);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onLoadFinished(Loader<ArrayList<?>> loader, ArrayList<?> data) {
		this.dialogAndNavigationListener.hideDialog();
		Logging.Log(LOG_TAG, "MORERESULTS: " + moreResults);
		if (moreResults) {
			Parcelable state = this.votedList.onSaveInstanceState();
			this.votes.addAll((ArrayList<ArchiveVoteObj>)data);
			this.refreshVoteList();
			this.votedList.onRestoreInstanceState(state);
			moreResults=false;
		} else {
			this.votes = (ArrayList<ArchiveVoteObj>) data;
			this.refreshVoteList();

		}

		if (voteType == Voting.VOTES_SHOWS_BY_ARTIST && actionBarTitle.equals("Top Voted")) {
	        actionBarTitle = "Shows By Artist";
	        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	        		getActivity(),
					R.array.voting_by_artist,
					android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter);
			spinner.setOnItemSelectedListener(this);
		} else if (voteType != Voting.VOTES_SHOWS_BY_ARTIST && actionBarTitle.equals("Shows By Artist")) {
	        actionBarTitle = "Top Voted";
	        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	        		getActivity(),
					R.array.voting_array,
					android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter);
			spinner.setOnItemSelectedListener(this);
		}
		moreButton.setVisibility(this.votes.size()>0?View.VISIBLE:View.GONE);

	}
	
	@Override
	public void onResume() {
		super.onResume();
		this.refreshVoteList();
		if(this.getArguments()!=null&&this.getArguments().containsKey("ArchiveArtist")){
			ArchiveArtistObj artist = (ArchiveArtistObj)this.getArguments().get("ArchiveArtist");
			if(artist!=null){
				this.openShowsByArtist(artist);
				this.getArguments().remove("ArchiveArtist");
			}
		}
	}

	@Override	
	public void onPause() {
		super.onPause();
	}
	
	private void refreshVoteList(){
		this.votedList.setAdapter(new VoteAdapter(getActivity(), R.layout.voted_show_list_row, votes));

	}

	@Override
	public void onLoaderReset(Loader<ArrayList<?>> arg0) {
		// TODO Auto-generated method stub
		
	}
	
	private class VoteAdapter extends ArrayAdapter<ArchiveVoteObj> {
				
		public VoteAdapter(Context context, int textViewResourceId, ArrayList<ArchiveVoteObj> votes){
			super(context, textViewResourceId, votes);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Object voteObject = getItem(position);
			if(voteObject instanceof ArchiveArtistObj){
				if (convertView == null) {
					LayoutInflater vi = (LayoutInflater) getActivity()
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = vi.inflate(R.layout.voted_show_list_row, null);
				}
				
				TextView artistText = (TextView) convertView.findViewById(R.id.ArtistText);
				TextView showText = (TextView) convertView.findViewById(R.id.ShowText);
				TextView votesText = (TextView) convertView.findViewById(R.id.RatingText);
				artistText.setVisibility(View.VISIBLE);
				votesText.setVisibility(View.VISIBLE);
				artistText.setText((ArchiveArtistObj)voteObject + " ");
				artistText.setSelected(true);
				showText.setText("Last voted: " + ((ArchiveArtistObj)voteObject).getVoteTime());
				votesText.setText("Votes: " + ((ArchiveArtistObj)voteObject).getVotes() + " ");
				return convertView;
			} else if(voteObject instanceof ArchiveShowObj){
				final ArchiveShowObj show = (ArchiveShowObj) voteObject;
				if (convertView == null) {
					LayoutInflater vi = (LayoutInflater) getActivity()
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = vi.inflate(R.layout.voted_show_list_row, null);
				}
				TextView artistText = (TextView) convertView.findViewById(R.id.ArtistText);
				TextView showText = (TextView) convertView.findViewById(R.id.ShowText);
				TextView votesText = (TextView) convertView.findViewById(R.id.RatingText);
				final ImageView menuIcon = (ImageView) convertView.findViewById(R.id.MenuIcon);
				final PopupMenu menu = new PopupMenu(this.getContext(), menuIcon);
				artistText.setVisibility(View.VISIBLE);
				votesText.setVisibility(View.VISIBLE);
				artistText.setText(show.getShowArtist());
				artistText.setSelected(true);
				showText.setText(show.getShowTitle());
				showText.setSelected(true);
				votesText.setText("Votes: " + show.getVotes() + " ");
				artistText.setSelected(true);
				artistText.setMarqueeRepeatLimit(-1);
				artistText.setSingleLine();
				artistText.setHorizontallyScrolling(true);
				showText.setSelected(true);
				showText.setMarqueeRepeatLimit(-1);
				showText.setSingleLine();
				showText.setHorizontallyScrolling(true);
				
				menuIcon.setVisibility(View.VISIBLE);
				
				if(db.getShowExists(show)){
					int status = db.getShowDownloadStatus(show);
					menu.getMenuInflater().inflate(R.menu.show_options_menu, menu.getMenu());
					if(status == StaticDataStore.SHOW_STATUS_NOT_DOWNLOADED){
						menu.getMenu().add(Menu.NONE, 100, Menu.NONE, "Download show");
					} else if(status == StaticDataStore.SHOW_STATUS_FULLY_DOWNLOADED){
						menu.getMenu().add(Menu.NONE, 101, Menu.NONE, "Delete show");
					} else{
						menu.getMenu().add(Menu.NONE, 100, Menu.NONE, "Download remaining");
						menu.getMenu().add(Menu.NONE, 101, Menu.NONE, "Delete downloaded");
					}
				}				
				
				menuIcon.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						
						menu.setOnMenuItemClickListener(new OnMenuItemClickListener(){
							@Override
							public boolean onMenuItemClick(MenuItem item) {
								Intent i = new Intent(Intent.ACTION_SEND);
								i.setType("text/plain");
								// Add data to the intent, the receiving app will decide what to do with it.
								i.putExtra(Intent.EXTRA_SUBJECT, "Vibe Vault");
								i.putExtra(Intent.EXTRA_TEXT, show.getShowArtist() + " " + show.getShowTitle() + " " + show.getShowURL()
										+ "\n\nSent using #VibeVault for Android.");
								MenuItem share = menu.getMenu().findItem(R.id.ShareButton);
							    mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(share);
								if(mShareActionProvider!=null){
									mShareActionProvider.setShareIntent(i);
								}
								switch (item.getItemId()) {
								case (100):
									DownloadingAsyncTask task = new DownloadingAsyncTask(getActivity());
									ArrayList<ArchiveSongObj> songs = db.getSongsFromShow(show.getIdentifier());
									task.execute(songs.toArray(new ArchiveSongObj[songs.size()]));
									break;
									case (R.id.AddButton):
										Intent intent = new Intent(PlaybackService.ACTION_QUEUE_SHOW);
										intent.putExtra(PlaybackService.EXTRA_DO_PLAY, false);
										getContext().startService(intent);
										break;
									case (101):
										if(Downloading.deleteShow(getContext(), show, db)){
											Toast.makeText(getContext(), R.string.confirm_songs_show_deleted_message_text, Toast.LENGTH_SHORT).show();
										} else{
											Toast.makeText(getContext(), R.string.error_songs_show_not_deleted_message_text, Toast.LENGTH_SHORT).show();
										}
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
			} else{
				if (convertView == null) {
					LayoutInflater vi = (LayoutInflater) getActivity()
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = vi.inflate(R.layout.voted_show_list_row, null);
				}
				return convertView;
			}			
		}		
	}
}