/*
 * SearchFragment.java
 * VERSION X.
 *
 * Copyright 2012 Andrew Pearson and Sanders DeNardi.
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

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.code.android.vibevault.SearchSettingsDialogFragment.SearchSettingsDialogInterface;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements SearchSettingsDialogInterface {

    private static final String LOG_TAG = SearchFragment.class.getName();

    private static final int MENU_INFO = 0;
    private static final int MENU_BOOKMARK = 1;
    private static final int MENU_EMAIL = 2;

    private ListView searchList;
    private File appRootDir;
    private TextView labelText;

    private ImageButton artistSearchButton;
    private AutoCompleteTextView artistSearchInput;
    private Button searchMoreButton;
    private TextView searchHintTextView;
    private Menu actionBarMenu;

    private int pageNum;
    private boolean isSearchMore = false;
    private boolean dateChanged = false;
    private ArrayList<ArchiveShowObj> searchResults;
    private String artistSearchText;
    private int dateSearchModifierPos;
    private int numResultsPref;
    private String sortPref;
    private int month;
    private int day;
    private int year;

    private StaticDataStore db;

    // Listeners for dialogs and for triggering a new Activity/Fragment when a
    // user clicks on a show.
    private DialogAndNavigationListener dialogAndNavigationListener;

    private Toolbar topAppBar;

    /**
     * Overriden fragment lifecycle methods
     */

    // Ensures parent activity implements proper interfaces.
    // Called right before onCreate(), which is right before onCreateView().
    // http://developer.android.com/guide/topics/fundamentals/fragments.html#Lifecycle
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            dialogAndNavigationListener = (DialogAndNavigationListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement DialogListener");
        }
    }

    // Called when the fragment is first created.  According to the Android API,
    // "should initialize essential components of the fragment that you want
    // to retain when the fragment is paused or stopped, then resumed."
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logging.Log(LOG_TAG, "onCreate");
        searchResults = new ArrayList<>();
        artistSearchText = "";
        month = 0;
        day = 0;
        year = 0;
        dateSearchModifierPos = 0;
        pageNum = 1;
        // Control whether a fragment instance is retained across Activity re-creation (such as from a configuration change).
        this.setRetainInstance(true);
        setHasOptionsMenu(true);
        // Create the directory for our app if it don't exist.
        // FIXME should we move this elsewhere?  Maybe to the home screen?
//		appRootDir = new File(Environment.getExternalStorageDirectory(),Downloading.APP_DIRECTORY);
//		if (!appRootDir.isFile() || !appRootDir.isDirectory()) {
//			if (Environment.getExternalStorageState().equals(
//					Environment.MEDIA_MOUNTED)) {
//				appRootDir.mkdirs();
//			} else {
//				Toast.makeText(
//						getActivity(),
//						"sdcard is unwritable...  is it mounted on the computer?",
//						Toast.LENGTH_SHORT).show();
//			}
//		}

        db = StaticDataStore.getInstance(getActivity());
        Logging.Log(LOG_TAG, "Setting prefs.");
        numResultsPref = Integer.parseInt(db.getPref("numResults"));
        sortPref = db.getPref("sortOrder");

        if (this.getArguments() != null && this.getArguments().containsKey("Artist")) {
            String artist = this.getArguments().getString("Artist");
            if (artist != null) {
                searchResults.clear();
                artistSearchText = artist;

//                this.browseArtist(artist);
//                this.getArguments().remove("Artist");
            }
        }
        // Must call in order to get callback to onOptionsItemSelected()
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logging.Log(LOG_TAG, "CREATING VIEW.");
        // Inflate the fragment and grab a reference to it.
        View view = inflater.inflate(R.layout.search_fragment, container, false);
        // Initialize the various elements of the SearchScreen.
        searchList = view.findViewById(R.id.ResultsListView);
        searchList.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v,
                                            ContextMenu.ContextMenuInfo menuInfo) {
                menu.add(Menu.NONE, MENU_EMAIL, Menu.NONE, "Email Link to Show");
                menu.add(Menu.NONE, MENU_INFO, Menu.NONE, "Show Info");
                menu.add(Menu.NONE, MENU_BOOKMARK, Menu.NONE, "Bookmark Show");
            }
        });

