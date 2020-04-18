package com.code.android.vibevault;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.NodeList;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class Searching {

	private static final String LOG_TAG = Searching.class.getName();

	private static final String SEARCHING_PREFIX = "com.code.android.vibevault.searching.";
	public static final String SEARCHING_UPDATE = SEARCHING_PREFIX + "SEARCHING_UPDATE";

	public static final String EXTRA_STATUS = SEARCHING_PREFIX + "EXTRA_STATUS";
	public static final String EXTRA_TOTAL = SEARCHING_PREFIX + "EXTRA_TOTAL";
	public static final String EXTRA_COMPLETED = SEARCHING_PREFIX + "EXTRA_COMPLETED";

	private final static int timeout = (int) (15 * DateUtils.SECOND_IN_MILLIS);

	public static final int STATUS_DOWNLOADING = 0;
	public static final int STATUS_INSERTING = 1;
	public static final int STATUS_COMPLETED = 2;
	public static final int STATUS_ERROR = 3;

	public static String makeSearchURLString(final int pageNum,
											 final int monthSearchInt,
											 final int daySearchInt,
											 final int yearSearchInt,
											 final String artistSearchText,
											 final int numSearchResults,
											 final String sortResults,
											 final int dateType){
		String sortPref = sortResults;

		if(sortPref.equalsIgnoreCase("Date")){
			sortPref = "date+desc";
		} else if(sortPref.equalsIgnoreCase("Rating")){
			sortPref= "avg_rating+desc";
		}

		final StringBuilder queryString = new StringBuilder();

		try {
			final StringBuilder dateModifier = new StringBuilder();
			// FIXME
			switch(dateType){
				case SearchSettingsDialogFragment.ANYTIME:
					Logging.Log(LOG_TAG, "ANYTIME.");
					break;
				case SearchSettingsDialogFragment.BEFORE:	//Before
					Logging.Log(LOG_TAG, "BEFORE.");
					dateModifier.append("date:[1800-01-01%20TO%20")
							.append(yearSearchInt)
							.append("-")
							.append((monthSearchInt>0?String.format(Locale.getDefault(),"%02d",monthSearchInt):"01"))
							.append("-01]%20AND%20");
					break;
				case SearchSettingsDialogFragment.AFTER:	//After
					final int curDate = Calendar.getInstance().get(Calendar.DATE);
					final int curMonth = Calendar.getInstance().get(Calendar.MONTH);
					final int curYear = Calendar.getInstance().get(Calendar.YEAR);

					dateModifier.append("date:[")
							.append((monthSearchInt>0?yearSearchInt:yearSearchInt+1))
							.append("-")
							.append((monthSearchInt > 0 ? String.format(Locale.getDefault(), "%02d", monthSearchInt) : "01"))
							.append("-01%20TO%20")
							.append(curYear)
							.append("-")
							.append(String.format(Locale.getDefault(), "%02d", curMonth))
							.append("-")
							.append(String.format(Locale.getDefault(), "%02d", curDate))
							.append("]%20AND%20");
					break;
				case SearchSettingsDialogFragment.DURING:	// In Year.
					dateModifier.append("date:[")
							.append(yearSearchInt)
							.append("-")
							.append((monthSearchInt > 0 ? String.format(Locale.getDefault(), "%02d", monthSearchInt) : "01"))
							.append("-01%20TO%20")
							.append(yearSearchInt)
							.append("-")
							.append((monthSearchInt > 0 ? String.format(Locale.getDefault(), "%02d", monthSearchInt) : "12"))
							.append("-31]%20AND%20");
					break;
				case SearchSettingsDialogFragment.SPECIFIC:
					final String specific = yearSearchInt +
							"-" +
							String.format(Locale.getDefault(), "%02d", monthSearchInt) +
							"-" +
							String.format(Locale.getDefault(), "%02d", daySearchInt);

					dateModifier.append("date:[")
							.append(specific)
							.append("%20TO%20")
							.append(specific)
							.append("]%20AND%20");
					break;
			}
			// We search creator:(random's artist)%20OR%20creator(randoms artist) because
			// archive.org does not like apostrophes in the creator query.
			final StringBuilder specificSearch = new StringBuilder("");
//			if(searchType.equals("Artist")){

			final String artistSearchTextNoApostrophes = artistSearchText.replace("'", "").replace("\"", "");

			specificSearch.append("(")
					.append("creator:(")
					.append(URLEncoder.encode(artistSearchText,"UTF-8"))
					.append(")")
					.append("%20OR%20creator:(")
					.append(URLEncoder.encode(artistSearchTextNoApostrophes,"UTF-8"))
					.append(")")
					.append(")");

//			} else if(searchType.equals("Show/Artist Description")){
//				specificSearch = "(creator:(" + URLEncoder.encode(artistSearchText,"UTF-8") + ")" + "%20OR%20description:(" + URLEncoder.encode(artistSearchText.replace("'", "").replace("\"", ""),"UTF-8") + "))";
//			}

			queryString.append("http://www.archive.org/advancedsearch.php?q=")
					.append("(")
					.append(dateModifier)
					.append("mediatype:(etree)")
					.append("%20AND%20format:(mp3)")
					.append("%20AND%20")
					.append("(")
					.append(specificSearch)
					.append(")")
					.append(")")
					.append("&fl[]=date&fl[]=avg_rating&fl[]=source&fl[]=format&fl[]=identifier&fl[]=mediatype&fl[]=title&sort[]=")
					.append(sortPref)
					.append("&sort[]=&sort[]=&rows=")
					.append(numSearchResults)
					.append("&page=")
					.append(pageNum)
					.append("&output=json&save=yes");

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		Logging.Log(LOG_TAG,queryString.toString());
		return queryString.toString();
	}

	public static void getShows(final String query, final ArrayList<ArchiveShowObj> searchResults, final Context applicationContext){

		StringRequest stringRequest = new StringRequest(Request.Method.GET, query, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				/*
				 * Parse the JSON String (queryResult) that we got from archive.org. If the
				 * mediatype is etree, create an ArchiveShowObj which encapsulates
				 * the information for a particular result from the archive.org
				 * query. Populate the ArrayList which backs the ListView, and call
				 * the inherited refreshSearchList().
				 */
				JSONObject jObject;
				try {
					Logging.Log(LOG_TAG, "JSON grabbed.");
					jObject = new JSONObject(response).getJSONObject("response");
					JSONArray docsArray = jObject.getJSONArray("docs");
					int numItems = docsArray.length();
					if(numItems == 0){
						Logging.Log(LOG_TAG, "Artist may not have content on archive.org...");
					}
					for (int i = 0; i < numItems; i++) {
						if (docsArray.getJSONObject(i).optString("mediatype").equals("etree")) {
							// Might be inefficient to keep getting size().
							searchResults.add(
									searchResults.size(),
									new ArchiveShowObj(
											docsArray.getJSONObject(i).optString("title"),
											docsArray.getJSONObject(i).optString("identifier"),
											docsArray.getJSONObject(i).optString("date"),
											docsArray.getJSONObject(i).optDouble("avg_rating"),
											docsArray.getJSONObject(i).optString("format"),
											docsArray.getJSONObject(i).optString("source")));
						}
					}
				} catch (JSONException e) {
					// DEBUG
					Logging.Log(LOG_TAG, "JSON error: " + response);
					Logging.Log(LOG_TAG, e.toString());
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

		Logging.Log(LOG_TAG, "Returning results.");
	}

	public static Boolean updateArtists(final StaticDataStore db, final Context applicationContext){
		Logging.Log(LOG_TAG, "Fetching Artists");
		final ArrayList<ArrayList<String>> artists = new ArrayList<ArrayList<String>>();

		final HtmlCleaner pageParser = new HtmlCleaner();
		CleanerProperties props = pageParser.getProperties();
		props.setAllowHtmlInsideAttributes(true);
		props.setAllowMultiWordAttributes(true);
		props.setRecognizeUnicodeChars(true);
		props.setOmitComments(true);

		String url = "http://www.archive.org/browse.php?field=/metadata/bandWithMP3s&collection=etree";

		StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					TagNode node = pageParser.clean(response);

					org.w3c.dom.Document doc = new DomSerializer(new CleanerProperties()).createDOM(node);
					XPath xpath = XPathFactory.newInstance().newXPath();
					NodeList artistNodes = (NodeList) xpath.evaluate("//div[@class='row']//div[@class='col-sm-4']/a", doc, XPathConstants.NODESET);
					NodeList numberNodes = (NodeList) xpath.evaluate("//div[@class='row']//div[@class='col-sm-4']/text()[preceding-sibling::a]", doc, XPathConstants.NODESET);
					Logging.Log(LOG_TAG, "artistNodes: " + artistNodes.getLength());
					Logging.Log(LOG_TAG, "numberNodes: " + numberNodes.getLength());

					if (artistNodes.getLength() == numberNodes.getLength()) {
						for (int i = 0; i < artistNodes.getLength(); i++) {
							ArrayList<String> artistPair = new ArrayList<String>();
							artistPair.add(artistNodes.item(i).getTextContent().replace("&apos;", "'").replace("&gt;", ">").replace("&lt;", "<").replace("&quot;", "\"").replace("&amp;", "&"));
							artistPair.add(numberNodes.item(i).getTextContent());
							artists.add(artistPair);
						}
					}
					if (artists.size() > 0) {
						db.insertArtistBulk(artists);
						String s = DateFormat.format("yyyy-MM-dd", new GregorianCalendar().getTime()).toString();
						db.updatePref("artistUpdate", s);
						Logging.Log(LOG_TAG, "Finished Fetching Artists");
					} else {
						Logging.Log(LOG_TAG, "Error Fetching Artists");
					}
				} catch (ParserConfigurationException | XPathExpressionException e) {
					e.printStackTrace();
				}

			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Logging.Log(LOG_TAG, "Error Fetching Artists. " + error.getMessage());
			}
		});

		stringRequest.setRetryPolicy(new DefaultRetryPolicy(
				timeout,
				DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

		RequestQueueSingleton.getInstance(applicationContext).addToRequestQueue(stringRequest);

		return true;

	}

}