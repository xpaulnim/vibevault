package com.code.android.vibevault.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.code.android.vibevault.ArchiveShowObj;
import com.code.android.vibevault.data.Show;

import java.util.List;

@Dao
public interface ShowDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertShow(Show show);

    @Update
    void updateShow(Show show);

    @Query("SELECT showIdent     as identifier" +
            "    , showTitle     as showTitle" +
            "    , showArtist    as showArtist" +
            "    , showSource    as source" +
            "    , hasVBR        as hasVBR" +
            "    , hasLBR        as hasLBR" +
            "    , _id           as DBID" +
            " FROM showTbl " +
            "WHERE showIdent = :identifier")
    List<ArchiveShowObj> getShow(String identifier);

    @Query("SELECT showIdent     as identifier" +
            "    , showTitle     as showTitle" +
            "    , showArtist    as showArtist" +
            "    , showSource    as source" +
            "    , hasVBR        as hasVBR" +
            "    , hasLBR        as hasLBR" +
            "    , _id           as DBID" +
            " FROM showTbl " +
            "WHERE LTRIM(RTRIM(showTitle)) = '' OR LTRIM(RTRIM(showArtist)) = ''")
    List<ArchiveShowObj> getBadShows();

    @Query("UPDATE songTbl " +
            "SET isDownloaded = 'false', download_id = null " +
            "WHERE EXISTS (SELECT 1 FROM showTbl show " +
            "WHERE show.showIdent = :show_identifier AND show._id = show_id)")
    void setShowDeleted(String show_identifier);

    @Query("UPDATE showTbl " +
            "  SET showExists = 'true' " +
            "WHERE showIdent = :show_identifier")
    void setShowExists(String show_identifier);

    @Query("SELECT COUNT(1) AS count " +
            " FROM songTbl song " +
            "INNER JOIN showTbl show ON song.show_id = show._id " +
            "  AND show.showIdent = :show_identifier " +
            "  AND show.showExists = 'true'")
    int getShowExists(String show_identifier);

    @Query("SELECT showIdent     as identifier" +
            "    , showTitle     as showTitle" +
            "    , showArtist    as showArtist" +
            "    , showSource    as source" +
            "    , hasVBR        as hasVBR" +
            "    , hasLBR        as hasLBR" +
            "    , _id           as DBID" +
            " FROM downloadedShowsVw")
    List<ArchiveShowObj> getDownloadShows();

    @Query("UPDATE showTbl " +
            "  SET showTitle = :show_title " +
            "    , showArtist = :show_artist " +
            "WHERE showIdent = :show_identifier")
    void updateShow(String show_identifier, String show_title, String show_artist);
}