//        ImageButton searchButton = view.findViewById(R.id.SearchButton);
        searchMoreButton = new Button(getActivity());
        searchMoreButton.setText("Get More Results");
        searchMoreButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        searchMoreButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Logging.Log(LOG_TAG, "MORE BUTTON.");

                if (isMoreSearch(artistSearchInput.getText().toString())) {
                    Logging.Log(LOG_TAG, "MORE SEARCH.");
                    ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(artistSearchInput.getWindowToken(), 0);
                    isSearchMore = true;
                    // pageNum is incremented then searched with to get the next page.
                    executeSearch(Searching.makeSearchURLString(++pageNum, month, day, year, artistSearchText, numResultsPref, sortPref, dateSearchModifierPos));
                    // vibrator.vibrate(50);
                }
            }
        });
        searchList.addFooterView(searchMoreButton);

        if (searchResults.size() != 0) {
            searchMoreButton.setVisibility(View.VISIBLE);
        } else {
            searchMoreButton.setVisibility(View.GONE);
        }

        searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                ArchiveShowObj show = (ArchiveShowObj) searchList.getItemAtPosition(position);
                Logging.Log(LOG_TAG, show.getArtistAndTitle());

                Bundle bundle = new Bundle();
                bundle.putSerializable("show", show);

                NavHostFragment
                        .findNavController(SearchFragment.this)
                        .navigate(R.id.action_frag_search_to_frag_show_details, bundle);
            }
        });

        topAppBar = view.findViewById(R.id.topAppBar);
        topAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case android.R.id.home:
                        dialogAndNavigationListener.goHome();
                        break;
                    case R.id.HelpButton:
                        dialogAndNavigationListener.showDialog(SearchFragment.this.getResources().getString(R.string.search_screen_help), "Help");
                        break;
                    case R.id.SettingsButton:
                        Bundle bundle = new Bundle();
                        bundle.putString("order", SearchFragment.this.sortPref);
                        bundle.putInt("number", SearchFragment.this.numResultsPref);
                        bundle.putInt("month", SearchFragment.this.month);
                        bundle.putInt("day", SearchFragment.this.day);
                        bundle.putInt("year", SearchFragment.this.year);
                        bundle.putInt("datepos", SearchFragment.this.dateSearchModifierPos);
                        dialogAndNavigationListener.showSettingsDialog(bundle);
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initTopAppBar();
    }

    private void initTopAppBar() {
        Logging.Log(LOG_TAG, "Creating actionbar.");

        actionBarMenu = topAppBar.getMenu();

        artistSearchButton = actionBarMenu.findItem(R.id.SearchActionBarButton).getActionView().findViewById(R.id.SearchButton);

        artistSearchInput = actionBarMenu.findItem(R.id.SearchActionBarButton).getActionView().findViewById(R.id.ArtistSearchBox);
        artistSearchInput.setText(artistSearchText);
        // This changes the appearance of the search button to reflect whether or not we are searching for "more" shows with the same query.
        artistSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (isMoreSearch(s.toString())) {
                    searchMoreButton.setVisibility(View.VISIBLE);
                } else {
                    searchMoreButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (s.toString().contains("\n")) {
                    artistSearchInput.setText(s.toString().replace("\n", ""));
                    artistSearchButton.callOnClick();
                }
            }
        });

        // Set the adapter for autocomplete.
        if (!db.getPref("artistUpdate").equals("2010-01-01")) {
            artistSearchInput.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.artist_search_row, db.getArtistsStrings()));
        }
        // Set listeners in the show details and artist search bars for the enter key.
        // This is not guaranteed to work for soft keyboards because they are IME devices (read online).
        // Analogous code is in the onTextChanged() method overriden in the artistSearchInput's
        // TextChangedListener.
        artistSearchInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Logging.Log(LOG_TAG, event.toString());
                if (event != null) {
                    if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        if (event.getAction() == KeyEvent.ACTION_UP) {
                            // Act like the search button has been pressed.
                            return artistSearchButton.callOnClick();
                        } else {
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        artistSearchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Blank
                if (artistSearchInput.getText().toString().equals("")) {
                    // vibrator.vibrate(50);
                    Toast.makeText(getActivity(), R.string.error_no_query_message_text, Toast.LENGTH_SHORT).show();
                }
                // Search more
                else if (isMoreSearch(artistSearchInput.getText().toString())) {
                    Logging.Log(LOG_TAG, "MORE SEARCH.");
                    ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(artistSearchInput.getWindowToken(), 0);

                    isSearchMore = true;
                    // pageNum is incremented then searched with to get the next page.
                    executeSearch(Searching.makeSearchURLString(++pageNum, month, day, year, artistSearchText, numResultsPref, sortPref, dateSearchModifierPos));
                    // vibrator.vibrate(50);
                }
                // New search
                else {
                    artistSearchText = artistSearchInput.getText().toString();
                    isSearchMore = false;
                    Logging.Log(LOG_TAG, "NEW SEARCH.");
                    ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(artistSearchInput.getWindowToken(), 0);
                    searchResults.clear();
                    pageNum = 1;
                    // "1" is passed to retrieve page number 1.
                    // vibrator.vibrate(50);
                    executeSearch(Searching.makeSearchURLString(pageNum, month, day, year, artistSearchText, numResultsPref, sortPref, dateSearchModifierPos));
                }

            }
        });

        MenuItem searchActionBarMenuItem = actionBarMenu.findItem(R.id.SearchActionBarButton);
        searchActionBarMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return true;  // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;  // Return true to expand action view
            }
        });

        if (artistSearchText != null && !artistSearchText.equals("")) {
            searchActionBarMenuItem.expandActionView();
            executeSearch(Searching.makeSearchURLString(pageNum++, month, day, year, artistSearchText, numResultsPref, sortPref, dateSearchModifierPos));
        } else {
            Logging.Log(LOG_TAG, "Requesting focus.");
            searchActionBarMenuItem.expandActionView();
            artistSearchInput.requestFocus();
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(artistSearchInput, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void executeSearch(String query) {
        dateChanged = false;
        getShows(query);
    }

    private void getShows(final String query) {
        dialogAndNavigationListener.showLoadingDialog("Searching...");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, query, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Logging.Log(LOG_TAG, "Search finished.");

                searchFinished(new ArrayList<>(Searching.parseSearchResults(response)));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Logging.Log(LOG_TAG, "Search error: " + error.getMessage());
            }
        });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                Searching.TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueueSingleton.getInstance(getContext()).addToRequestQueue(stringRequest);
    }

    private void searchFinished(ArrayList<ArchiveShowObj> searchResponse) {
        Logging.Log(LOG_TAG, "LOADER FINISHED.");
        if (isSearchMore) {
            isSearchMore = false;
            Parcelable state = this.searchList.onSaveInstanceState();
            searchResults.addAll(searchResponse);
            this.searchList.onRestoreInstanceState(state);
        } else if (!searchResults.containsAll(searchResults) || searchResults.isEmpty()) {
            // The containsAll() call above is necessary because onLoadFinished() is being called on rotations,
            // and otherwise it will replace the searchResults with whatever the Loader most recently returned.
            searchResults.clear();
            searchResults.addAll(searchResponse);
        }

        this.refreshSearchList();
        dialogAndNavigationListener.hideDialog();
        if (searchResults.size() != 0) {
            if (searchResponse.size() > 5) {
                this.searchMoreButton.setVisibility(View.VISIBLE);
            } else {
                this.searchMoreButton.setVisibility(View.GONE);
            }
            this.searchMoreButton.setEnabled(true);
            ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(artistSearchInput.getWindowToken(), 0);
        } else {
            Toast.makeText(getActivity(), R.string.no_search_results_message_text, Toast.LENGTH_SHORT).show();
            this.searchMoreButton.setVisibility(View.GONE);
            this.searchMoreButton.setEnabled(false);
        }
    }

    private boolean isMoreSearch(String artist) {
        if (searchResults.size() == 0) {
            return false;
        }
        // Explanation:
        // If the artist input field is the same as the stored artist search text AND
        // The year input field is the same as the stored year search int (or if
        // the input is blank, the int is -1),
        // Return true because you should be fetching more results. Otherwise,
        // false to search initially.
        if (!dateChanged) {
            if (artist.equalsIgnoreCase(artistSearchText)) {
                Logging.Log(LOG_TAG, "DATECHANGED " + dateChanged);
                Logging.Log(LOG_TAG, "moreSearch set to true.");
                return true;
            } else {
                this.month = 0;
                this.day = 0;
                this.year = 0;
                this.dateSearchModifierPos = SearchSettingsDialogFragment.ANYTIME;
                return false;
            }
        } else {
            Logging.Log(LOG_TAG, "DATECHANGED " + dateChanged);
            Logging.Log(LOG_TAG, "moreSearch set to false.");
//			dateSearchModifierPos = 0;
//			this.datePref = 0;
            return false;
        }
    }

    private void refreshSearchList() {
        Logging.Log(LOG_TAG, "RESULT SIZE: " + searchResults.size());
        searchList.setAdapter(new RatingsAdapter(getActivity(), R.layout.search_list_row, searchResults));
        if (searchResults.isEmpty()) {
            Logging.Log(LOG_TAG, "Refreshing...  Empty list.");
            if (actionBarMenu != null) {
                Logging.Log(LOG_TAG, "Expanding ActionBar MenuItem.");
                actionBarMenu.findItem(R.id.SearchActionBarButton).expandActionView();
            }
        }
    }

    // ArrayAdapter for the ListView of shows with ratings.
    private class RatingsAdapter extends ArrayAdapter<ArchiveShowObj> {

        RatingsAdapter(Context context, int textViewResourceId,
                       List<ArchiveShowObj> objects) {
            super(context, textViewResourceId, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ArchiveShowObj show = (ArchiveShowObj) searchList
                    .getItemAtPosition(position);

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.search_list_row, null);
            }
            TextView artistText = convertView
                    .findViewById(R.id.ArtistText);
            TextView showText = convertView
                    .findViewById(R.id.ShowText);
            TextView sourceText = convertView.findViewById(R.id.SourceText);
            ImageView ratingsIcon = convertView
                    .findViewById(R.id.rating);
            if (show != null) {
                artistText.setText(show.getShowArtist() + " ");
                artistText.setSelected(true);
                artistText.setSingleLine();
                showText.setText(show.getShowTitle());
                showText.setSelected(true);
                showText.setSingleLine();
                sourceText.setText(show.getSource());
                sourceText.setSelected(true);
                sourceText.setSingleLine();
                switch ((int) show.getRating()) {
                    case 1:
                        ratingsIcon.setImageDrawable(getActivity().getResources()
                                .getDrawable(R.drawable.star1));
                        break;
                    case 2:
                        ratingsIcon.setImageDrawable(getActivity().getResources()
                                .getDrawable(R.drawable.star2));
                        break;
                    case 3:
                        ratingsIcon.setImageDrawable(getActivity().getResources()
                                .getDrawable(R.drawable.star3));
                        break;
                    case 4:
                        ratingsIcon.setImageDrawable(getActivity().getResources()
                                .getDrawable(R.drawable.star4));
                        break;
                    case 5:
                        ratingsIcon.setImageDrawable(getActivity().getResources()
                                .getDrawable(R.drawable.star5));
                        break;
                    default:
                        ratingsIcon.setImageDrawable(getActivity().getResources()
                                .getDrawable(R.drawable.star0));
                        break;
                }
            }
            return convertView;
        }
    }

    @Override
    public void onSettingsOkayButtonPressed(String sortType, int numResults, int datePos, int month, int day, int year) {
        Logging.Log(LOG_TAG, year + " , " + this.year + " , " + datePos + " , " + dateSearchModifierPos + " , " + dateChanged);

        this.sortPref = sortType;
        this.numResultsPref = numResults;
        if (this.month != month || this.day != day || this.year != year || dateSearchModifierPos != datePos) {
            Logging.Log(LOG_TAG, year + " , " + year + " , " + datePos + " , " + dateSearchModifierPos + " , " + dateChanged);
            Logging.Log(LOG_TAG, "Date changed.  Settings button press received.");
            this.month = month;
            this.day = day;
            this.year = year;
            this.dateSearchModifierPos = datePos;
            dateChanged = true;
            this.searchMoreButton.setEnabled(false);
        } else {
            dateChanged = false;
            this.searchMoreButton.setEnabled(true);
        }
    }

}