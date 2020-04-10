package com.code.android.vibevault;

import java.util.ArrayList;

import com.code.android.vibevault.SearchFragment.SearchActionListener;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ShowsStoredFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<ArrayList<ArchiveShowObj>>,
		AdapterView.OnItemClickListener, ActionBar.OnNavigationListener {
	
	private static final String LOG_TAG = ShowsStoredFragment.class.getName();

	private DialogAndNavigationListener dialogAndNavigationListener;

	private ListView storedList;

	private StaticDataStore db;

	private SearchActionListener searchActionListener;
	
	private int stored_type = -1;

	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			dialogAndNavigationListener = (DialogAndNavigationListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement DialogListener");
		}
		try {
			searchActionListener = (SearchActionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement ActionListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.shows_stored_fragment, container,
				false);
		storedList = (ListView) v.findViewById(R.id.StoredListView);
		storedList.setOnItemClickListener(this);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setHasOptionsMenu(true);
		AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
//		appCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		appCompatActivity.getSupportActionBar().setTitle("Your Shows");
		Logging.Log(LOG_TAG, "ACTION MODE: " + appCompatActivity.getSupportActionBar().getNavigationMode());
		appCompatActivity.getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				appCompatActivity, R.array.stored_array,
				android.R.layout.simple_spinner_dropdown_item);
		appCompatActivity.getSupportActionBar().setListNavigationCallbacks(adapter, this);
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		LoaderManager lm = getLoaderManager();
		Bundle b = new Bundle();
		b.putInt("storedType", itemPosition);
		lm.restartLoader(2, b, ShowsStoredFragment.this);
		stored_type = (itemPosition == ShowsStoredAsyncTaskLoader.STORED_RECENT_SHOWS ?
				ScrollingShowAdapter.MENU_RECENT : ScrollingShowAdapter.MENU_BOOKMARK);
				
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.HelpButton:
			dialogAndNavigationListener.showDialog(
					this.getResources().getString(R.string.recent_shows_screen_help),
					"Help");
			break;
		case android.R.id.home:
			dialogAndNavigationListener.goHome();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (!menu.hasVisibleItems()) {
			inflater.inflate(R.menu.help, menu);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		searchActionListener.onShowSelected((ArchiveShowObj) parent.getAdapter().getItem(position));
	}

	@Override
	public Loader<ArrayList<ArchiveShowObj>> onCreateLoader(int id, Bundle args) {
		this.dialogAndNavigationListener.showLoadingDialog("Getting stored shows...");
		int storedType = args.getInt("storedType");
		Logging.Log("Stored Frag", "Created Loader");
		return new ShowsStoredAsyncTaskLoader(getActivity(), storedType);
	}

	@Override
	public void onLoadFinished(@NonNull Loader<ArrayList<ArchiveShowObj>> loader,
			ArrayList<ArchiveShowObj> data) {
		Logging.Log("Stored Frag", "Loader Finished");
		this.dialogAndNavigationListener.hideDialog();

		ScrollingShowAdapter showAdapter = new ScrollingShowAdapter(
				getActivity(), R.id.StoredListView, data, db, stored_type);
		storedList.setAdapter(showAdapter);
	}

	@Override
	public void onLoaderReset(Loader<ArrayList<ArchiveShowObj>> arg0) {
		// TODO Auto-generated method stub

	}

}
