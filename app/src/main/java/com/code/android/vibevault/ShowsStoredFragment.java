package com.code.android.vibevault;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class ShowsStoredFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<ArrayList<ArchiveShowObj>>,
        AdapterView.OnItemClickListener {

    private static final String LOG_TAG = ShowsStoredFragment.class.getName();

    private DialogAndNavigationListener dialogAndNavigationListener;

    private ListView storedList;

    private StaticDataStore db;

    private int stored_type = -1;

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            dialogAndNavigationListener = (DialogAndNavigationListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogListener");
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.shows_stored_fragment, container, false);
        storedList = view.findViewById(R.id.StoredListView);
        storedList.setOnItemClickListener(this);

        Toolbar topAppBar = view.findViewById(R.id.topAppBar);
        topAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.SearchActionBarButton:
                        NavHostFragment
                                .findNavController(ShowsStoredFragment.this)
                                .navigate(R.id.frag_search);
                        break;
                    case R.id.HelpButton:
                        dialogAndNavigationListener.showDialog(
                                ShowsStoredFragment.this.getResources().getString(R.string.recent_shows_screen_help),
                                "Help");
                        break;
                    case android.R.id.home:
                        dialogAndNavigationListener.goHome();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.stored_array,
                android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner = MenuItemCompat.getActionView(topAppBar.getMenu()
                .findItem(R.id.ShowsStoredSpinner))
                .findViewById(R.id.planets_spinner);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LoaderManager lm = getLoaderManager();
                Bundle bundle = new Bundle();
                bundle.putInt("storedType", position);
                lm.restartLoader(2, bundle, ShowsStoredFragment.this);

                if (position == ShowsStoredAsyncTaskLoader.STORED_RECENT_SHOWS) {
                    stored_type = ScrollingShowAdapter.MENU_RECENT;
                } else {
                    stored_type = ScrollingShowAdapter.MENU_BOOKMARK;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Bundle bundle = new Bundle();
        bundle.putSerializable("show", (ArchiveShowObj) parent.getAdapter().getItem(position));

        NavHostFragment
                .findNavController(ShowsStoredFragment.this)
                .navigate(R.id.action_menu_your_shows_to_frag_show_details, bundle);
    }

    @Override
    public Loader<ArrayList<ArchiveShowObj>> onCreateLoader(int id, Bundle args) {
        this.dialogAndNavigationListener.showLoadingDialog("Getting stored shows...");
        int storedType = args.getInt("storedType");
        Logging.Log("Stored Frag", "Created Loader");
        return new ShowsStoredAsyncTaskLoader(getActivity(), storedType);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<ArrayList<ArchiveShowObj>> loader, ArrayList<ArchiveShowObj> data) {
        Logging.Log("Stored Frag", "Loader Finished");
        this.dialogAndNavigationListener.hideDialog();

        ScrollingShowAdapter showAdapter = new ScrollingShowAdapter(getActivity(), R.id.StoredListView, data, db, stored_type);
        storedList.setAdapter(showAdapter);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<ArchiveShowObj>> arg0) {
        // TODO Auto-generated method stub
    }

}
