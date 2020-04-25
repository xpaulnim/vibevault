package com.code.android.vibevault.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.code.android.vibevault.ArchiveSongObj;
import com.code.android.vibevault.data.PlaylistSong;

import java.util.List;

@Dao
public interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addToPlaylist(List<PlaylistSong> songs);

    @Insert
    void addToPlaylist(PlaylistSong songs);

    @Query(" INSERT INTO playlistSongsTbl(playlist_id,song_id,trackNum) " +
            "SELECT 0, :song_id, MAX(trackNum) " +
            "  FROM playlistSongsTbl where playlist_id = 0")
    void addSongToNowPlaying(long song_id);

    @Query("SELECT song.songTitle                                 as songTitle " +
            "    , song.folderName                                as folderName " +
            "    , song.fileName                                  as fileName " +
            "    , show.showArtist || 'Live at' || show.showTitle as showTitle " +
            "    , show.showIdent                                 as showIdent " +
            "    , show.showArtist                                as showArtist " +
            "    , song._id                                       as DBID " +
            " FROM playlistSongsTbl pls " +
            "INNER JOIN songTbl song ON song._id = pls.song_id " +
            "INNER JOIN showTbl show ON show._id = song.show_id " +
            "WHERE pls.playlist_id = :playlist_id " +
            "ORDER BY pls.trackNum")
    List<ArchiveSongObj> getSongsFromPlaylist(long playlist_id);

    @Query("DELETE FROM playlistSongsTbl")
    void clearNowPlayingSongs();

    @Query("SELECT song.songTitle                                 as songTitle " +
            "    , song.folderName                                as folderName " +
            "    , song.fileName                                  as fileName " +
            "    , show.showArtist || 'Live at' || show.showTitle as showTitle " +
            "    , show.showIdent                                 as showIdent " +
            "    , show.showArtist                                as showArtist " +
            "    , song._id                                       as DBID " +
            " FROM playlistSongsTbl pls " +
            "INNER JOIN songTbl song ON song._id = pls.song_id " +
            "INNER JOIN showTbl show ON show._id = song.show_id " +
            "WHERE pls.playlist_id = :playlist_id " +
            "ORDER BY pls.trackNum")
    List<ArchiveSongObj> getSongsFromPlaylist(int playlist_id);

    @Query(" INSERT INTO playlistSongsTbl(playlist_id,song_id,trackNum) " +
            "SELECT :playlist_id " +
            "     , song._id " +
            "     , :position " +
            "  FROM songTbl song " +
            " WHERE song.fileName = :song_file_name")
    void insertKnownSongIntoPlaylist(int playlist_id, int position, String song_file_name);
}
