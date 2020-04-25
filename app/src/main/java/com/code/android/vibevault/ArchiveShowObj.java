/*
 * ArchiveShowObj.java
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

import androidx.room.Ignore;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

public class ArchiveShowObj extends ArchiveVoteObj implements Serializable {
	
	private static final String LOG_TAG = ArchiveShowObj.class.getName();

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static final String ArchiveShowPrefix = "http://www.archive.org/details/";

	@Ignore
	private String wholeTitle = "";
	@Ignore
	private String date = "";
	@Ignore
	private double rating = 0.0;
	@Ignore
	private URL showURL = null;
	@Ignore
	private boolean hasSelectedSong = false;
	@Ignore
	private String selectedSong = "";

	private String identifier = "";
	private String showTitle = "";
	private String showArtist = "";
	private String source = "";
	private boolean hasVBR = false;
	private boolean hasLBR = false;

	
	/** Create an object which represents a show returned from an archive.org search.
	 * 
	 * @param wholeTitle Show's title.
	 * @param identifier Show's "identifier" (unique part of its URL).
	 * @param date Show's date.
	 * @param rating Show's rating.
	 * @param format Show's format list.
	 * @param src Show's source.
	 */
	public ArchiveShowObj(String wholeTitle,
						  String identifier,
						  String date,
						  double rating,
						  String format,
						  String src) {
		this.wholeTitle = wholeTitle;

		String[] artistAndShowTitle = wholeTitle.split(" Live at ");
		if(artistAndShowTitle.length < 2){
			artistAndShowTitle = wholeTitle.split(" Live @ ");
		}
		if(artistAndShowTitle.length < 2){
			artistAndShowTitle = wholeTitle.split(" Live ");
		}
		showArtist = artistAndShowTitle[0].replaceAll(" - ", "").replaceAll("-","");
		if(artistAndShowTitle.length >= 2){
			showTitle = artistAndShowTitle[1];
		}
		this.identifier = identifier;
		this.date = date;
		this.rating = rating;
		source = src;
		this.parseFormatList(format);
		try{
			showURL = new URL(ArchiveShowPrefix + this.identifier);
		} catch(MalformedURLException e){
			// url is null in this case!
		}
	}
	
	// Constructor called from DB version > 5
	public ArchiveShowObj(String identifier,
						  String showTitle,
						  String showArtist,
						  String source,
						  boolean hasVBR,
						  boolean hasLBR,
						  int DBID){
		wholeTitle = showArtist + " Live at " + showTitle;
		this.identifier = identifier;
		this.showTitle = showTitle;
		this.showArtist = showArtist;
		this.source = source;
		this.hasVBR = hasVBR;
		this.hasLBR = hasLBR;
		this.DBID = DBID;
		try{
			showURL = new URL(ArchiveShowPrefix + this.identifier);
		} catch(MalformedURLException e){
			// url is null in this case!
		}
	}
	
	// Constructor called from DB version <= 5
	public ArchiveShowObj(String id,
						  String tit,
						  String hasVBR,
						  String hasLBR){
		wholeTitle = tit;
		String artistAndShowTitle[] = tit.split(" Live at ");
		if(artistAndShowTitle.length < 2){
			artistAndShowTitle = tit.split(" Live @ ");
		}
		if(artistAndShowTitle.length < 2){
			artistAndShowTitle = tit.split(" Live ");
		}
		showArtist = artistAndShowTitle[0].replaceAll(" - ", "").replaceAll("-","");
		if(artistAndShowTitle.length >= 2){
			showTitle = artistAndShowTitle[1];
		}
		identifier = id;
		if(hasVBR.equals("1")){
			this.hasVBR = true;
		}
		if(hasLBR.equals("1")){
			this.hasLBR = true;
		}
		try{
			showURL = new URL(ArchiveShowPrefix + identifier);
		} catch(MalformedURLException e){
			// url is null in this case!
		}
	}
	
	public ArchiveShowObj(String linkString,
						  boolean hasSelected) {
		wholeTitle = "";
		// This should take care of any prefix (eg. http://, http://www., www.).
		identifier = linkString.split("archive.org/details/")[1];
		showTitle = "";
		showArtist = "";
		source = "";
		hasVBR = true;
		hasLBR = true;
		hasSelectedSong = hasSelected;
		try{
			showURL = new URL(linkString);
		} catch(MalformedURLException e){
			// url is null in this case!
		}
	}
	
	// Constructor called from vote return
	public ArchiveShowObj(String ident,
						  String title,
						  String artist,
						  String date,
						  String src,
						  double rat,
						  int numVotes){
		wholeTitle = title;
		identifier = ident;
		showTitle = title;
		showArtist = artist;
		String artistAndShowTitle[] = title.split(" Live at ");
		if(artistAndShowTitle.length < 2){
			artistAndShowTitle = title.split(" Live @ ");
		}
		if(artistAndShowTitle.length < 2){
			artistAndShowTitle = title.split(" Live ");
		}
		if(artistAndShowTitle.length >= 2){
			showTitle = artistAndShowTitle[1];
		}
		source = src;
		hasVBR = true;
		hasLBR = false;
		rating = rat;
		votes = numVotes;
		try{
			showURL = new URL(ArchiveShowPrefix + identifier);
		} catch(MalformedURLException e){
			// url is null in this case!
		}
	}
	
	public void setFullTitle(String s){
		Logging.Log(LOG_TAG, s);
		wholeTitle = s;
		String artistAndShowTitle[] = s.split(" Live at ");
		if(artistAndShowTitle.length < 2){
			artistAndShowTitle = s.split(" Live @ ");
		}
		if(artistAndShowTitle.length < 2){
			artistAndShowTitle = s.split(" Live ");
		}
		showArtist = artistAndShowTitle[0];
		Logging.Log(LOG_TAG, "SHOW ARTIST: " + showArtist);
		if(artistAndShowTitle.length >= 2){
			showTitle = artistAndShowTitle[1];
		}
	}

	private void parseFormatList(String formatList){
		if(formatList.contains("64Kbps MP3")){
			hasLBR = true;
		}
		if(formatList.contains("64Kbps M3U")){
			hasLBR = true;
		}
		if(formatList.contains("VBR MP3")){
			hasVBR = true;
		}
		if(formatList.contains("VBR M3U")){
			hasVBR = true;
		}
	}
	
	public boolean hasVBR(){
		return hasVBR;
	}
	
	public boolean hasLBR(){
		return hasLBR;
	}
	
	//should only be called from the voted shows activity
	public double getRating(){
		return rating;
	}
	
	public String getSource(){
		return source;
	}
	
	public String getDate(){
		return date;
	}

	public String getShowSource(){
		return source;
	}

	public String getShowArtist(){
		return showArtist;
	}

	public String getShowTitle(){
		return showTitle;
	}

	public String getArtistAndTitle(){
		return wholeTitle;
	}
	
	public String getIdentifier(){
		return identifier;
	}
	
	public String getLinkPrefix(){
		return "http://www.archive.org/download/" + identifier + "/" + identifier;
	}
	
	// Used when the ArchiveShowObj is created by an Intent from
	// the user clicking on a song link.
	public boolean hasSelectedSong(){
		return hasSelectedSong;
	}
	
	// Used when the ArchiveShowObj is created by an Intent from
	// the user clicking on a song link.
	public void setSelectedSong(String s){
		selectedSong = s;
	}
	
	// Used when the ArchiveShowObj is created by an Intent from
	// the user clicking on a song link.
	public String getSelectedSong(){
		return selectedSong;
	}
	
	// CALLER MUST CHECK FOR NULL RETURN VALUE!
	// \/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
	public URL getShowURL() {
		return showURL;
	}

	public void setWholeTitle(String wholeTitle) {
		this.wholeTitle = wholeTitle;
	}

	public void setShowURL(URL showURL) {
		this.showURL = showURL;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setShowArtist(String showArtist) {
		this.showArtist = showArtist;
	}

	public void setShowTitle(String showTitle) {
		this.showTitle = showTitle;
	}

	public void setHasVBR(boolean hasVBR) {
		this.hasVBR = hasVBR;
	}

	public void setHasLBR(boolean hasLBR) {
		this.hasLBR = hasLBR;
	}

	public void setHasSelectedSong(boolean hasSelectedSong) {
		this.hasSelectedSong = hasSelectedSong;
	}

	@Override
	public String toString() {
		return String.format(wholeTitle);
	}

	@Override
	public boolean equals(Object obj){
		ArchiveShowObj show = (ArchiveShowObj) obj;
		if(this.identifier == show.getIdentifier()){
			return true;
		}
		else{
			return false;
		}
	}
}