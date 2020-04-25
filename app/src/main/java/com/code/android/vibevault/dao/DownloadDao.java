package com.code.android.vibevault.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.code.android.vibevault.ArchiveShowObj;
import com.code.android.vibevault.data.DownloadStatus;

import java.util.List;

@Dao
public interface DownloadDao {

    @Query("SELECT showIdent     as identifier " +
            "    , showTitle     as showTitle " +
            "    , showArtist    as showArtist " +
            "    , showSource    as source " +
            "    , hasVBR        as hasVBR " +
            "    , hasLBR        as hasLBR " +
            "    , _id           as DBID " +
            " FROM downloadedShowsVw")
    List<ArchiveShowObj> getDownloadShows();

    @Query("SELECT (" +
            "SELECT count(1)" +
            "  FROM songTbl song " +
            " INNER JOIN showTbl show ON show._id = song.show_id " +
            "   AND show.showIdent = :show_identifier " +
            " WHERE song.isDownloaded = 'true') AS 'downloaded', ( " +
            "SELECT count(1) " +
            "  FROM songTbl song " +
            " INNER JOIN showTbl show ON show._id = song.show_id " +
            "   AND show.showIdent = :show_identifier) AS 'total'")
    List<DownloadStatus> getShowDownloadStatus(String show_identifier);
}
