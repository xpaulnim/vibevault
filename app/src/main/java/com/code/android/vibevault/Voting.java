package com.code.android.vibevault;

import android.content.Context;
import android.text.format.DateUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class Voting {

    private final static String LOG_TAG = Voting.class.getName();

    private final static String host = "http://sandersdenardi.com/vibevault/php/";

    public static final int VOTES_SHOWS = 0;
    public static final int VOTES_ARTISTS = 1;
    public static final int VOTES_SHOWS_BY_ARTIST = 2;

    public static final int VOTES_ALL_TIME = 1;
    public static final int VOTES_DAILY = 2;
    public static final int VOTES_WEEKLY = 3;
    public static final int VOTES_NEWEST_ADDED = 4;
    public static final int VOTES_NEWEST_VOTED = 5;

    private final static int timeout = (int) (15 * DateUtils.SECOND_IN_MILLIS);

    public static String vote(final String showIdent,
                              final String showArtist,
                              final String showTitle,
                              final String showDate,
                              final StaticDataStore db,
                              final Context applicationContext) {

        int userId = Integer.parseInt(db.getPref("userId"));

        // TODO: this is a hack
        final String[] message = {"Error voting"};

        URI queryString = null;
        try {
            queryString = new URI(host + "vote.php?" +
                    "userId=" + userId +
                    "&showIdent=" + URLEncoder.encode(showIdent, "UTF-8") +
                    "&showArtist=" + URLEncoder.encode(showArtist.replace("'", ""), "UTF-8") +
                    "&showTitle=" + URLEncoder.encode(showTitle.replace("'", ""), "UTF-8") +
                    "&showDate=" + URLEncoder.encode(showDate, "UTF-8") +
                    "&showSource=" + "" +
                    "&showRating=" + 0.0);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Logging.Log(LOG_TAG, "Syntax Error. Please e-mail developer.");
            return message[0];
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return message[0];
        }

        Logging.Log(LOG_TAG, queryString.toString());

        StringRequest stringRequest = new StringRequest(Request.Method.GET, queryString.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.equalsIgnoreCase("0")) {
                    Logging.Log(LOG_TAG, "Error parsing server response. Please e-mail developer.");
                }

                int returnedUserId = 0;
                JSONObject jObject;
                try {
                    jObject = new JSONObject(response);
                    JSONArray resultArray = jObject.getJSONArray("results");
                    JSONObject resultObject = resultArray.getJSONObject(0);
                    returnedUserId = resultObject.optInt("userId");

                    db.updatePref("userId", Integer.toString(returnedUserId));
                    message[0] = resultObject.getString("resultText");
                } catch (JSONException e) {
                    e.printStackTrace();
                    Logging.Log(LOG_TAG, "Error parsing server response. Please e-mail developer.");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Logging.Log(LOG_TAG, "Can not reach external server. Check internet connection.");
            }
        });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                timeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueueSingleton.getInstance(applicationContext).addToRequestQueue(stringRequest);

        return message[0];
    }

    public static String vote(final ArchiveShowObj show,
                              final StaticDataStore db,
                              final Context applicationContext) {

        int userId = Integer.parseInt(db.getPref("userId"));

        final String[] message = {"Error voting"};

        URI queryString = null;
        try {
            queryString = new URI(host + "vote.php?" +
                    "userId=" + userId +
                    "&showIdent=" + URLEncoder.encode(show.getIdentifier(), "UTF-8") +
                    "&showArtist=" + URLEncoder.encode(show.getShowArtist().replace("'", ""), "UTF-8") +
                    "&showTitle=" + URLEncoder.encode(show.getShowTitle().replace("'", ""), "UTF-8") +
                    "&showDate=" + URLEncoder.encode(show.getDate(), "UTF-8") +
                    "&showSource=" + URLEncoder.encode(show.getShowSource(), "UTF-8") +
                    "&showRating=" + show.getRating());
        } catch (URISyntaxException | UnsupportedEncodingException e) {
            e.printStackTrace();
            Logging.Log(LOG_TAG, "Syntax Error. Please e-mail developer.");
            return message[0];
        }

        Logging.Log(LOG_TAG, queryString.toString());


        StringRequest stringRequest = new StringRequest(Request.Method.GET, queryString.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.equalsIgnoreCase("0")) {
                    Logging.Log(LOG_TAG, "Error parsing server response. Please e-mail developer.");
                }

                int returnedUserId = 0;
                JSONObject jObject;
                try {
                    jObject = new JSONObject(response);
                    JSONArray resultArray = jObject.getJSONArray("results");
                    JSONObject resultObject = resultArray.getJSONObject(0);
                    returnedUserId = resultObject.optInt("userId");

                    db.updatePref("userId", Integer.toString(returnedUserId));
                    message[0] = resultObject.getString("resultText");

                } catch (JSONException e) {
                    e.printStackTrace();
                    Logging.Log(LOG_TAG, "Error parsing server response. Please e-mail developer.");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Logging.Log(LOG_TAG, "Can not reach external server. Check internet connection.");
            }
        });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                timeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueueSingleton.getInstance(applicationContext).addToRequestQueue(stringRequest);

        return message[0];
    }

    public static ArrayList<ArchiveShowObj> getShows(final int resultType,
                                                     final int numResults,
                                                     final int offset,
                                                     final StaticDataStore db,
                                                     final Context applicationContext) {

        final ArrayList<ArchiveShowObj> shows = new ArrayList<>();
        int userId = Integer.parseInt(db.getPref("userId"));

        URI queryString = null;
        try {
            queryString = new URI(host + "getShows.php?" +
                    "resultType=" + resultType +
                    "&numResults=" + numResults +
                    "&offset=" + offset +
                    "&userId=" + userId);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return shows;
        }

        Logging.Log(LOG_TAG, queryString.toString());

        StringRequest stringRequest = new StringRequest(Request.Method.GET, queryString.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                int returnedUserId = 0;

                JSONObject jObject;
                try {
                    jObject = new JSONObject(response);
                    JSONArray showArray = jObject.getJSONArray("shows");
                    int numItems = showArray.length();
                    for (int i = 0; i < numItems; i++) {
                        JSONObject showObject = showArray.getJSONObject(i);
                        ArchiveShowObj newShow = new ArchiveShowObj(showObject.optString("identifier"),
                                showObject.optString("title"),
                                showObject.optString("artist"),
                                showObject.optString("date"),
                                showObject.optString("source"),
                                showObject.optDouble("rating"),
                                showObject.optInt("votes"));
                        returnedUserId = showObject.optInt("userId");

                        shows.add(newShow);
                    }

                    db.updatePref("userId", Integer.toString(returnedUserId));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                timeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueueSingleton.getInstance(applicationContext).addToRequestQueue(stringRequest);

        return shows;
    }

    public static ArrayList<ArchiveArtistObj> getArtists(final int resultType,
                                                         final int numResults,
                                                         final int offset,
                                                         final StaticDataStore db,
                                                         final Context applicationContext) {

        final ArrayList<ArchiveArtistObj> artists = new ArrayList<>();
        int userId = Integer.parseInt(db.getPref("userId"));

        URI queryString = null;
        try {
            queryString = new URI(host + "getArtists.php?" +
                    "resultType=" + resultType +
                    "&numResults=" + numResults +
                    "&offset=" + offset +
                    "&userId=" + userId);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return artists;
        }

        Logging.Log(LOG_TAG, queryString.toString());

        StringRequest stringRequest = new StringRequest(Request.Method.GET, queryString.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                int returnedUserId = 0;
                JSONObject jObject;
                try {
                    jObject = new JSONObject(response);
                    JSONArray artistArray = jObject.getJSONArray("artists");
                    int numItems = artistArray.length();
                    for (int i = 0; i < numItems; i++) {
                        JSONObject artistObject = artistArray.getJSONObject(i);
                        ArchiveArtistObj newArtist = new ArchiveArtistObj(artistObject.optInt("artistId"),
                                artistObject.optString("artist"),
                                artistObject.optDouble("rating"),
                                artistObject.optInt("votes"),
                                artistObject.optString("lastVote"));
                        returnedUserId = artistObject.optInt("userId");

                        artists.add(newArtist);
                    }

                    db.updatePref("userId", Integer.toString(returnedUserId));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                timeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueueSingleton.getInstance(applicationContext).addToRequestQueue(stringRequest);

        return artists;
    }

    public static ArrayList<ArchiveShowObj> getShowsByArtist(final int resultType,
                                                             final int numResults,
                                                             final int offset,
                                                             final int artistId,
                                                             final StaticDataStore db,
                                                             final Context applicationContext) {

        final ArrayList<ArchiveShowObj> shows = new ArrayList<>();
        int userId = Integer.parseInt(db.getPref("userId"));

        URI queryString = null;
        try {
            queryString = new URI(host + "getShowsByArtist.php?" +
                    "resultType=" + resultType +
                    "&numResults=" + numResults +
                    "&offset=" + offset +
                    "&userId=" + userId +
                    "&artistId=" + artistId);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return shows;
        }

        Logging.Log(LOG_TAG, queryString.toString());

        StringRequest stringRequest = new StringRequest(Request.Method.GET, queryString.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                int returnedUserId = 0;
                JSONObject jObject;
                try {
                    jObject = new JSONObject(response);
                    JSONArray showArray = jObject.getJSONArray("shows");
                    int numItems = showArray.length();
                    for (int i = 0; i < numItems; i++) {
                        JSONObject showObject = showArray.getJSONObject(i);
                        ArchiveShowObj newShow = new ArchiveShowObj(showObject.optString("identifier"),
                                showObject.optString("title"),
                                showObject.optString("artist"),
                                showObject.optString("date"),
                                showObject.optString("source"),
                                showObject.optDouble("rating"),
                                showObject.optInt("votes"));
                        returnedUserId = showObject.optInt("userId");

                        shows.add(newShow);
                    }

                    db.updatePref("userId", Integer.toString(returnedUserId));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Logging.Log(LOG_TAG, error.getMessage());
            }
        });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                timeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueueSingleton.getInstance(applicationContext).addToRequestQueue(stringRequest);

        return shows;
    }
}
