/*
 * DataStore.java
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

import android.content.Context;

import com.code.android.vibevault.data.Artist;
import com.code.android.vibevault.data.DownloadStatus;
import com.code.android.vibevault.data.PlaylistSong;
import com.code.android.vibevault.data.Show;
import com.code.android.vibevault.repository.ArtistRepository;
import com.code.android.vibevault.repository.DownloadRepository;
import com.code.android.vibevault.repository.PlaylistRepository;
import com.code.android.vibevault.repository.PrefRepository;
import com.code.android.vibevault.repository.ShowRepository;
import com.code.android.vibevault.repository.SongRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StaticDataStore {

    private static final String LOG_TAG = StaticDataStore.class.getName();

    public static final int SHOW_STATUS_NOT_DOWNLOADED = 0;
    public static final int SHOW_STATUS_PARTIALLY_DOWNLOADED = 1;
    public static final int SHOW_STATUS_FULLY_DOWNLOADED = 2;

    private static StaticDataStore dataStore = null;

    private ArtistRepository artistRepository;
    private DownloadRepository downloadRepository;
    private SongRepository songRepository;
    private ShowRepository showRepository;
    private PlaylistRepository playlistRepository;
    private PrefRepository prefRepository;

    public static StaticDataStore getInstance(Context ctx) {
        if (dataStore == null) {
            dataStore = new StaticDataStore(ctx, "StaticDataStore");
        }

        return dataStore;
    }

    private StaticDataStore(Context context, String caller) {
        artistRepository = new ArtistRepository(context);
        downloadRepository = new DownloadRepository(context);
        songRepository = new SongRepository(context);
        showRepository = new ShowRepository(context);
        playlistRepository = new PlaylistRepository(context);
        prefRepository = new PrefRepository(context);

        Logging.Log(LOG_TAG, "DB opened by (initialized): " + caller);
    }

    public static String sanitize(String s) {
        return s.replaceAll("[^-a-zA-Z0-9\\s-_@><&+\\\\/\\*]", "");
    }

    public String getPref(String pref_name) {
        return prefRepository.getPref(sanitize(pref_name));
    }

    public void updatePref(String pref_name, String pref_value) {
        Logging.Log(LOG_TAG, "Update " + pref_name + " to " + pref_value);
        prefRepository.updatePref(pref_name, pref_value);
    }

    public void insertShow(ArchiveShowObj show) {
        showRepository.insertShow(new Show(
                show.getIdentifier(),
                show.getShowTitle().replaceAll("'", "''"),
                show.getShowArtist().replaceAll("'", "''"),
                show.getShowSource().replaceAll("'", "''"),
                show.getShowSource().replaceAll("'", "''"),
                show.hasVBR(),
                show.hasLBR()));
    }

    public void updateShow(ArchiveShowObj show) {
        showRepository.updateShow(show.getIdentifier(), show.getShowTitle(), show.getShowArtist());
    }

    public ArrayList<ArchiveShowObj> getBadShows() {
        return new ArrayList<>(showRepository.getBadShows());
    }

    public void insertRecentShow(ArchiveShowObj show) {
        insertShow(show);
        showRepository.insertRecentShow(show.getIdentifier());
    }

    public ArrayList<ArchiveShowObj> getRecentShows() {
        Logging.Log(LOG_TAG, "Returning all recent shows");
        return new ArrayList<>(showRepository.getRecentShows());
    }

    public ArrayList<ArchiveShowObj> getDownloadShows() {
        Logging.Log(LOG_TAG, "Returning all downloaded shows");
        return new ArrayList<>(downloadRepository.getDownloads());
    }

    public ArchiveShowObj getShow(String identifier) {
        Logging.Log(LOG_TAG, "Getting show: " + identifier);
        return showRepository.getShow(identifier);
    }

    public void deleteRecentShow(long show_id) {
        Logging.Log(LOG_TAG, "Deleting recent show at id=" + show_id);
        showRepository.deleteRecentShow(show_id);
    }

    public void clearRecentShows() {
        Logging.Log(LOG_TAG, "Deleting all show");
        showRepository.clearRecentShows();
    }

    public long insertSong(ArchiveSongObj song) {
        songRepository.insert(song.getShowIdentifier(), song.getSongTitle().replaceAll("'", "''"), song.getFileName());

        long song_id = songRepository.getSongIdByFilename(song.getFileName());
        if (song.hasFolder()) {
            songRepository.setFolderName(song_id, song.getFolderName());
        }
        return song_id;
    }

    public void setSongDownloading(ArchiveSongObj song, long id) {
        Logging.Log(LOG_TAG, "Start Downloading " + song.getFileName() + ": " + id);
        songRepository.setSongIsDownloading(id, song.getFileName());
    }

    public boolean getSongIsDownloading(ArchiveSongObj song) {
        Logging.Log(LOG_TAG, "Get downloading status for " + song.getFileName());

        int count = songRepository.getSongIsDownloading(song.getFileName());
        Logging.Log(LOG_TAG, "Result: " + count);

        return count > 0;
    }

    public void setSongDownloaded(long id) {
        Logging.Log(LOG_TAG, "Finished Downloading: " + id);
        songRepository.setSongDownloaded(id);
    }

    public void setSongDownloaded(String fileName) {
        songRepository.setSongDownloaded(fileName);
    }

    public void setSongDeleted(ArchiveSongObj song) {
        songRepository.setSongDeleted(song.getFileName());
    }

    public void setShowDeleted(ArchiveShowObj show) {
        showRepository.setShowDeleted(show.getIdentifier());
    }

    public boolean getShowExists(ArchiveShowObj show) {
        return showRepository.getShowExists(show.getIdentifier()) > 0;
    }

    public int getShowDownloadStatus(ArchiveShowObj show) {
        int downloaded = 0;
        int total = 0;

        for (DownloadStatus downloadStatus : downloadRepository.getShowDownloadStatus(show.getIdentifier())) {
            downloaded += downloadStatus.downloaded;
            total += downloadStatus.total;
        }

        if (downloaded > 0) {
            if (downloaded < total) {
                return SHOW_STATUS_PARTIALLY_DOWNLOADED;
            } else {
                return SHOW_STATUS_FULLY_DOWNLOADED;
            }
        } else {
            return SHOW_STATUS_NOT_DOWNLOADED;
        }
    }

    public void setShowExists(ArchiveShowObj archiveShow) {
        showRepository.setShowExists(archiveShow.getIdentifier());
    }

    public boolean songIsDownloaded(String song_filename) {
        return songRepository.songIsDownloaded(song_filename).size() > 0;
    }

    public ArrayList<ArchiveSongObj> getSongsFromShow(String show_identifier) {
        return new ArrayList<>(songRepository.getSongsFromShow(show_identifier));
    }

    public ArrayList<ArchiveSongObj> getDownloadedSongsFromShow(String showIdent) {
        return new ArrayList<>(songRepository.getDownloadedSongsFromShow(showIdent));
    }

    public ArrayList<ArchiveSongObj> getSongsFromShowKey(int id) {
        return new ArrayList<>(songRepository.getSongsFromShowKey(id));
    }

    public void insertKnownSongIntoPlaylist(int playlist_id, ArchiveSongObj song, int position) {
        if (playlist_id <= 0) {
            //Playlist not saved yet
            return;
        }

        playlistRepository.insertKnownSongIntoPlaylist(playlist_id, position, song.getFileName());
    }

    public void clearNowPlayingSongs() {
        playlistRepository.clearNowPlayingSongs();
    }

    //Returns cursor of songs associated with playlist
    public ArrayList<ArchiveSongObj> getSongsFromPlaylist(long key) {
        return new ArrayList<>(playlistRepository.getSongsFromPlaylist(key));
    }

    public ArrayList<ArchiveSongObj> getNowPlayingSongs() {
        return new ArrayList<>(playlistRepository.getSongsFromPlaylist(0));
    }

    public void setNowPlayingSongs(ArrayList<ArchiveSongObj> songs) {
        clearNowPlayingSongs();
        if (songs.size() > 0) {
            List<PlaylistSong> playlistSongs = new ArrayList<>();
            int index = 0;
            for (ArchiveSongObj archiveSong : songs) {
                PlaylistSong playlistSong = new PlaylistSong();
                playlistSong.setPlaylistId(0);
                playlistSong.setSongId(archiveSong.getID());
                playlistSong.setTrackNum(index);

                playlistSongs.add(playlistSong);

                index++;
            }

            playlistRepository.addToPlaylist(playlistSongs);
        }
    }

    public void addSongToNowPlaying(ArchiveSongObj s) {
        playlistRepository.addSongToNowPlaying(s.getID());
    }

    public void addArtist(String artistName, String numShows) {
        Artist artist = new Artist();
        artist.setArtistName(artistName);
        artist.setNumShows(numShows);
    }

    public ArrayList<HashMap<String, String>> getArtist(String firstLetter) {
        List<Artist> artists = artistRepository.getArtist(firstLetter);

        ArrayList<HashMap<String, String>> artistsMap = new ArrayList<>();

        for (Artist artist : artists) {
            HashMap<String, String> artistMap = new HashMap<>();
            artistMap.put("artist", artist.getArtistName());
            artistMap.put("shows", artist.getNumShows());

            artistsMap.add(artistMap);
        }

        return artistsMap;
    }

    public void insertArtistBulk(ArrayList<ArrayList<String>> artists) {
        Logging.Log(LOG_TAG, "Bulk inserting " + artists.size() + " artists");
        artistRepository.deleteArtists();

        List<Artist> artistsList = new ArrayList<>();
        for (int i = 0; i < artists.size(); i++) {
            Artist artist = new Artist();
            artist.setArtistName(artists.get(i).get(0));
            artist.setNumShows(artists.get(i).get(1));

            artistsList.add(artist);
        }

        artistRepository.insertArtists(artistsList);
    }

    public void clearArtists() {
        prefRepository.updatePref("artistUpdate", "2010-01-01");
    }

    public void insertFavoriteShow(ArchiveShowObj show) {
        insertShow(show);
        showRepository.insertFavouriteShow(show.getIdentifier());
    }

    public ArrayList<ArchiveShowObj> getFavoriteShows() {
        Logging.Log(LOG_TAG, "Returning all favorite shows");
        /*
         * return db.query(RECENT_TBL, new String[] { SHOW_KEY, SHOW_IDENT,
         * SHOW_TITLE, SHOW_HASVBR, SHOW_HASLBR }, null, null, null, null,
         * null);
         */
        return new ArrayList<>(showRepository.getFavoriteShows());
    }

    public void deleteFavoriteShow(long show_id) {
        Logging.Log(LOG_TAG, "Deleting favorite show at show_id=" + show_id);
        showRepository.deleteFavoriteShow(show_id);
    }

    public void clearFavoriteShows() {
        Logging.Log(LOG_TAG, "Deleting all show");
        showRepository.deleteFavoriteShows();
    }

    public String[] getArtistsStrings() {
        return artistRepository.getArtistName().toArray(new String[0]);
    }

}
