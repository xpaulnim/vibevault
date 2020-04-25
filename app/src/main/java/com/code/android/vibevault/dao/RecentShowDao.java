package com.code.android.vibevault.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.code.android.vibevault.ArchiveShowObj;

import java.util.List;

@Dao
public interface RecentShowDao {
    @Query("INSERT INTO recentShowsTbl(show_id) " +
            "SELECT show._id " +
            "  FROM showTbl show " +
            " WHERE show.showIdent = :show_ident AND NOT EXISTS (" +
            "       SELECT 1 " +
            "         FROM recentShowsVw recent " +
            "        WHERE recent.showIdent = :show_ident)")
    void insertRecentShow(String show_ident);

    @Query("DELETE FROM recentShowsTbl")
    void clearRecentShows();

    @Query("DELETE FROM recentShowsTbl " +
            "WHERE show_id = :show_id")
    void deleteRecentShow(long show_id);

    @Query("SELECT showIdent     as identifier " +
            "    , showTitle     as showTitle " +
            "    , showArtist    as showArtist " +
            "    , showSource    as source " +
            "    , hasVBR        as hasVBR " +
            "    , hasLBR        as hasLBR " +
            "    , _id           as DBID " +
            " FROM recentShowsVw")
    List<ArchiveShowObj> getRecentShows();
}
