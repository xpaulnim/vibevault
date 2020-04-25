package com.code.android.vibevault.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.code.android.vibevault.ArchiveSongObj;
import com.code.android.vibevault.data.Song;

import java.util.List;

@Dao
public interface SongDao {

    @Query(" INSERT INTO songTbl(fileName,songTitle,show_id,isDownloaded,folderName) " +
            "SELECT :song_filename, :song_title,show._id, 'false', '' " +
            "  FROM showTbl show " +
            " WHERE show.showIdent = :song_show_identifier" +
            "   AND NOT EXISTS (SELECT 1 FROM songTbl song WHERE song.fileName = :song_filename)")
    void insertSong(String song_show_identifier, String song_title, String song_filename);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSong(List<Song> song);

    @Query("UPDATE songTbl " +
            "  SET download_id = :download_id " +
            "WHERE fileName = :song_filename")
    void setSongIsDownloading(long download_id, String song_filename);

    @Query("SELECT count(1) AS count " +
            " FROM songTbl " +
            "WHERE fileName = :song_filename " +
            "  AND download_id IS NOT null " +
            "  AND isDownloaded = 'false'")
    int getSongIsDownloading(String song_filename);

    @Query("UPDATE songTbl " +
            "  SET isDownloaded = 'true' " +
            "WHERE download_id = :download_id")
    void setSongDownloaded(long download_id);

    @Query("UPDATE songTbl " +
            "  SET folderName = :song_folder_name " +
            "WHERE _id = :song_id")
    void setSongFolderName(long song_id, String song_folder_name);

    @Query("UPDATE songTbl " +
            "  SET isDownloaded = 'true' " +
            "WHERE fileName = :song_filename")
    void setSongDownloaded(String song_filename);

    @Query("UPDATE songTbl " +
            "  SET isDownloaded = 'false', download_id = null " +
            "WHERE fileName = :song_filename")
    void setSongDeleted(String song_filename);

    @Query("SELECT 1 " +
            " FROM songTbl song " +
            "WHERE song.fileName = :song_filename " +
            "  AND song.isDownloaded LIKE 'true'")
    List<Integer> songIsDownloaded(String song_filename);

    @Query("SELECT _id " +
            " FROM songTbl" +
            " WHERE fileName = :song_filename")
    long getSongIdByFilename(String song_filename);

    @Query("SELECT song.songTitle                                 as songTitle " +
            "    , song.folderName                                as folderName " +
            "    , song.fileName                                  as fileName " +
            "    , show.showArtist || 'Live at' || show.showTitle as showTitle " +
            "    , show.showIdent                                 as showIdent " +
            "    , show.showArtist                                as showArtist " +
            "    , song._id                                       as DBID " +
            " FROM songTbl song " +
            "INNER JOIN showTbl show ON song.show_id = show._id " +
            "  AND show.showIdent = :show_identifier")
    List<ArchiveSongObj> getSongsFromShow(String show_identifier);

    @Query("SELECT song.songTitle                                 as songTitle " +
            "    , song.folderName                                as folderName " +
            "    , song.fileName                                  as fileName " +
            "    , show.showArtist || 'Live at' || show.showTitle as showTitle " +
            "    , show.showIdent                                 as showIdent " +
            "    , show.showArtist                                as showArtist " +
            "    , song._id                                       as DBID " +
            " FROM songTbl song " +
            "INNER JOIN showTbl show ON song.show_id = show._id " +
            "  AND show.showIdent = :show_identifier " +
            "  AND song.isDownloaded = 'true'")
    List<ArchiveSongObj> getDownloadedSongsFromShow(String show_identifier);

    @Query("SELECT song.songTitle                                 as songTitle " +
            "    , song.folderName                                as folderName " +
            "    , song.fileName                                  as fileName " +
            "    , show.showArtist || 'Live at' || show.showTitle as showTitle " +
            "    , show.showIdent                                 as showIdent " +
            "    , show.showArtist                                as showArtist " +
            "    , song._id                                       as DBID " +
            " FROM songTbl song " +
            "INNER JOIN showTbl show ON song.show_id = show._id " +
            "  AND show._id = :show_id " +
            "ORDER BY song.fileName")
    List<ArchiveSongObj> getSongsFromShowKey(int show_id);
}
