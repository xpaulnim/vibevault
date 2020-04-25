/*
 * ArchiveSongObj.java
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

import android.os.Environment;

import androidx.room.Ignore;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

public class ArchiveSongObj extends ArchiveVoteObj implements Serializable {

    private static final long serialVersionUID = 1L;
    // I'm pretty sure that URL objects are bigger
    // than String objects, so we don't store the
    // URL's as actual URL's unless the user indicates
    // that we are actually going to download or stream.
    @Ignore
    private String urlString;
    private String songTitle;
    private String showTitle;
    private String showIdent;
    private String showArtist;
    private String fileName;
    private String folderName;

    @Ignore
    private int status;
    @Ignore
    private ArchiveShowObj downloadShow;

    protected static final String LOG_TAG = ArchiveSongObj.class.getName();

    /**
     * Create a song object.
     * <p>
     * The constructor will use the song name and show title to search in the
     * proper sdcard location to see if the file is there. If the file has the
     * INCOMPLETE_DL_STRING appended to it, it knows that the song is in
     * progress. If the file exists without that appended to it, it has been
     * completed. Otherwise, the song has not been downloaded (or started to be
     * downloaded).
     *
     * @param songTitle The title of the song.
     * @param showTitle The title of the show which the song is a part of.
     */
    @Ignore
    public ArchiveSongObj(String songTitle,
                          String urlString,
                          String showTitle,
                          String showIdent,
                          String showArtist) {
        Logging.Log(LOG_TAG, "Creating show: ");

        this.songTitle = songTitle.replace("&apos;", "'")
                .replace("&gt;", ">")
                .replace("&lt;", "<")
                .replace("&quot;", "\"")
                .replace("&amp;", "&");
        this.showTitle = showTitle;
        this.showArtist = showArtist;
        this.urlString = urlString;
        this.showIdent = showIdent;

        status = -1;

        String[] splitArray = urlString.split("/");
        fileName = splitArray[splitArray.length - 1];

        if (showIdent.equalsIgnoreCase(splitArray[splitArray.length - 3])) {
            folderName = splitArray[splitArray.length - 2];
        } else {
            folderName = "";
        }
    }

    // Constructor from DB
    public ArchiveSongObj(String songTitle,
                          String folderName,
                          String fileName,
                          String showTitle,
                          String showIdent,
                          String showArtist,
                          int DBID) {
        if (!folderName.equals("")) {
            urlString = "http://www.archive.org/download/" + showIdent + "/" + folderName + "/" + fileName;
        } else {
            urlString = "http://www.archive.org/download/" + showIdent + "/" + fileName;
        }
        this.folderName = folderName;
        status = -1;
        this.songTitle = songTitle.replace("&apos;", "'")
                .replace("&gt;", ">")
                .replace("&lt;", "<")
                .replace("&quot;", "\"")
                .replace("&amp;", "&");
        this.showTitle = showTitle;
        this.showArtist = showArtist;
        this.showIdent = showIdent;
        this.fileName = fileName;
        this.DBID = DBID;
    }

    public static String sanitizeForFilename(String s) {
        return s.replaceAll("[^a-zA-Z0-9]", "");
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean exists(StaticDataStore db) {
        return db.songIsDownloaded(fileName) && new File(getFilePath(db)).exists();
    }

    public String getSongPath(StaticDataStore db) {
        if (exists(db)) {
            return getFilePath(db);
        } else {
            return getLowBitRate().toString();
        }
    }

    public String getFilePath(StaticDataStore db) {
        return Environment.getExternalStorageDirectory() +
                Downloading.getAppDirectory(db) + showIdent + '/' +
                fileName;
    }

    public void setDownloadShow(ArchiveShowObj show) {
        downloadShow = show;
    }

    public ArchiveShowObj getDownloadShow() {
        return downloadShow;
    }

//	public void setDownloadStatus(int status){
//		this.status = status;
//		if(status == DownloadSongThread.COMPLETE){
//			checkExists();
//		}
//	}

    public int getDownloadStatus() {
        return status;
    }

    public String getShowIdentifier() {
        return showIdent;
    }

    public String getShowTitle() {
        return showTitle;
    }

    public String getShowArtist() {
        return showArtist;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public String getSongArtistAndTitle() {
        return showArtist + " - " + songTitle;
    }

    /**
     * Returns a URL object of the lowest bitrate for a song.
     * <p>
     * Returns 64kbps if it exists, VBR if 64kbs doesn't exist, and any .mp3 if VBR doesn't exist.
     * Otherwise, it returns null, which the caller should check for.
     */
    public URL getLowBitRate() {
        if (urlString != null) {
            try {
                return new URL(urlString);
            } catch (MalformedURLException e) {
                return null;

            }
        } else {
            return null;
        }
    }

    public boolean hasFolder() {
        return !folderName.equals("");
    }

    public String getFolderName() {
        return folderName;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public void setShowTitle(String showTitle) {
        this.showTitle = showTitle;
    }

    public void setShowIdent(String showIdent) {
        this.showIdent = showIdent;
    }

    public void setShowArtist(String showArtist) {
        this.showArtist = showArtist;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ArchiveSongObj) {
            ArchiveSongObj song = (ArchiveSongObj) obj;
			return fileName.equals(song.fileName);
        } else {
            return false;
        }
    }

}